#!/usr/bin/env python3
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=(ROOT/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/OpenCodeToolPolicy.java').read_text()
TEST=(ROOT/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeToolPolicyTest.java').read_text()
def ok(x,m):
 if not x: raise SystemExit('FAIL opencode-tool-policy: '+m)
for x in ('ALLOW_PROJECT_SUMMARY','ALLOW_TEST_DIAGNOSIS','ALLOW_CONTROLLED_ORGANIZATION','ROOT','SENSITIVE_PATH','DESTRUCTIVE_GIT','NETWORK','SHELL_ESCAPE'): ok(x in JAVA,x)
for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','Files.read','Files.write','su -c'): ok(x not in JAVA,'execution '+x)
ok('OpenCodeToolPolicyTest allow=3 root=5 sensitive=9 git=8 network=7 shell=6 unknown=3' in TEST,'matrix')
print('PASS opencode-tool-policy allow=3 deny=root+sensitive_path+destructive_git+network+shell execution=0')
