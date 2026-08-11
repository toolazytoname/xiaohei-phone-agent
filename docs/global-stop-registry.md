# Global stop registry

`GlobalStopRegistry` is the local fan-out core for a visible global-stop request. Runtime owners must explicitly register one of eight closed categories: voice, DSP, CPU wake, conversation, Phone Agent, tool execution, OpenCode, or root.

On stop, every registered owner is called once. If all confirm stop, the result is `STOPPED`; if any return false or throw, the result is `STOP_FAILED` and `allResourcesReleased` remains false. The registry never discovers, launches, signals, or kills resources by name. It refuses late registration and repeat stop requests, preventing hidden retry loops.

The existing home button and its status-notification `global_stop` intent use this registry for the three owners held by `MainActivity`: voice, DSP and CPU wake. Conversation registers its pending request/TTS owner; authorized tool execution registers its cancellation signal; and a Phone Agent task registers only while it is pending, then releases that handle on completion, stop, or Accessibility-service destruction. The Phone Agent owner delegates to its existing `stopInternal` path; it does not start or kill a service.

OpenCode, root, voice-command/widget entry points, and platform-level zero-resource proof remain independently open. UX-005 therefore remains `VERIFY`: source wiring is not device evidence, and a global-stop click cannot claim to have stopped an unregistered runtime.
