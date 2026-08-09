# M4 notification and confirmed-draft acceptance

Date: 2026-08-09  
Devices: OnePlus 8T / Android 14; independent Android 14 AOSP ARM64 device

## Real-device evidence

- With notification access revoked, “Does WeChat have unread messages?” returned “not authorized” and read nothing. After explicit test authorization, the assistant counted only current, non-ongoing notifications and named their source apps.
- With the screen off, the same query returned the lock-screen privacy denial. The gate binds the pre-launch screen/Keyguard state to the command session, so `showWhenLocked` cannot erase the privacy decision.
- No notification title, body, contact, or history is written to SharedPreferences or files. Summaries come from `getActiveNotifications()` and disappear with the notification.
- A test-only, clearly labelled local fixture posted one notification under the WeChat package ID because real WeChat and a test account are absent. “Reply on WeChat saying I will arrive later” displayed App, target “Test Contact”, and the complete draft. Cancel opened nothing.
- Confirming the visible preview opened only the fixture app. Xiaohei did not populate a field, invoke RemoteInput, click Send, or simulate Accessibility. When the notification was removed before confirmation, the fresh check cancelled the action.
- The fixture was uninstalled after the run; `com.tencent.mm` is absent again.
- The product now also treats notification-listener disconnect as a privacy event: it publishes only a package-local access boolean, never notification metadata, and clears any visible pending draft. Because some Android builds do not reliably deliver the listener-disconnect callback, the foreground draft view additionally polls only the access-granted boolean while a draft exists and clears within one second after revocation.
- The independent AOSP device completed that dynamic revoke path end to end. While a visible Messaging draft and confirmation button existed, notification-listener access was removed. Within the bounded foreground check, both draft and confirmation nodes became zero and the UI stated that access was revoked and the draft cleared. AOSP retained a stale Secure Settings component string after the real grant was removed; `accessGranted()` now uses Android's authoritative `NotificationManager.isNotificationListenerAccessGranted()` on API 27+, so status and privacy logic do not trust that stale string.
- The test fixture covered PUBLIC, PRIVATE, and SECRET notification variants. PUBLIC and PRIVATE showed the synthetic test target only while unlocked; SECRET displayed only “private conversation”. The fixture was removed afterward and is not counted as isolated-account evidence.
- For independent, non-primary messaging evidence, the AOSP emulator received a real emulated SMS in the stock `com.android.messaging` app under a synthetic telephony identity. Xiaohei now supports exactly allowlisted WeChat and AOSP Messaging notification adapters. “Reply to the message saying acknowledged” displayed the Messaging app, synthetic conversation target, and full draft. The confirmation was tapped while the exact notification key was still active; Xiaohei opened only the stock conversation list. The SMS sent-provider row count was zero before and after, and the UI showed no pre-filled composer or automatic send. No synthetic number, title, or message body matched any Xiaohei SharedPreferences/file. A screen-off command produced no draft.

## Safety boundary

Notification access is optional and user-controlled. Locked sessions expose no notification content. Reply drafts are volatile, bound to the currently visible notification, and require a fresh visible confirmation. The current product never sends a message automatically.

## M4 exit conclusion

The OnePlus and independent AOSP evidence covers optional permission, unread limitations, lock/screen-off privacy, PUBLIC/PRIVATE/SECRET variants, notification disappearance and exact-key freshness, revocation while a draft is visible, an isolated non-primary messaging identity, visible target/content confirmation, and manual-send behavior with zero sent messages. It does not claim unattended WeChat automation; the primary WeChat account remains outside test scope. Notification access was removed and the fixture/test messages were cleared after acceptance. M4 is complete.
