#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootEncryptedBackup.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootEncryptedBackupTest.java').read_text();s=(r/'contracts/root-encrypted-backup.v1.schema.json').read_text()
if not all(x in j for x in ('AES/GCM/NoPadding','MAX_BYTES=16*1024','KEY_BYTES=32','IV_BYTES=12','new SecureRandom()')):raise SystemExit('FAIL root-backup: crypto')
if any(x in j for x in ('java.io.','java.nio.file','ProcessBuilder','Runtime.getRuntime','android.','Socket(','su ')):raise SystemExit('FAIL root-backup: persistence_or_execution')
if 'RootEncryptedBackupTest aes_gcm=roundtrip key=32 iv=12 wrong_key=deny tamper=deny disk_paths=0' not in t:raise SystemExit('FAIL root-backup: matrix')
if not all(x in s for x in ('"aes-256-gcm"','"iv_bytes":{"const":12}','"public_log_safe":{"const":false}')):raise SystemExit('FAIL root-backup: schema')
print('PASS root-encrypted-backup aes_256_gcm=true max_bytes=16384 disk_paths=0 execution=0')
