# Sensitive action permanent denial

[简体中文](sensitive-action-denial.zh-CN.md) · [authorization tiers](authorization-tiers.md) · [status](../STATUS.md)

`POLICY-004` is a local fail-closed corpus evaluated before semantic UI actions. It permanently denies any matching package name, visible text, or requested label involving payment or transfer, authentication secrets (including OTP/CVV/password), or evasion of verification, fraud/risk controls, and security protection. It recognizes Chinese and English variants.

This is a denial boundary, not an intent classifier: a non-match grants no permission and must still pass the tool catalog, tier policy, confirmation, and gateway. The policy has no model, UI, network, root, or execution path. New sensitive variants must extend the regression corpus rather than weaken the default.
