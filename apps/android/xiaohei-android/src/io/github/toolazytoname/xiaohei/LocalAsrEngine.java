package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Reflection boundary for the optional, locally bundled sherpa-onnx runtime. */
final class LocalAsrEngine implements AutoCloseable {
    private static final String MODEL = "sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23/";
    private final Object recognizer;
    private final Object stream;

    LocalAsrEngine(Context context) throws Exception {
        File modelDir = new File(context.getFilesDir(), "asr/zh-14m");
        String encoder = copyAsset(context, MODEL + "encoder-epoch-99-avg-1.int8.onnx", modelDir);
        String decoder = copyAsset(context, MODEL + "decoder-epoch-99-avg-1.onnx", modelDir);
        String joiner = copyAsset(context, MODEL + "joiner-epoch-99-avg-1.int8.onnx", modelDir);
        String tokens = copyAsset(context, MODEL + "tokens.txt", modelDir);

        Object feature = configure("com.k2fsa.sherpa.onnx.FeatureConfig",
            new String[] { "setSampleRate", "setFeatureDim", "setDither" },
            new Class<?>[] { int.class, int.class, float.class },
            new Object[] { 16000, 80, 0.0f });
        Object transducer = configure("com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig",
            new String[] { "setEncoder", "setDecoder", "setJoiner" },
            new Class<?>[] { String.class, String.class, String.class },
            new Object[] { encoder, decoder, joiner });
        Class<?> transducerClass = Class.forName("com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig");
        Object model = configure("com.k2fsa.sherpa.onnx.OnlineModelConfig",
            new String[] { "setTransducer", "setTokens", "setNumThreads", "setDebug" },
            new Class<?>[] { transducerClass, String.class, int.class, boolean.class },
            new Object[] { transducer, tokens, 2, false });
        Class<?> featureClass = Class.forName("com.k2fsa.sherpa.onnx.FeatureConfig");
        Class<?> modelClass = Class.forName("com.k2fsa.sherpa.onnx.OnlineModelConfig");
        Object config = configure("com.k2fsa.sherpa.onnx.OnlineRecognizerConfig",
            new String[] { "setFeatConfig", "setModelConfig", "setEnableEndpoint" },
            new Class<?>[] { featureClass, modelClass, boolean.class },
            new Object[] { feature, model, true });
        Class<?> configClass = Class.forName("com.k2fsa.sherpa.onnx.OnlineRecognizerConfig");
        Constructor<?> ctor = Class.forName("com.k2fsa.sherpa.onnx.OnlineRecognizer")
            .getConstructor(AssetManager.class, configClass);
        recognizer = ctor.newInstance(null, config);
        stream = invoke(recognizer, "createStream", new Class<?>[] { String.class }, "");
    }

    void accept(short[] pcm, int count) throws Exception {
        float[] samples = new float[count];
        for (int i = 0; i < count; i++) samples[i] = pcm[i] / 32768.0f;
        invoke(stream, "acceptWaveform", new Class<?>[] { float[].class, int.class }, samples, 16000);
        while ((Boolean) invoke(recognizer, "isReady", new Class<?>[] { stream.getClass() }, stream)) {
            invoke(recognizer, "decode", new Class<?>[] { stream.getClass() }, stream);
        }
    }

    boolean isEndpoint() throws Exception {
        return (Boolean) invoke(recognizer, "isEndpoint", new Class<?>[] { stream.getClass() }, stream);
    }

    String text() throws Exception {
        Object result = invoke(recognizer, "getResult", new Class<?>[] { stream.getClass() }, stream);
        return (String) invoke(result, "getText", new Class<?>[0]);
    }

    @Override public void close() {
        try { invoke(stream, "release", new Class<?>[0]); } catch (Exception ignored) { }
        try { invoke(recognizer, "release", new Class<?>[0]); } catch (Exception ignored) { }
    }

    static boolean isBundled() {
        try {
            Class.forName("com.k2fsa.sherpa.onnx.OnlineRecognizer");
            return true;
        } catch (ClassNotFoundException unavailable) {
            return false;
        }
    }

    private static Object configure(String className, String[] setters, Class<?>[] types,
            Object[] values) throws Exception {
        Class<?> type = Class.forName(className);
        Object value = type.getConstructor().newInstance();
        for (int i = 0; i < setters.length; i++) {
            value.getClass().getMethod(setters[i], types[i]).invoke(value, values[i]);
        }
        return value;
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... values)
            throws Exception {
        Method method = target.getClass().getMethod(name, types);
        return method.invoke(target, values);
    }

    private static String copyAsset(Context context, String asset, File modelDir) throws Exception {
        modelDir.mkdirs();
        File output = new File(modelDir, asset.substring(asset.lastIndexOf('/') + 1));
        if (output.isFile() && output.length() > 0) return output.getAbsolutePath();
        File pending = new File(output.getAbsolutePath() + ".pending");
        try (InputStream input = context.getAssets().open(asset);
             FileOutputStream sink = new FileOutputStream(pending)) {
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) sink.write(buffer, 0, count);
            sink.getFD().sync();
        }
        if (!pending.renameTo(output)) throw new IllegalStateException("ASR model install failed");
        return output.getAbsolutePath();
    }
}
