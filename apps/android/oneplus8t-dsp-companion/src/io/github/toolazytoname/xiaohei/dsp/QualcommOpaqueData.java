package io.github.toolazytoname.xiaohei.dsp;

/** Minimal SVA 3/4 confidence-level TLV, derived from the matching OEM client ABI. */
final class QualcommOpaqueData {
    private static final int PARAM_SIZE = 0x22b8;
    private static final int MODEL_STRIDE = 0x378;

    static byte[] stockSva4(int firstStageKeyphrase, int secondStageKeyphrase) {
        byte[] data = new byte[8 + PARAM_SIZE];
        putInt(data, 0, 0); // ST_PARAM_KEY_CONFIDENCE_LEVELS
        putInt(data, 4, PARAM_SIZE);
        putInt(data, 8, 2); // confidence-level ABI version
        putInt(data, 12, 3); // first stage, second-stage keyphrase, user stage
        putModel(data, 0, 1, firstStageKeyphrase);
        putModel(data, 1, 2, secondStageKeyphrase);
        putModel(data, 2, 4, 0);
        return data;
    }

    private static void putModel(byte[] data, int index, int modelId, int keywordLevel) {
        int base = index * MODEL_STRIDE;
        putInt(data, base + 16, modelId);
        putInt(data, base + 20, 1); // one keyphrase
        putInt(data, base + 24, keywordLevel);
        putInt(data, base + 28, 0); // no trained users in the stock model
    }

    private static void putInt(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
    }

    private QualcommOpaqueData() { }
}
