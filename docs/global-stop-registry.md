# Global stop registry

`GlobalStopRegistry` is the local fan-out core for a visible global-stop request. Runtime owners must explicitly register one of eight closed categories: voice, DSP, CPU wake, conversation, Phone Agent, tool execution, OpenCode, or root.

On stop, every registered owner is called once. If all confirm stop, the result is `STOPPED`; if any return false or throw, the result is `STOP_FAILED` and `allResourcesReleased` remains false. The registry never discovers, launches, signals, or kills resources by name. It refuses late registration and repeat stop requests, preventing hidden retry loops.

The existing home button and its status-notification `global_stop` intent now use this registry for the three owners held by `MainActivity`: voice, DSP and CPU wake. Phone Agent, Conversation, OpenCode, tool execution, root, voice command and widget entry points remain independently owned, and the registry cannot prove platform resources reached zero. UX-005 remains `VERIFY` pending that integration and device evidence.
