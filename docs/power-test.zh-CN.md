# 物理拔线功耗与待机流程

本流程在 USB 真正拔除后由手机自行采样。Android 电池模拟只允许验证 harness，绝不能作为验收证据。

1. 把小黑置于目标模式并记录：`cpu-off-dsp-off`、`cpu-off-dsp-armed` 或 `cpu-kws-on-dsp-off`。
2. USB 尚连接时启动监控。已经验收的 Android TLS 无线调试 transport 可以作为 `SERIAL`；不要为了本测试开启传统 TCP/5555：

   ```sh
   bash scripts/start-idle-monitor.sh --serial SERIAL --mode cpu-off-dsp-off --hours 8
   ```

3. 十分钟内物理拔掉 USB/电源。只有 Android 同时报告 AC、USB、无线供电均为 false、无活跃通话且屏幕不处于交互唤醒后才开始计时。原始 TSV 同时记录请求时间、实际开始采样时间和前置等待时长，不能把主机发起脚本的时间误当成静置开始。
4. 运行期间不要使用手机；到时重新插线并收集：

   ```sh
   XIAOHEI_IDLE_OUTPUT=/安全的本地路径/run.tsv \
     bash scripts/collect-idle-monitor.sh --serial SERIAL --mode cpu-off-dsp-off
   ```

5. 在起始电量、温度、网络、息屏和时长可比的条件下，OFF/ARMED A/B 至少重复三轮。若原始 TSV 暴露私人使用时间，应保存在公开仓库之外。

监控会采样电量/充电状态/设备允许读取时的电流、温度、thermal 状态、活跃录音、含 Xiaohei 名称的 wakelock、wakefulness、当前是否通话和是否重新接电；不保存号码、前台 App、通知或用户内容。启动后不需要 Mac，也不会为了测试保持 USB。验收要求出现 `# COMPLETE`、达到完整时间，且录音、小黑 wakelock、Awake、通话和接电样本均为 0；任一命中都会使该轮失效，不能靠平均数掩盖。设备隐藏电流传感器时必须如实标记。
