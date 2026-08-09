package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.MediaStore;

/** Low-risk public Android Intent only; no shell, accessibility, or app-private protocol. */
final class GalleryActionAdapter {
    boolean openGallery(Context context) {
        // Ask Android for the installed gallery application first, then pin the
        // exact resolved component. A broad image/* VIEW intent can be claimed
        // by file receivers and app stores (observed with Tencent App Store).
        Intent gallery = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_GALLERY);
        ResolveInfo resolvedGallery = context.getPackageManager().resolveActivity(gallery, 0);
        if (resolvedGallery != null && resolvedGallery.activityInfo != null) {
            gallery.setClassName(resolvedGallery.activityInfo.packageName,
                resolvedGallery.activityInfo.name);
            gallery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(gallery);
            return true;
        }
        // The Android 13+ system picker is a visible, public, low-risk fallback
        // and cannot be redirected to an arbitrary image/file handler.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent picker = new Intent(MediaStore.ACTION_PICK_IMAGES);
            picker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (picker.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(picker);
                return true;
            }
        }
        // Compatibility fallback for older devices that have neither an
        // APP_GALLERY activity nor the system picker.
        Intent view = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (view.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(view);
            return true;
        }
        return false;
    }
}
