# Voice Gateway

Creates one bounded post-wake command session, runs VAD and a replaceable ASR adapter, then closes microphone access. It must not implement always-on CPU recording.

每次唤醒只创建一个有边界的命令会话，完成 VAD 与可替换 ASR 后关闭麦克风；禁止实现 CPU 常驻录音。
