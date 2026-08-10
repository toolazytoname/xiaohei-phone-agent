# OpenCode Web takeover ownership

`OpenCodeTakeoverOwnership` models explicit control ownership between the local executor and a verified Web session. Taking over and returning only transfer control ownership; neither operation starts, resumes, duplicates, or changes a task.

Only an opaque, locally verified `web-…` session handle can request the transfer. A second takeover, a wrong return, or any request after terminal state is denied. The core does not implement a server, Web UI, transport, task content, filesystem access, or process execution.

OC-009 remains `VERIFY` until a real local Web handoff is wired with visible user control and independently shown not to duplicate an in-flight task.
