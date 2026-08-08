package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** First vertical slice: a manual wake event is routed to the safe gallery action. */
public final class MainActivity extends Activity implements WakewordBroker.Listener {
    private TextView stateView;
    private TextView historyView;
    private Button armButton;
    private final GalleryActionAdapter gallery = new GalleryActionAdapter();
    private WakewordBroker broker;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broker = new WakewordBroker(this);
        setContentView(buildView());
        onStateChanged(broker.state(), "尚未启用");
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("小黑 / Xiaohei\n第一条安全动作链路");
        title.setTextSize(24);
        root.addView(title);

        stateView = new TextView(this);
        stateView.setTextSize(17);
        stateView.setPadding(0, pad, 0, pad);
        root.addView(stateView);

        armButton = new Button(this);
        armButton.setText("启用基础模式");
        armButton.setOnClickListener(v -> {
            if (broker.state() == WakewordBroker.State.OFF || broker.state() == WakewordBroker.State.ERROR) broker.armManualMode();
            else broker.disarm();
        });
        root.addView(armButton);

        Button testButton = new Button(this);
        testButton.setText("模拟“小布小布”命中 → 打开相册");
        testButton.setOnClickListener(v -> broker.dispatchManualHit());
        root.addView(testButton);

        historyView = new TextView(this);
        historyView.setGravity(Gravity.START);
        historyView.setPadding(0, pad, 0, 0);
        root.addView(historyView);
        return root;
    }

    @Override public void onStateChanged(WakewordBroker.State state, String detail) {
        if (stateView == null) return;
        stateView.setText("状态：" + state + "\n" + detail);
        armButton.setText(state == WakewordBroker.State.ARMED ? "关闭基础模式" : "启用基础模式");
    }

    @Override public void onWakewordHit(WakewordEvent event) {
        boolean opened = gallery.openGallery(this);
        historyView.setText("最近事件：" + event.source + " · " + event.keywordId
            + " · 动作：打开相册/系统图片选择器" + (opened ? "（已发起）" : "（设备无可用图片应用）"));
    }
}
