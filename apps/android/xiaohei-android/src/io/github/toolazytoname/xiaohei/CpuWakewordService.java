package io.github.toolazytoname.xiaohei;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

/** Explicit opt-in foreground CPU keyword spotting. This is not the DSP backend. */
public final class CpuWakewordService extends Service {
    static final String ACTION_START = "io.github.toolazytoname.xiaohei.cpu.START";
    static final String ACTION_STOP = "io.github.toolazytoname.xiaohei.cpu.STOP";
    static final String ACTION_RESUME = "io.github.toolazytoname.xiaohei.cpu.RESUME";
    static final String STATUS_EVENT = "io.github.toolazytoname.xiaohei.cpu.STATUS";
    private static final String CHANNEL = "xiaohei_cpu_wakeword";
    private static final int ID = 1211;
    private volatile boolean running;
    private Thread worker;
    private AudioRecord recorder;

    @Override public void onCreate() {
        super.onCreate();
        getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(
            CHANNEL, "小黑 CPU 唤醒（高功耗）", NotificationManager.IMPORTANCE_LOW));
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) { stopListening("OFF", "已停止并释放麦克风"); stopSelf(); return START_NOT_STICKY; }
        startForeground(ID, notification("正在准备“小黑小黑”监听"));
        if (!running) startListening();
        return START_NOT_STICKY;
    }

    private synchronized void startListening() {
        if (running) return;
        running = true;
        worker = new Thread(this::listenLoop, "xiaohei-cpu-kws");
        worker.start();
    }

    private void listenLoop() {
        int min = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        try (LocalKwsEngine engine = new LocalKwsEngine(this)) {
            recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 8192));
            recorder.startRecording();
            publish("LISTENING", "CPU 实验模式正在监听“小黑小黑”（非 DSP，耗电较高）");
            short[] pcm = new short[1600];
            while (running) {
                int count = recorder.read(pcm, 0, pcm.length);
                if (count <= 0) continue;
                String keyword = engine.accept(pcm, count);
                if (keyword != null && !keyword.isEmpty()) {
                    Log.i("XiaoheiCpuKws", "keyword=" + keyword);
                    stopListening("DETECTED", "已听到“小黑小黑”；麦克风已交给短命令会话");
                    Intent wake = new Intent(WakewordReceiver.ACTION).setPackage(getPackageName())
                        .putExtra(WakewordReceiver.EXTRA_KEYWORD_ID, "xiaohei-xiaohei")
                        .putExtra(WakewordReceiver.EXTRA_CONFIDENCE, -1)
                        .putExtra(WakewordReceiver.EXTRA_CAPTURE_AVAILABLE, false)
                        .putExtra(WakewordReceiver.EXTRA_SOURCE, "CPU_KWS");
                    sendBroadcast(wake, "io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT");
                    break;
                }
            }
        } catch (Exception error) {
            Log.e("XiaoheiCpuKws", "listener failed", error);
            stopListening("ERROR", "启动失败：" + error.getClass().getSimpleName());
            stopSelf();
        }
    }

    private synchronized void stopListening(String state, String detail) {
        running = false;
        AudioRecord active = recorder;
        recorder = null;
        if (active != null) { try { active.stop(); } catch (Exception ignored) { } active.release(); }
        publish(state, detail);
    }

    private void publish(String state, String detail) {
        getSharedPreferences("cpu_wakeword", MODE_PRIVATE).edit().putString("state", state)
            .putString("detail", detail).apply();
        sendBroadcast(new Intent(STATUS_EVENT).setPackage(getPackageName())
            .putExtra("state", state).putExtra("detail", detail));
        if (!"OFF".equals(state)) getSystemService(NotificationManager.class).notify(ID, notification(detail));
    }

    private Notification notification(String detail) {
        PendingIntent open = PendingIntent.getActivity(this, 31,
            new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(this, 32,
            new Intent(this, CpuWakewordService.class).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_xiaohei_tile)
            .setContentTitle("小黑小黑 · CPU 实验唤醒").setContentText(detail)
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(new Notification.Action.Builder(null, "停止并释放麦克风", stop).build()).build();
    }

    @Override public void onDestroy() { stopListening("OFF", "服务已销毁"); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
