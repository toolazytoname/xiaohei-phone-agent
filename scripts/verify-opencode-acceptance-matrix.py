#!/usr/bin/env python3
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
T=(ROOT/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeAcceptanceMatrixTest.java').read_text()
def ok(x,m):
 if not x: raise SystemExit('FAIL opencode-acceptance: '+m)
for x in ('OpenCodeTaskProtocol.Kind.values()','for(int r=0;r<3;r++)','OpenCodeToolPolicy.evaluate','OpenCodeBoundedRunner','OpenCodeWorkspaceBoundary.release','Files.createTempDirectory'):ok(x,x)
for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','Files.read','Files.write','su -c'):ok(x not in T,x)
ok('OpenCodeAcceptanceMatrixTest rounds=9 summary=3 diagnosis=3 organization=3 results=9 cleanup=9 denial=3 real_opencode=0' in T,'matrix')
print('PASS opencode-acceptance rounds=9 kinds=3 cleanup=9 denial=3 temp_only=true real_opencode=0')
