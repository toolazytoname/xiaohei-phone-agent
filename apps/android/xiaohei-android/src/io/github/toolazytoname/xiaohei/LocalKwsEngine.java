package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.res.AssetManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Reflection boundary for the optional sherpa-onnx keyword spotter. */
final class LocalKwsEngine implements AutoCloseable {
    private static final String MODEL = "sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01/";
    private final Object spotter;
    private final Object stream;

    LocalKwsEngine(Context context) throws Exception {
        Object feature = configure("com.k2fsa.sherpa.onnx.FeatureConfig",
            new String[] { "setSampleRate", "setFeatureDim" },
            new Class<?>[] { int.class, int.class }, new Object[] { 16000, 80 });
        Object transducer = configure("com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig",
            new String[] { "setEncoder", "setDecoder", "setJoiner" },
            new Class<?>[] { String.class, String.class, String.class },
            new Object[] { MODEL + "encoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                MODEL + "decoder-epoch-12-avg-2-chunk-16-left-64.onnx",
                MODEL + "joiner-epoch-12-avg-2-chunk-16-left-64.onnx" });
        Class<?> transducerType = Class.forName("com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig");
        Object model = configure("com.k2fsa.sherpa.onnx.OnlineModelConfig",
            new String[] { "setTransducer", "setTokens", "setNumThreads", "setProvider", "setDebug" },
            new Class<?>[] { transducerType, String.class, int.class, String.class, boolean.class },
            new Object[] { transducer, MODEL + "tokens.txt", 2, "cpu", false });
        Class<?> featureType = Class.forName("com.k2fsa.sherpa.onnx.FeatureConfig");
        Class<?> modelType = Class.forName("com.k2fsa.sherpa.onnx.OnlineModelConfig");
        Object config = configure("com.k2fsa.sherpa.onnx.KeywordSpotterConfig",
            new String[] { "setFeatConfig", "setModelConfig", "setKeywordsFile",
                "setKeywordsScore", "setKeywordsThreshold" },
            new Class<?>[] { featureType, modelType, String.class, float.class, float.class },
            new Object[] { feature, model, MODEL + "keywords.txt", 1.5f, 0.25f });
        Class<?> configType = Class.forName("com.k2fsa.sherpa.onnx.KeywordSpotterConfig");
        Constructor<?> ctor = Class.forName("com.k2fsa.sherpa.onnx.KeywordSpotter")
            .getConstructor(AssetManager.class, configType);
        spotter = ctor.newInstance(context.getAssets(), config);
        stream = invoke(spotter, "createStream", new Class<?>[] { String.class }, "");
    }

    String accept(short[] pcm, int count) throws Exception {
        float[] samples = new float[count];
        for (int i = 0; i < count; i++) samples[i] = pcm[i] / 32768.0f;
        invoke(stream, "acceptWaveform", new Class<?>[] { float[].class, int.class }, samples, 16000);
        while ((Boolean) invoke(spotter, "isReady", new Class<?>[] { stream.getClass() }, stream))
            invoke(spotter, "decode", new Class<?>[] { stream.getClass() }, stream);
        Object result = invoke(spotter, "getResult", new Class<?>[] { stream.getClass() }, stream);
        return (String) invoke(result, "getKeyword", new Class<?>[0]);
    }

    @Override public void close() {
        try { invoke(stream, "release", new Class<?>[0]); } catch (Exception ignored) { }
        try { invoke(spotter, "release", new Class<?>[0]); } catch (Exception ignored) { }
    }

    static boolean isBundled() {
        try { Class.forName("com.k2fsa.sherpa.onnx.KeywordSpotter"); return true; }
        catch (ClassNotFoundException unavailable) { return false; }
    }

    private static Object configure(String name, String[] setters, Class<?>[] types, Object[] values)
            throws Exception {
        Object value = Class.forName(name).getConstructor().newInstance();
        for (int i = 0; i < setters.length; i++) value.getClass().getMethod(setters[i], types[i])
            .invoke(value, values[i]);
        return value;
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... values)
            throws Exception {
        Method method = target.getClass().getMethod(name, types);
        return method.invoke(target, values);
    }
}
