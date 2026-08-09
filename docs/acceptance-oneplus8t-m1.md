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

## Fixed during acceptance

Secure-setting-only Assistant selection, accidental use of a global recognizer, Activity-owned DSP lifetime, lock-screen background-service status reads, and stale Role state during direct uninstall were replaced with RoleManager, an explicit app-owned recognizer, a signature-gated foreground DSP service, a read-only status provider, and ordered rollback scripts.

## M1 gates still open

- Three continuous screen-off acoustic wake → offline ASR → Photo Picker runs are still required. Current evidence proves the acoustic and ASR halves, but not three repeated continuous runs.
- Physically unplugged DSP OFF/ARMED power A/B is still required.
- An audible prompt is not implemented yet.

