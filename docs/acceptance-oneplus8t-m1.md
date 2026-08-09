# OnePlus 8T M1 real-device acceptance record

Date: 2026-08-09  
Device: OnePlus 8T (KB2000 / OnePlus8T)  
OS: LineageOS 21 / Android 14  
Product: ordinary Xiaohei APK plus a unique-UID `system_ext` DSP Companion

## Passed

- Three cold boots after adopting Assistant Role: `sys.boot_completed=1`, role holder, Xiaohei VoiceInteraction/Recognition services, and `mBound=true` persisted every time.
- Three product-UI arm/disarm cycles: every arm reached `ACTIVE` and Qualcomm LPI; every stop/unload returned status 0 and middleware ended at `DETACH`.
- Screen-off acoustic “Xiaobu Xiaobu”, callback, 750 ms automatic re-arm, and a second callback passed with second-stage confidence 99.
- Offline Chinese ASR transcribed “打开相册”, launched the system Photo Picker, and returned to `ARMED`.
- AudioFlinger reported no active record clients after ASR; no active/loaded DSP model remained after disarm.
- Ordinary-app uninstall and reinstall from the same offline APK passed while the Companion remained `DETACHED`.
- Three continuous end-to-end runs passed from verified Dozing/display-off state through confidence-99 DSP detection, Assistant invocation, offline Chinese gallery transcription, Photo Picker action, automatic re-arm, and zero active record clients.

## Fixed during acceptance

Secure-setting-only Assistant selection, accidental use of a global recognizer, Activity-owned DSP lifetime, lock-screen background-service status reads, stale Role state during direct uninstall, and duplicate Assistant/maintenance tasks were replaced with RoleManager, an explicit app-owned recognizer, a signature-gated foreground DSP service, a read-only status provider, ordered rollback scripts, and a single-task main surface.

## M1 gates still open

- Physically unplugged DSP OFF/ARMED power A/B is still required.
- A 120 ms audible prompt is implemented and still needs one real-device listening check.
