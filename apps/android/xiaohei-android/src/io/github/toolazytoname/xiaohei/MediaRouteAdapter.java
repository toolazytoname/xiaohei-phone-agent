package io.github.toolazytoname.xiaohei;
import android.content.Context; import android.media.AudioDeviceInfo; import android.media.AudioManager; import java.util.HashMap; import java.util.Map;
/** Android media-route observation plus bounded relative volume; no session control or route forcing. */
final class MediaRouteAdapter {
 private final AudioManager audio; MediaRouteAdapter(Context c){audio=c.getApplicationContext().getSystemService(AudioManager.class);}
 Map<String,String> execute(MediaControlPolicy.Operation op){Map<String,String> r=new HashMap<>();if(op==MediaControlPolicy.Operation.VOLUME_UP||op==MediaControlPolicy.Operation.VOLUME_DOWN){int before=audio.getStreamVolume(AudioManager.STREAM_MUSIC);audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,op==MediaControlPolicy.Operation.VOLUME_UP?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);r.put("before",String.valueOf(before));r.put("after",String.valueOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC)));}r.put("route",route());return r;}
 private String route(){for(AudioDeviceInfo d:audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)){int t=d.getType();if(t==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP||t==AudioDeviceInfo.TYPE_BLE_HEADSET)return "bluetooth";if(t==AudioDeviceInfo.TYPE_WIRED_HEADPHONES||t==AudioDeviceInfo.TYPE_WIRED_HEADSET)return "wired";}return "speaker_or_system";}
}
