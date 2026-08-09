package android.hardware.soundtrigger;

import android.media.permission.Identity;
import android.os.Handler;
import java.util.ArrayList;
import java.util.UUID;

/** Compile-only Android hidden-API stub. Runtime resolution uses framework.jar. */
public final class SoundTrigger {
    public static int listModulesAsOriginator(ArrayList<ModuleProperties> modules, Identity identity) {
        throw new UnsupportedOperationException("compile-only stub");
    }

    public static SoundTriggerModule attachModuleAsOriginator(
            int moduleId, StatusListener listener, Handler handler, Identity identity) {
        throw new UnsupportedOperationException("compile-only stub");
    }

    public interface StatusListener {
        void onRecognition(RecognitionEvent event);
        void onResourcesAvailable();
        void onServiceDied();
        void onModelUnloaded(int modelHandle);
    }

    public static class RecognitionEvent { }

    public abstract static class SoundModel { }

    public static final class KeyphraseSoundModel extends SoundModel {
        public KeyphraseSoundModel(UUID uuid, UUID vendorUuid, byte[] data, Keyphrase[] keyphrases) {
            throw new UnsupportedOperationException("compile-only stub");
        }
    }

    public static final class Keyphrase {
        public Keyphrase(int id, int recognitionModes, java.util.Locale locale,
                String text, int[] users) {
            throw new UnsupportedOperationException("compile-only stub");
        }
    }

    public static final class ModuleProperties {
        public int getId() { throw new UnsupportedOperationException("compile-only stub"); }
        public UUID getUuid() { throw new UnsupportedOperationException("compile-only stub"); }
    }
}
