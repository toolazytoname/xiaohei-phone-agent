# 有边界的纯内存 Conversation 会话

状态日期：2026-08-10。`CHAT-006` 为后续追问对话提供可复用的上下文边界；它本身不启用多轮 UI、语音、工具、规划或动作。

## 运行时契约

`MemoryConversationSession` 是与 `conversation-session.v1` 对齐的纯 Java、进程内对象：

- 1–8 个已完成的用户/助手 turn；
- 64–8192 个估算正文 token；
- 1 秒–15 分钟总生命周期；
- 同时最多一个在途 turn；
- `action_authority=none`，正文只在内存中处理。

Token 计数是保守、与供应商无关的护栏，不是计费 tokenizer：每条消息至少按 Unicode code point 数计费，同时至少按每四个 UTF-8 字节一个 token 计费。供应商侧 `max_tokens` 仍是独立的传输上限。

## 状态与清空规则

只有三项预算都允许时，`beginTurn` 才加入一条归一化用户消息；`completeTurn` 加入一条助手回复。请求失败时，`abortTurn` 删除尚未配对的用户消息，保证条件改变后的重试不会继承“幽灵 turn”。

出现以下任一情况，会话会关闭并释放自己持有的全部正文引用：

- 下一段文字会超过 token 预算；
- 达到配置的 turn 上限；
- 到达单调时钟总截止时间，或时钟倒退；
- 用户取消；
- 无效助手回复会让在途 turn 状态含糊。

调用方会得到明确原因。对已关闭会话的重复操作返回 `CLOSED`，但不会覆盖首次终止原因。可公开记录的状态只含计数、预算、生命周期标志和原因，绝不含消息正文。

Java 无法保证不可变 `String` 或调用方已持有副本的物理清零。因此这里的“清空”准确含义是：会话释放自己持有的引用，并且不存在恢复入口；它不是内存取证级清零声明。

## 进程重启不恢复

实现不引入 Android 持久化 API、数据库、文件序列化、Parcel 或日志器，也没有静态正文集合。新进程只能创建新的空实例；没有任何方法可以从存储接收或恢复对话正文。

## 验证

12 条确定性用例覆盖非法边界、有序历史、turn/输入 token/输出 token 上限、截止时间与时钟倒退、取消、失败请求回滚、并发 turn 拒绝、不可变请求视图、中文保守计数和新实例模拟重启。

运行：

```bash
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-conversation-memory-boundary.py
bash scripts/verify.sh
```

静态门禁还会读取 JSON 契约，比较六个运行时上下限常量，并拒绝持久化或正文日志路径。

## 当前集成

`CHAT-007` 已把不可变请求视图转换为 6 turn 半双工模型路径；它不持久化正文、不绕过终止结果，也不会在切换模型/profile、锁屏、退到后台、明确结束或超时后复用旧会话。参见[有边界的半双工 Conversation](conversation-half-duplex.zh-CN.md)。
