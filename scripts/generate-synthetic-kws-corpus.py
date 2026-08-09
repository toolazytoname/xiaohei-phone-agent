#!/usr/bin/env python3
"""Generate a disposable macOS speech-synthesis corpus for Xiaohei KWS checks.

The generated audio is test input only and must not be committed. The TSV manifest
contains opaque case IDs, expected outcomes, and local paths.
"""

import argparse
import subprocess
from pathlib import Path

import numpy as np
import scipy.signal
import soundfile as sf


VOICES = ("Eddy", "Flo", "Grandma", "Grandpa", "Reed", "Rocko", "Sandy", "Shelley", "Tingting")
NEGATIVE_PHRASES = ("小布小布", "小爱小爱", "小黑", "打开相册")


def arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, type=Path)
    return parser.parse_args()


def synthesize(voice: str, phrase: str, output: Path):
    subprocess.run(("say", "-v", voice, "-o", str(output), phrase), check=True)


def load_mono_16k(path: Path):
    audio, sample_rate = sf.read(path, dtype="float32", always_2d=False)
    if audio.ndim > 1:
        audio = np.mean(audio, axis=1)
    if sample_rate != 16000:
        divisor = np.gcd(sample_rate, 16000)
        audio = scipy.signal.resample_poly(audio, 16000 // divisor, sample_rate // divisor)
    return np.asarray(audio, dtype=np.float32)


def normalized(audio):
    peak = float(np.max(np.abs(audio))) if audio.size else 0.0
    return audio if peak <= 0.98 or peak == 0 else audio * (0.98 / peak)


def write_variant(path: Path, audio):
    sf.write(path, normalized(np.asarray(audio, dtype=np.float32)), 16000, subtype="PCM_16")


def main():
    args = arguments()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    rng = np.random.default_rng(20260809)
    rows = []

    for voice_index, voice in enumerate(VOICES, 1):
        source = output / f"p{voice_index:02d}-source.aiff"
        synthesize(voice, "小黑小黑", source)
        audio = load_mono_16k(source)

        clean = output / f"p{voice_index:02d}-clean.wav"
        write_variant(clean, audio)
        rows.append((f"p{voice_index:02d}-clean", 1, clean))

        quiet = output / f"p{voice_index:02d}-quiet.wav"
        write_variant(quiet, audio * 0.35)
        rows.append((f"p{voice_index:02d}-quiet", 1, quiet))

        signal_power = max(float(np.mean(audio * audio)), 1e-8)
        noise = rng.normal(0, np.sqrt(signal_power / 10.0), audio.shape)
        noisy = output / f"p{voice_index:02d}-noise10db.wav"
        write_variant(noisy, audio + noise)
        rows.append((f"p{voice_index:02d}-noise10db", 1, noisy))

        impulse = np.zeros(2401, dtype=np.float32)
        impulse[[0, 800, 1600, 2400]] = [1.0, 0.32, 0.18, 0.10]
        reverberant = scipy.signal.fftconvolve(audio, impulse)[: len(audio) + 2400]
        reverb = output / f"p{voice_index:02d}-reverb.wav"
        write_variant(reverb, reverberant)
        rows.append((f"p{voice_index:02d}-reverb", 1, reverb))

        for phrase_index, phrase in enumerate(NEGATIVE_PHRASES, 1):
            negative_source = output / f"n{voice_index:02d}-{phrase_index:02d}-source.aiff"
            synthesize(voice, phrase, negative_source)
            negative = output / f"n{voice_index:02d}-{phrase_index:02d}.wav"
            write_variant(negative, load_mono_16k(negative_source))
            rows.append((f"n{voice_index:02d}-{phrase_index:02d}", 0, negative))

    manifest = output / "manifest.tsv"
    manifest.write_text(
        "# case_id\texpected\taudio_path\n"
        + "".join(f"{case_id}\t{expected}\t{path}\n" for case_id, expected, path in rows),
        encoding="utf-8",
    )
    print(manifest)


if __name__ == "__main__":
    main()
