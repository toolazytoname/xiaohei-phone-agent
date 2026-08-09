package android.hardware.soundtrigger;

import android.media.permission.Identity;
import java.util.ArrayList;
import java.util.UUID;

/** Compile-only Android hidden-API stub. Runtime resolution uses framework.jar. */
public final class SoundTrigger {
    public static int listModulesAsOriginator(ArrayList<ModuleProperties> modules, Identity identity) {
        throw new UnsupportedOperationException("compile-only stub");
    }

    public static final class ModuleProperties {
        public int getId() { throw new UnsupportedOperationException("compile-only stub"); }
        public UUID getUuid() { throw new UnsupportedOperationException("compile-only stub"); }
    }
}
