#!/usr/bin/env python3
import json
from pathlib import Path
p=Path(__file__).resolve().parents[1]/'contracts/root-capability-catalog.v1.json'
d=json.loads(p.read_text())
required={'schema_version','public_log_safe','root_broker_implemented','allowlisted_future_action_ids','denied_surfaces','recovery_owner','recovery_requirements','notes'}
if set(d)!=required or d['schema_version']!=1 or d['root_broker_implemented'] is not False or d['public_log_safe'] is not True: raise SystemExit('FAIL root-catalog: shape')
deny={'generic_su','arbitrary_command','arbitrary_path','credential_store','payment_otp_password','destructive_git','system_partition_write','boot_image','network_exfiltration'}
if set(d['denied_surfaces'])!=deny or d['recovery_owner']!='human_device_owner': raise SystemExit('FAIL root-catalog: deny/recovery')
if any('su' in x or 'command' in x for x in d['allowlisted_future_action_ids']): raise SystemExit('FAIL root-catalog: generic authority')
print('PASS root-capability-catalog allow=3 deny=9 broker_implemented=false recovery_owner=human')
