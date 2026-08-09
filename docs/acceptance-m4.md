# M4 notification and confirmed-draft acceptance

Date: 2026-08-09  
Device: OnePlus 8T / Android 14

## Real-device evidence

- With notification access revoked, “Does WeChat have unread messages?” returned “not authorized” and read nothing. After explicit test authorization, the assistant counted only current, non-ongoing notifications and named their source apps.
- With the screen off, the same query returned the lock-screen privacy denial. The gate binds the pre-launch screen/Keyguard state to the command session, so `showWhenLocked` cannot erase the privacy decision.
- No notification title, body, contact, or history is written to SharedPreferences or files. Summaries come from `getActiveNotifications()` and disappear with the notification.
- A test-only, clearly labelled local fixture posted one notification under the WeChat package ID because real WeChat and a test account are absent. “Reply on WeChat saying I will arrive later” displayed App, target “Test Contact”, and the complete draft. Cancel opened nothing.
- Confirming the visible preview opened only the fixture app. Xiaohei did not populate a field, invoke RemoteInput, click Send, or simulate Accessibility. When the notification was removed before confirmation, the fresh check cancelled the action.
- The fixture was uninstalled after the run; `com.tencent.mm` is absent again.
- The product now also treats notification-listener disconnect as a privacy event: it publishes only a package-local access boolean, never notification metadata, and clears any visible pending draft. Because some Android builds do not reliably deliver the listener-disconnect callback, the foreground draft view additionally polls only the access-granted boolean while a draft exists and clears within one second after revocation. This implementation passed compilation/static verification; a fresh end-to-end revoke run remains required below.

## Safety boundary

Notification access is optional and user-controlled. Locked sessions expose no notification content. Reply drafts are volatile, bound to the currently visible notification, and require a fresh visible confirmation. The current product never sends a message automatically.

## Open M4 exit gate

The fixture proves permission, privacy, confirmation, cancellation, and disappearing-notification behavior, but it is not a real isolated WeChat account. Final M4 acceptance still needs a non-primary test account on a supported messaging app, privacy-notification variants, access revocation while running, and confirmation that manual sending behaves as documented.
