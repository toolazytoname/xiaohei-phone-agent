#!/usr/bin/env python3
"""Evaluate the pinned Xiaohei sherpa-onnx KWS model against a TSV WAV manifest.

The manifest has three tab-separated columns: case_id, expected (0 or 1), audio_path.
Model weights and audio remain external to Git. Output contains only case IDs and outcomes.
"""

import argparse
import json
from pathlib import Path

import numpy as np
import scipy.signal
import sherpa_onnx
import soundfile as sf


def arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model-dir", required=True, type=Path)
    parser.add_argument("--keywords", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    return parser.parse_args()


def load_audio(path: Path):
    audio, sample_rate = sf.read(path, dtype="float32", always_2d=False)
    if audio.ndim > 1:
        audio = np.mean(audio, axis=1)
    if sample_rate != 16000:
        divisor = np.gcd(sample_rate, 16000)
        audio = scipy.signal.resample_poly(audio, 16000 // divisor, sample_rate // divisor)
    return np.asarray(audio, dtype=np.float32)


def detect(spotter, audio):
    stream = spotter.create_stream()
    stream.accept_waveform(16000, audio)
    stream.accept_waveform(16000, np.zeros(int(0.8 * 16000), dtype=np.float32))
    stream.input_finished()
    result = ""
    while spotter.is_ready(stream):
        spotter.decode_stream(stream)
        current = spotter.get_result(stream)
        if current:
            result = current
            spotter.reset_stream(stream)
            break
    return result


def main():
    args = arguments()
    model = args.model_dir
    spotter = sherpa_onnx.KeywordSpotter(
        tokens=str(model / "tokens.txt"),
        encoder=str(model / "encoder-epoch-12-avg-2-chunk-16-left-64.onnx"),
        decoder=str(model / "decoder-epoch-12-avg-2-chunk-16-left-64.onnx"),
        joiner=str(model / "joiner-epoch-12-avg-2-chunk-16-left-64.onnx"),
        keywords_file=str(args.keywords),
        num_threads=2,
        provider="cpu",
        keywords_score=1.5,
        keywords_threshold=0.25,
    )
    rows = []
    for line_number, raw in enumerate(args.manifest.read_text(encoding="utf-8").splitlines(), 1):
        if not raw or raw.startswith("#"):
            continue
        columns = raw.split("\t")
        if len(columns) != 3 or columns[1] not in ("0", "1"):
            raise ValueError(f"invalid manifest line {line_number}")
        case_id, expected_text, audio_path = columns
        expected = expected_text == "1"
        keyword = detect(spotter, load_audio(Path(audio_path)))
        detected = bool(keyword)
        rows.append({
            "case_id": case_id,
            "expected": expected,
            "detected": detected,
            "passed": detected == expected,
        })
    positives = [row for row in rows if row["expected"]]
    negatives = [row for row in rows if not row["expected"]]
    report = {
        "schema_version": 1,
        "cases": len(rows),
        "passed": sum(row["passed"] for row in rows),
        "positive_hits": sum(row["detected"] for row in positives),
        "positive_total": len(positives),
        "negative_false_accepts": sum(row["detected"] for row in negatives),
        "negative_total": len(negatives),
        "results": rows,
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    raise SystemExit(0 if report["passed"] == report["cases"] else 1)


if __name__ == "__main__":
    main()
