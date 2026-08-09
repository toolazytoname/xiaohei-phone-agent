package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import java.net.URLEncoder;

/** Public Android API adapters for the allowlisted deterministic actions. */
final class ActionDispatcher {
    static final class Result {
        final boolean ok;
        final String detail;
        Result(boolean ok, String detail) { this.ok = ok; this.detail = detail; }
    }

    private final GalleryActionAdapter gallery = new GalleryActionAdapter();

    Result execute(Activity activity, CommandRouter.Request request) {
        try {
            switch (request.action) {
                case OPEN_GALLERY:
                    return result(gallery.openGallery(activity), "打开相册/系统图片选择器");
                case OPEN_SETTINGS:
                    return start(activity, new Intent(Settings.ACTION_SETTINGS), "打开系统设置");
                case OPEN_WIFI_SETTINGS:
                    return start(activity, new Intent(Settings.ACTION_WIFI_SETTINGS), "打开 Wi-Fi 设置");
                case OPEN_BLUETOOTH_SETTINGS:
                    return start(activity, new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "打开蓝牙设置");
                case OPEN_CAMERA:
                    return start(activity, new Intent(MediaStore.ACTION_IMAGE_CAPTURE), "打开相机");
                case OPEN_BROWSER:
                    return start(activity, new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.baidu.com/")), "打开浏览器");
                case OPEN_DIALER:
                    return start(activity, new Intent(Intent.ACTION_DIAL), "打开拨号盘（未拨出）");
                case OPEN_ALARMS:
                    return start(activity, new Intent(AlarmClock.ACTION_SHOW_ALARMS), "打开闹钟");
                case NAVIGATE:
                    String encoded = URLEncoder.encode(request.argument, "UTF-8");
                    return start(activity, new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query=" + encoded)),
                        "打开导航搜索：" + request.argument);
                case TORCH_ON:
                    return torch(activity, true);
                case TORCH_OFF:
                    return torch(activity, false);
                case VOLUME_UP:
                    return volume(activity, AudioManager.ADJUST_RAISE, "调大媒体音量");
                case VOLUME_DOWN:
                    return volume(activity, AudioManager.ADJUST_LOWER, "调小媒体音量");
                default:
                    return new Result(false, "未匹配允许的命令");
            }
        } catch (Exception error) {
            return new Result(false, "动作不可用：" + error.getClass().getSimpleName());
        }
    }

    private static Result start(Activity activity, Intent intent, String detail) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException unavailable) {
            return new Result(false, detail + "；设备没有可处理应用");
        }
        return new Result(true, detail + "（已发起）");
    }

    private static Result volume(Activity activity, int direction, String detail) {
        activity.getSystemService(AudioManager.class).adjustStreamVolume(
            AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
        return new Result(true, detail);
    }

    private static Result torch(Activity activity, boolean enabled) throws Exception {
        CameraManager manager = activity.getSystemService(CameraManager.class);
        for (String cameraId : manager.getCameraIdList()) {
            Boolean available = manager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (Boolean.TRUE.equals(available)) {
                manager.setTorchMode(cameraId, enabled);
                return new Result(true, enabled ? "手电筒已打开" : "手电筒已关闭");
            }
        }
        return new Result(false, "设备没有可用闪光灯");
    }

    private static Result result(boolean ok, String detail) {
        return new Result(ok, detail + (ok ? "（已发起）" : "；设备不可用"));
    }
}
