# DSP wake-word device candidates

[简体中文](dsp-device-candidates.zh-CN.md)

## Read the ratings correctly

Three different claims are often collapsed into “this phone supports DSP”:

1. The hardware contains a low-power audio DSP.
2. The stock OEM assistant can wake from that DSP.
3. Xiaohei can load a compatible model and receive callbacks through the current ROM.

Only the third claim matters to a Xiaohei DSP backend. AOSP explicitly describes SoundTrigger models as an opaque, vendor-specific hidden contract and says SoundTrigger applications are intended to be vendor-provided. Android's management and hotword capture permissions are also system/privileged/role permissions. Hardware presence alone is therefore weak evidence.

The ratings below are engineering priors, not statistically calibrated percentages. Every new hardware/ROM pair remains unsupported until it produces a real `load → start → acoustic trigger → callback → unload` record and a clean rollback.

## Candidate tiers

| Tier | Candidate group | Stock/OEM hotword | Reusing an existing vendor model for Xiaohei | Custom “Xiaohei” model | Reason |
|---|---|---|---|---|---|
| S — Proven | OnePlus 8T `KB2000`, current LineageOS 21 profile | Proven | Proven with the locally extracted stock SVA 4 phrase | Unproven | Real screen-off ADSP/LPI callback at confidence 99 and clean rollback |
| A — Highest next probability | OnePlus 8 / 8 Pro / other matching 8T variants on the SM8250 family | Strong evidence | High probability after exact OTA/ABI checks | Unknown, likely harder | Same OnePlus SM8250 common platform as the proven 8T and the common blob list includes Kona SoundTrigger and Qualcomm SVA libraries |
| A− — High probability | OnePlus 9 / 9 Pro on SM8350 | Strong evidence | High-to-medium probability | Unknown | Same vendor lineage; common blobs include CNN, RNN, VOP, and ListenSoundModel, but the SoC/HAL generation changed |
| B+ — Good research targets | Xiaomi SM8250 examples such as Mi 10 (`umi`) and POCO F2 Pro/Redmi K30 Pro (`lmi`); SM8350 example Mi 11 (`venus`) | Strong platform evidence | Medium probability | Low-to-medium | Lineage common trees include SoundTrigger/SVA assets, but Xiaomi's client, model, permissions, configs, and SELinux differ from OnePlus |
| B — Technically capable, more adaptation | OnePlus 7 family (SM8150); OnePlus 11/12 families (SM8550/SM8650) | Strong platform evidence | Medium | Low-to-medium | Older models use an earlier SVA/enrollment generation; newer models use a newer 64-bit vendor library generation, so the proven bridge is not ABI-compatible |
| C — OEM works, third-party reuse is closed | Pixel 6/6 Pro Tensor reference; supported Galaxy devices with Hey Google or Bixby | Yes for OEM phrase | Low | Low without OEM/system cooperation | Pixel has its own `gs101` STHAL and Google enrollment; Samsung documents Bixby wake-up, but neither proves a public custom-model contract |
| C− — Hardware-first guess | Other Snapdragon flagship phones with an OEM always-on assistant but no inspected firmware profile | Often | Low-to-unknown | Low-to-unknown | Qualcomm branding or a Hexagon DSP does not prove the model, plugin, permission, or client contract |
| D — Not viable in the current ROM | Emulator; only AOSP fake STHAL; no vendor SoundTrigger module; HAL reports no usable model slots | No hardware path | No | No | There is no hardware SoundTrigger route to target |
| D — Direct DSP install blocked | Locked stock device where Xiaohei cannot obtain the assistant role or privileged/system integration and no compatible vendor model path is exposed | OEM may still work | No under the current method | No under the current method | The OEM can use its private chain, but Xiaohei cannot join it; Tier A manual invocation still works |

“Proven” on the OnePlus 8T currently means the stock phrase and exact tested software profile. It does not mean a redistributable one-click APK, and it does not yet prove the custom phrase “Xiaohei.”

## Why the OnePlus 8 and 9 families rank first

- The LineageOS device repositories for OnePlus 8 (`instantnoodle`), 8 Pro (`instantnoodlep`), and 8T (`kebab`) all depend on the same `android_device_oneplus_sm8250-common` tree.
- That SM8250 common blob list contains `sound_trigger.primary.kona.so`, `libcapiv2svacnn.so`, `libcapiv2vop.so`, and `liblistensoundmodel2.so`.
- OnePlus 9 (`lemonade`) and 9 Pro (`lemonadep`) share the SM8350 common tree, whose blob list includes the CNN, RNN, VOP, and ListenSoundModel libraries.

This removes several unknowns but not the model/client compatibility check. The next device should therefore be an 8 or 8 Pro before jumping to a different OEM.

## Why Pixel and Samsung are not top Xiaohei targets

- Google's Pixel 6/6 Pro device tree includes Google `HotwordEnrollment...FUSIONPro` packages and `sound_trigger.primary.gs101.so`, proving a vendor hotword stack exists—not that Xiaohei can supply an arbitrary model.
- Samsung publicly supports hands-free Bixby wake-up on compatible Galaxy devices, proving the OEM feature exists. The model format, enrollment, permissions, and system client remain Samsung-controlled.

They are excellent examples of finished OEM user experience, but poor first targets for reusing our Qualcomm SVA work.

## Six-gate read-only assessment

Run these gates in order and stop when a hard requirement is absent:

1. **Framework:** `soundtrigger` and `soundtrigger_middleware` services exist.
2. **Real HAL:** middleware lists a non-fake vendor module with usable model/phrase limits.
3. **Low-power config:** vendor/ODM config declares an ADSP/DSP execution path, LPI or hardware-hotword capture.
4. **Algorithm closure:** matching model parser, second-stage plugin, model file, vendor UUID, and ABI are present in the same firmware family.
5. **Authority:** Xiaohei can be the selected assistant or a deliberately installed privileged/system component; locked OEM-only access is a stop.
6. **Runtime proof:** a real acoustic event reaches Android callback, then stop/unload and reboot rollback succeed.

Custom keyword generation is a seventh, independent gate. Passing a stock model does not prove the vendor exposes a legal and reproducible compiler/training path.

## Primary evidence

- [AOSP Sound Trigger architecture and vendor hidden-contract warning](https://source.android.com/docs/core/audio/sound-trigger)
- [AOSP Android manifest: `MANAGE_SOUND_TRIGGER` and hotword permissions](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-qpr2-release/core/res/AndroidManifest.xml)
- [LineageOS OnePlus SM8250 common proprietary files](https://github.com/LineageOS/android_device_oneplus_sm8250-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS OnePlus SM8350 common proprietary files](https://github.com/LineageOS/android_device_oneplus_sm8350-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Xiaomi SM8250 common proprietary files](https://github.com/LineageOS/android_device_xiaomi_sm8250-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Xiaomi SM8350 common proprietary files](https://github.com/LineageOS/android_device_xiaomi_sm8350-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Pixel 6/6 Pro (`raviole`) device tree](https://github.com/LineageOS/android_device_google_raviole/tree/lineage-23.2)
- [Samsung Bixby voice wake-up documentation](https://www.samsung.com/us/support/answer/ANS10001415/)
