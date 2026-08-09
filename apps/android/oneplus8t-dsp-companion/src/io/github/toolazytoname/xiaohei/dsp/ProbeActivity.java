package io.github.toolazytoname.xiaohei.dsp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A narrow preflight probe: it never loads a model or captures audio. */
public final class ProbeActivity extends Activity {
    private TextView status;

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
        title.setText("小黑 DSP 伴随组件\nOnePlus 8T 预检（不加载模型）");
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
        return root;
    }

    private void refresh() {
        SoundTriggerGateway.ProbeResult result = SoundTriggerGateway.probe(this);
        status.setText(result.toDisplayText());
    }
}
