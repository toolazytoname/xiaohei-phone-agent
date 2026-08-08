package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;

/** Low-risk public Android Intent only; no shell, accessibility, or app-private protocol. */
final class GalleryActionAdapter {
    boolean openGallery(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        }
        // Lineage-style builds may deliberately omit a gallery application. The
        // Android 13+ system picker is still a visible, public, low-risk photo UI.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent picker = new Intent(MediaStore.ACTION_PICK_IMAGES);
            picker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (picker.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(picker);
                return true;
            }
        }
        return false;
    }
}
