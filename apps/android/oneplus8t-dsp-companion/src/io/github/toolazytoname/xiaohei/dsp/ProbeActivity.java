package io.github.toolazytoname.xiaohei.dsp;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A narrow lifecycle probe: it never starts recognition or captures audio. */
public final class ProbeActivity extends Activity {
    private static final int REQUEST_RECORD_AUDIO = 52;
    private TextView status;
    private boolean loadAfterPermission;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildView());
        refresh();
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("小黑 DSP 伴随组件\nOnePlus 8T 低功耗识别探针");
        title.setTextSize(23);
        root.addView(title);
        status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(0, pad, 0, pad);
        root.addView(status);
        Button refresh = new Button(this);
        refresh.setText("运行只读 SoundTrigger 预检");
        refresh.setOnClickListener(v -> refresh());
        root.addView(refresh);
        Button attach = new Button(this);
        attach.setText("Attach 模块（不加载模型）");
        attach.setOnClickListener(v -> attachWithPermission());
        root.addView(attach);
        Button load = new Button(this);
        load.setText("加载私有模型（不启动识别）");
        load.setOnClickListener(v -> loadWithPermission());
        root.addView(load);
        Button unload = new Button(this);
        unload.setText("卸载模型（保持 Attach）");
        unload.setOnClickListener(v -> show(SoundTriggerGateway.unloadModel(this)));
        root.addView(unload);
        Button start = new Button(this);
        start.setText("启动低功耗识别（等待小布小布）");
        start.setOnClickListener(v -> show(SoundTriggerGateway.startRecognition(this)));
        root.addView(start);
        Button stop = new Button(this);
        stop.setText("停止识别（保留模型）");
        stop.setOnClickListener(v -> show(SoundTriggerGateway.stopRecognition(this)));
        root.addView(stop);
        Button detach = new Button(this);
        detach.setText("Detach 并释放模块");
        detach.setOnClickListener(v -> show(SoundTriggerGateway.detach(this)));
        root.addView(detach);
        return root;
    }

    private void refresh() {
        show(SoundTriggerGateway.probe(this));
    }

    private void show(SoundTriggerGateway.ProbeResult result) {
        status.setText(result.toDisplayText());
    }

    private void attachWithPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            loadAfterPermission = false;
            requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO);
            return;
        }
        show(SoundTriggerGateway.attach(this));
    }

    private void loadWithPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            loadAfterPermission = true;
            requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO);
            return;
        }
        show(SoundTriggerGateway.loadModel(this));
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != REQUEST_RECORD_AUDIO) return;
        if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED) {
            if (loadAfterPermission) loadWithPermission(); else attachWithPermission();
        }
        else status.setText("预检：未通过\n用户未授予 RECORD_AUDIO；未 attach");
    }

    @Override protected void onDestroy() {
        SoundTriggerGateway.detach(this);
        super.onDestroy();
    }
}
