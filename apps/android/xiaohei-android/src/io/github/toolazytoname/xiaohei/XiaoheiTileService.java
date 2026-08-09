package io.github.toolazytoname.xiaohei;

import android.content.Intent;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.service.quicksettings.TileService;
import android.service.quicksettings.Tile;

/** Visible, user-controlled entry point. It opens the app; it never records in the background. */
public final class XiaoheiTileService extends TileService {
    @Override public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("小黑说话");
            tile.updateTile();
        }
    }

    @Override public void onClick() {
        super.onClick();
        Log.i("XiaoheiTile", "user requested visible voice session");
        Intent intent = new Intent(this, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("start_talk", true);
        if (Build.VERSION.SDK_INT >= 34) {
            PendingIntent pending = PendingIntent.getActivity(this, 7, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            startActivityAndCollapse(pending);
        } else {
            startActivityAndCollapse(intent);
        }
    }
}
