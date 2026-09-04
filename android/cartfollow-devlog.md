# Human Cart Simulator 开发进度记录

> 所属项目：自主跟随购物车原型
> 代码位置：`dev/OpenBot/android/robot/src/main/java/org/openbot/cartfollow/`
> 开发分支：`feature/human-cart-simulator`（Phase 1）/ `feature/distance-control`（Phase 2）/ `feature/person-crop-collector`（Phase 3 起）
> 最后更新：2026-07-13

---

## 1. 模块总览

Human Cart Simulator 是购物车跟随功能的上位机核心模块，在 OpenBot App 中新增一个功能页面（"Cart Simulator"），实现基于手机摄像头人物检测的跟随控制闭环。

### 文件清单

| 文件 | 作用 |
|------|------|
| `HumanCartSimulatorFragment.java` | 主 UI Fragment：摄像头预览、检测框绘制、目标确认、ReID 调度、行为决策 debug 与人类指令显示 |
| `FollowStateMachine.java` / `FollowState.java` | 管理初始化、确认、重捕获、跟随、谨慎跟随、身份不确定、搜索与停止 |
| `ActionArbitrator.java` / `BehaviorAction.java` / `BehaviorDecisionResult.java` | 阶段 A 行为层：将状态与证据映射为 `FOLLOW_SLOW / MOTION_STOP / LOCAL_SEARCH / BLOCKED_WAIT` 等动作 |
| `IdentityEvidence.java` / `DistanceEvidence.java` / `TraversabilityEvidence.java` / `SystemSafetyEvidence.java` | 统一证据结构，供 ActionArbitrator 和 debug 面板使用 |
| `ControlGenerator.java` | 控制算法：基于 DistanceState 决定 forward，转向由 xError 决定；当前仍只用于模拟提示，不直接控制底盘 |
| `DistanceState.java` / `ImageSetpointDistanceEstimator.java` | 初始化标定 + 图像伺服距离估计，输出 `TOO_FAR / OK / TOO_CLOSE / UNKNOWN` |
| `TargetMemory.java` | 目标记忆：confirmed bbox、颜色特征、距离 setpoint、previous/last bbox 和 ReID gallery |
| `TargetMatcher.java` | legacy 目标匹配：position + size + color + confidence，用于 ReID 不可用时的保守降级 |
| `ReIDFeatureExtractor.java` / `TfliteReIDFeatureExtractor.java` | ReID 抽象接口与 TFLite 推理实现，当前本地测试模型为 `osnet_x0_25_market1501.tflite` |
| `ReIDCoordinator.java` / `ReIDMatchResult.java` / `BboxContinuityEvidence.java` | 管理 ReID gallery、候选人推理、best/second/margin、bbox 连续性与推理耗时 |
| `HumanCommandInterpreter.java` | 将状态、距离和行为动作转换为 Human Cart Simulator 的中文动作提示 |
| `fragment_human_cart_simulator.xml` | 布局文件：OverlayView、指令文本、快照确认面板、倒计时、调试信息和底部面板 |

### 集成点（在 OpenBot App 中的入口）

| 文件 | 修改内容 |
|------|----------|
| `FeatureList.java` | 新增 `CART_SIMULATOR` 类别，显示在主菜单 |
| `MainFragment.java` | 添加 Cart Simulator 的导航路由 |
| `nav_graph.xml` | 注册 `cartSimFragment` 导航目标 |
| `strings.xml` | 新增 `cart_simulator` / `cart_sim_start` / `cart_sim_idle` 字符串 |

### Person Crop Collector（Phase 3 数据闭环入口）

`Person Crop Collector` 是 ReID 接入前的真实检测框数据采集工具页，代码位置为 `dev/OpenBot/android/robot/src/main/java/org/openbot/cropcollector/`。

| 文件 | 作用 |
|------|------|
| `PersonCropCollectorFragment.java` | 复用 OpenBot `Detector`，实时显示 person 检测框，并按 Person ID 启停采集 session |
| `PersonCropSession.java` | 为每次采集创建 `session_id`、`session_info.json`、`metadata.csv` 与 `crops/` 输出目录 |
| `PersonCropSaver.java` | 异步保存带 padding 的 person crop，按 `sensorOrientation` 旋转为正向图，并追加元数据 |
| `PersonCropCaptureConfig.java` | 管理采样间隔、置信度阈值、单人采集、padding、最大 crop 数和 JPEG 质量 |
| `fragment_person_crop_collector.xml` | 采集页 UI：模型选择、置信度、采样间隔、单人模式、Person ID、开始 / 停止按钮 |

当前输出路径位于 App 外部图片目录下的 `cartfollow_crops/<person_id>_<timestamp>/`，导出到 PC 后再由 `tools/reid_pc_test/prepare_openbot_crops_dataset.py` 整理为 `images_openbot_clean/`。

---

## 2. 当前实现状态

### 2.1 已完成（commit `dd6aa95` + `409d85f` + `4da208a` + Phase 2）

| 功能 | 状态 | 说明 |
|------|------|------|
| 人物检测（MobileNet-SSD） | 已完成 | 复用 OpenBot 现有 `Detector`，筛选 `classType="person"` |
| 两阶段目标初始化 | 已完成 | `CAPTURE_TARGET → LOCKED_PENDING_CONFIRM → CONFIRMED_ARMED`，采集时记录 confirmedBbox、面积、上下身颜色直方图、距离 setpoint，截图供用户确认 |
| 用户确认 / 重拍 / 取消 | 已完成 | 确认面板含快照预览与三按钮，状态切换正确 |
| 目标记忆 `TargetMemory` | 已完成 | 保存 confirmedBbox、confirmedArea、上下身 HSV 直方图、动态 lastBbox/lastCenter/lastArea/lastSeenTime、距离 setpoint（desiredHeightRatio/areaRatio/bottomRatio） |
| 目标匹配 `TargetMatcher` | 已完成 | position(0.40) + size(0.20) + color(0.30) + confidence(0.10) 融合评分，阈值 0.5；ReID 接口预留未接入 |
| 确认后重识别启动 | 已完成 | `CONFIRMED_ARMED → REACQUIRE_TARGET`，连续 `REACQUIRE_MATCH_N=8` 帧匹配后进入倒计时 |
| 倒计时启动 | 已完成 | `READY_TO_FOLLOW` 倒计时 3 秒后进入 FOLLOW |
| 完整状态机 `FollowStateMachine` | 已完成 | `IDLE → CAPTURE → CONFIRM → REACQUIRE → COUNTDOWN → FOLLOW → LOST → SEARCH → STOP` 全链路 |
| `LOST → SEARCH → STOP` 执行逻辑 | 已完成 | 连续 `FOLLOW_LOST_M=10` 帧未匹配进入 LOST；LOST 持续 `LOST_TO_SEARCH_MS=800ms` 进入 SEARCH；SEARCH 超时 `SEARCH_TIMEOUT_MS=5000ms` 进入 STOP；期间重新匹配则回 FOLLOW |
| **初始化距离标定 + 图像伺服** | 已完成（Phase 2） | `ImageSetpointDistanceEstimator` 基于采集时记录的 setpoint 输出 `height_scale / area_scale / bottom_shift`，无需恢复真实米制距离 |
| **DistanceState 输出** | 已完成（Phase 2） | 输出 `TOO_FAR / OK / TOO_CLOSE / UNKNOWN`，替代线性 distError；UNKNOWN 时停车 |
| **ControlGenerator 基于 DistanceState** | 已完成（Phase 2） | forward 由 DistanceState 决定：TOO_FAR→MAX_FORWARD，其余→0；移除硬编码 TARGET_H_RATIO/K_DIST/TOO_CLOSE_H_RATIO |
| **距离调试显示** | 已完成（Phase 2） | Simulator 显示 `dist / hScale / aScale / bShift / distConf` |
| **距离感知指令** | 已完成（Phase 2） | `HumanCommandInterpreter` 新增 DistanceState 重载：OK→"保持距离"、UNKNOWN→"距离不明，请停止" |
| 转向方向修正 | 已完成 | `FLIP_TURN=true`，commit `409d85f` 修正 |
| UI 模式切换 | 已完成 | 开关控制检测启停，启动后锁定模型选择 |
| 置信度调节 | 已完成 | +/- 按钮以 5% 步进调节，范围 5%-95% |
| 模型选择 | 已完成 | 支持本地和 URL 模型，修复了 URL 模型的错误提示 |
| 检测框可视化 | 已完成 | 绿色目标框 / 黄色候选框 / 白色普通行人框 / 红色匹配失败框 |
| 快照确认面板 | 已完成 | LOCKED_PENDING_CONFIRM 时显示候选目标截图 + 确认/重拍/取消 |
| 倒计时显示 | 已完成 | READY_TO_FOLLOW 时显示剩余秒数 |
| 调试信息面板 | 已完成 | 显示 state / forward / turn / left / right / persons / fps / dist / hScale / aScale / bShift / distConf |
| 导航集成 | 已完成 | 已注册到主菜单 "Cart Simulator" 入口 |
| **Person Crop Collector** | 已完成（Phase 3 前置） | 已注册到主菜单，可采集真实 OpenBot person bbox crop、`session_info.json` 与 `metadata.csv` |
| **真实 crop 数据 PC 端 ReID 复测** | 已完成首轮 | 基于 `images_openbot_clean`、`osnet_x0_25_market1501.pth`、`diverse gallery` 完成 pairwise / gallery-probe / target-follow 模拟 |
| **Person Sequence Collector** | 已完成 | 可采集无人帧、多人检测、bbox、crop 和人工事件，用于 PC sequence replay |
| **阶段 A 行为层** | 已完成并通过手机体验 | `Evidence -> BehaviorDecisionResult -> BehaviorAction -> HumanCommand` 已接入 Human Cart Simulator |
| **阶段 B Android ReID** | 已完成首版并通过手机运行 | TFLite ReID 可运行，debug 字段正常，实机约 30 FPS；仍需阶段 C 轨迹与身份信念层抑制跟错人 |
| **阶段 C 目标轨迹与身份信念层** | 已完成策略修正版代码接入 | 已包含短时 trackId、lockedTrackId、targetBelief、suspectedTrack、locked ghost、suspected 滞回、loose/default/strict bbox gate、恢复后 relock 与非 locked 空间支持门控，待安装新版 APK 后复测 |
| **诊断日志开关** | 已完成 | Human Cart Simulator 新增“记录日志”开关，默认关闭；关闭时不创建 diagnostics session，不写 CSV/JSON/crop/gallery/event |

### 2.2 Phase 3 首轮 ReID 实验结果（2026-07-06）

数据集：`tools/reid_pc_test/images_openbot_clean`，共 3 个身份、209 张真实 OpenBot 检测框 crop。

模型：`osnet_x0_25` + `weights/osnet_x0_25_market1501.pth`，CPU 推理，embedding 维度 512。

关键结论：

- Pairwise：同一人均值 `0.709`，不同人均值 `0.620`，均值差距 `0.089`，Top-1 最近邻身份正确率 `0.990`。
- Gallery-Probe：`gallery-k=8 + diverse` 强制识别准确率 `0.876`，优于 `gallery-k=5` 的 `0.840`。
- Target-follow 模拟：`gallery-k=8` 时目标存在场景强制选择目标准确率 `0.843`；`margin>=0.05` 时 accepted accuracy `0.957`，`margin>=0.08` 时 `0.986`。
- 目标缺席场景风险仍高：`gallery-k=8` 下 `margin>=0.05` 的 false accept rate 仍为 `0.457`，`margin>=0.10` 仍为 `0.184`。

工程判断：

```text
当前 ReID 主线暂定为 osnet_x0_25 + diverse confirmedGallery(k=8)。
ReID margin 可作为身份置信证据，但不能单独恢复 FOLLOW。
后续必须与位置连续性、bbox 尺寸、运动趋势、连续多帧稳定性和状态机融合。
```

### 2.3 核心控制算法（Phase 2 后）

```
输入：匹配目标 bbox + 画面尺寸 + 传感器角度 + TargetMemory(setpoint)
输出：Control(left, right) + DistanceEstimate

距离估计（ImageSetpointDistanceEstimator）：
  1. 处理 sensorOrientation 旋转
  2. currentHeightRatio = boxHeight / imgHeight
     currentAreaRatio   = boxArea / (imgW * imgH)
     currentBottomRatio = boxBottom / imgHeight
  3. heightScale = currentHeightRatio / desiredHeightRatio
     areaScale   = sqrt(currentAreaRatio / desiredAreaRatio)
     bottomShift = currentBottomRatio - desiredBottomRatio
  4. 校验：bbox 过小 / height_scale 与 area_scale 对数差异过大 → UNKNOWN
  5. 判态：heightScale < 0.85 → TOO_FAR
          heightScale > 1.15 → TOO_CLOSE
          否则                → OK

控制生成（ControlGenerator）：
  xError = target_centerX / imgWidth - 0.5
  turn = K_TURN × xError × (FLIP_TURN ? -1 : 1)
  forward =
    TOO_FAR  → MAX_FORWARD
    OK       → 0
    TOO_CLOSE→ 0  （首版不主动后退）
    UNKNOWN  → 0  （不确定就停）
  left = forward - turn, right = forward + turn
```

当前可调参数（`ControlGenerator`）：

| 参数 | 默认值 | 含义 |
|------|--------|------|
| `K_TURN` | 1.5 | 转向灵敏度 |
| `MAX_FORWARD` | 0.6 | TOO_FAR 时的固定前进速度 |
| `MIN_CONFIDENCE` | 0.5 | 最小检测置信度 |
| `FLIP_TURN` | true | 转向方向翻转 |

距离估计参数（`ImageSetpointDistanceEstimator`）：

| 参数 | 默认值 | 含义 |
|------|--------|------|
| `FAR_THRESHOLD` | 0.85 | heightScale 低于此值判定 TOO_FAR |
| `CLOSE_THRESHOLD` | 1.15 | heightScale 高于此值判定 TOO_CLOSE |
| `UNKNOWN_HEIGHT_DISAGREE` | 0.3 | height/area 对数差异上限 |
| `MIN_BBOX_HEIGHT_RATIO` | 0.1 | bbox 高度占比下限 |

当前状态机参数（`FollowStateMachine`）：

| 参数 | 默认值 | 含义 |
|------|--------|------|
| `CAPTURE_FRAMES` | 15 | 采集帧数阈值 |
| `REACQUIRE_MATCH_N` | 8 | 重识别连续匹配帧数 |
| `FOLLOW_LOST_M` | 10 | FOLLOW 连续未匹配进入 LOST 的帧数 |
| `LOST_TO_SEARCH_MS` | 800 | LOST 进入 SEARCH 的延时 |
| `SEARCH_TIMEOUT_MS` | 5000 | SEARCH 超时进入 STOP |
| `COUNTDOWN_MS` | 3000 | 倒计时时长 |

---

## 3. 尚未实现（待开发）

### 3.1 关键缺失

| 功能 | 优先级 | 说明 |
|------|--------|------|
| **近场传感器日志与固件标定** | 高 | Android 已取消来源不明最小值的运动门控；ESP32 仍需按“仅日志模式”说明修改、烧录并悬空验收 |
| **真实车急转弯与拐角恢复** | 中 | 首版只允许两轮同向缓弯，偏差过大时停车，不执行原地旋转搜索 |
| **参数持久化** | 低 | 当前调参仅内存生效，重启恢复默认 |
| **参数 UI 面板** | 低 | K_TURN / MAX_FORWARD / 阈值等参数需通过代码修改，没有 UI 界面 |
| **bottomShift 参与判态** | 低 | 当前 bottomShift 仅用于显示，未参与距离状态判断。待 90° 旋转下方向实测验证后决定是否纳入 |

### 3.2 状态机（已完整实现）

```
IDLE ──(启动开关)──→ CAPTURE_TARGET ──(采集N帧)──→ LOCKED_PENDING_CONFIRM
                                                          │
                                              确认 / 重拍 / 取消
                                                          │
CONFIRMED_ARMED ──(检测到人)──→ REACQUIRE_TARGET ──(连续N帧匹配)──→ READY_TO_FOLLOW
                                                                        │
                                                                  倒计时3秒
                                                                        ↓
                                                                  FOLLOW
                                                                    │
                                                            连续M帧未匹配
                                                                    ↓
                                                                   LOST ──(800ms)──→ SEARCH ──(5s超时)──→ STOP
                                                                    │                  │
                                                                    └──(重新匹配)──────┴──→ FOLLOW

STOP ──(用户重新开始)──→ CAPTURE_TARGET
```

### 3.3 已知代码问题

| 问题 | 说明 |
|------|------|
| 目标选择策略仍依赖位置+颜色 | TargetMatcher 未接入 ReID，多人长时间交叉下可能误锁。阶段3 处理 |
| forward 限幅非对称 | forward 只允许 ≥0，不允许后退。安全优先，合理 |
| bottomShift 旋转方向待验证 | 90° 旋转下 boxBottom 映射方向需实测确认，当前仅显示不参与判态 |

---

## 4. 与下位机的接口

| 方向 | 协议 | 说明 |
|------|------|------|
| 上位机→下位机 | `c<left>,<right>` | 由 `Vehicle.sendControl()` 发送，范围 [-255,255] |
| 心跳 | `h<interval_ms>` | 由 `Vehicle` 自动管理 |

**当前状态：** Human Cart Simulator 仍只显示模拟提示；独立的 Real Cart Follow 页面已经通过 `vehicle.setControl()` 接入 BLE 底盘，并在最终输出前增加真实车安全门和自动输出整形。

---

## 5. 后续开发计划

> 阶段划分对齐 `design/自主跟随购物车上位机软件开发计划.md` 与 `design/上位机软件开发 Phase 2——修正跟随距离控制计划书.md`

### Phase 1：状态机与目标初始化闭环（已完成）

- [x] 完整状态机 `IDLE → CAPTURE → CONFIRM → REACQUIRE → COUNTDOWN → FOLLOW → LOST → SEARCH → STOP`
- [x] 两阶段目标初始化 + 用户确认 + 重识别启动
- [x] TargetMemory / TargetMatcher / ControlGenerator 接入
- [x] Human Cart Simulator 实时提示与调试显示
- [ ] 多人干扰不切换目标（部分保证，待 ReID 增强）

### Phase 2：修正跟随距离控制（已完成）

- [x] 新增 `DistanceState` 枚举（`TOO_FAR / OK / TOO_CLOSE / UNKNOWN`）
- [x] 新增 `ImageSetpointDistanceEstimator`，输出 height_scale / area_scale / bottom_shift / state / confidence
- [x] `TargetMemory` 采集时记录 `desired_bbox_height_ratio / area_ratio / bottom_ratio`
- [x] 重构 `ControlGenerator`，forward 由 DistanceState 决定，移除硬编码 setpoint
- [x] `FollowStateMachine.FrameResult` 透传 distanceEstimate
- [x] Human Cart Simulator 显示 dist_state / hScale / aScale / bShift / distConf
- [x] `HumanCommandInterpreter` 纳入 distance state
- [x] 0.8-1.2 m 目标距离标定验证（初步测试基本通过，大多数情况下可以把距离保持为初始化时的距离。bShift在人远离的时候会向更负的方向变化，符合预期。）

**不在 Phase 2 范围**：`vehicle.setControl()` 接通（阶段6）、ReID 增强（阶段3）、障碍处理（阶段5）

### Phase 3：真实检测框数据闭环 + ReID

- [x] 新增 Person Crop Collector，从 OpenBot Android 导出真实 person bbox crop
- [x] PC 端验证 confirmedGallery / reid_score / reid_margin（首轮基于 `osnet_x0_25 + diverse gallery-k=8`）
- [x] Android 端部署 `osnet_x0_25` TFLite 首版，Human Cart Simulator 中 `reidAvailable=true`
- [x] 将 ReID 输出接入 `IdentityEvidence / FollowStateMachine / ActionArbitrator`
- [x] 多人、目标离开、目标返回、遮挡场景完成首轮手机观察
- [ ] 用阶段 C 的 track/belief 层继续降低跟错人风险，提高目标返回后的恢复速度

### Phase 4：目标轨迹与身份信念层（当前下一步）

- [x] 新增轻量 `TargetTrackManager`，用 bbox IoU / center distance / area ratio 维护短时 trackId
- [x] 新增 `IdentityBeliefAccumulator`，对每个 track 累计 targetBelief
- [x] locked target 不因干扰者单帧 ReID 高分被抢走，状态机恢复改为 belief 优先
- [x] 目标返回后通过 suspected track + 多帧 belief 稳定恢复到 `REACQUIRE_TARGET / FOLLOW_CAUTION`
- [x] debug 面板显示 `trackId / lockedTrackId / targetBelief / trackAge / missedFrames / beliefReason`
- [ ] 手机实测验收：目标离开、干扰者进入、目标返回、目标在场干扰者穿越、遮挡

### Phase 5：距离控制继续收敛

- [x] 初始化距离标定 + 图像伺服首版已完成
- [ ] 结合阶段 C 的稳定目标 track 重新验证 `TOO_FAR / OK / TOO_CLOSE`
- [ ] 评估 bottomShift 是否纳入距离状态判断

### Phase 6：局部可通行空间与跟随式避障

- [ ] LEFT / CENTER / RIGHT 三方向 free score
- [ ] 候选动作 SLOW_FORWARD / LEFT_ARC / RIGHT_ARC / BLOCKED_WAIT

### Phase 7：性能评估与增强开关

- [ ] 记录 detector、ReID、track/belief、ActionArbitrator 耗时
- [ ] 根据手机性能决定是否提高 ReID 频率
- [ ] 评估 MiDaS / Depth Anything Android 部署，只作为距离/障碍风险增强

### Phase 8：硬件联调

- [x] Real Cart Follow 通过 `vehicle.setControl()` 接通 BLE 底盘
- [x] 手动低速、BLE 保鲜、换向停稳与急停完成首轮联调
- [ ] 自动直行/同向差速缓弯真车验收
- [ ] 更大转向半径和控制延迟标定
- [ ] ToF / 超声波安全冗余

---

## 6. 提交历史

| Commit | 日期 | 说明 |
|--------|------|------|
| `dd6aa95` | 2026-07 | Add Human Cart Simulator for shopping cart follow debugging |
| `409d85f` | 2026-07 | Fix turn direction, confidence button height, and model selection |
| `4da208a` | 2026-07-02 | Add two-stage target init, target memory and full follow state machine |
| `173ef96` | 2026-07-06 | Add DistanceState and ImageSetpointDistanceEstimator for image-based visual servoing |
| `66bbf12` | 2026-07-06 | Calibrate distance setpoint in TargetMemory and refactor ControlGenerator to DistanceState |
| `c880025` | 2026-07-06 | Display distance state and scales in Human Cart Simulator |
| `80fa505` | 2026-07-06 | Add Person Crop Collector entry skeleton |
| `290282c` | 2026-07-06 | Show person detections in crop collector |
| `9e3c7c4` | 2026-07-06 | Save detected person crops with metadata |
| `6d9aa5f` | 2026-07-06 | Add capture controls and status panel |
| `765eb82` | 2026-07-06 | Put Person ID input on its own row for tap accessibility |
| `771345e` | 2026-07-06 | Rotate crop by sensorOrientation before saving to disk |
| recorded | 2026-07-07 | Add PersonSequenceCollector for continuous sequence data collection |
| recorded | 2026-07-08 | Add phase A behavior decision layer and Human Cart Simulator action debug |
| recorded | 2026-07-08 | Add phase B TFLite ReID evidence path for Human Cart Simulator |
| pending | 2026-07-08 | Add phase C TargetTrack and IdentityBelief layer |

---

## 7. Person Sequence Collector（Phase 3 时序数据采集）

> 更新日期：2026-07-07  
> 代码位置：`dev/OpenBot/android/robot/src/main/java/org/openbot/sequencecollector/`  
> 目的：为 PC 端 chronological replay / 状态机回放采集连续时序事实数据。  
> 当前状态：已实现、已构建通过、已安装到手机，并完成两条真实 sequence 采集。

### 7.1 模块定位

`PersonSequenceCollector` 是独立于 `PersonCropCollector` 的采集工具。它不做 ReID 推理，不控制小车，不写入 `FOLLOW / LOST / REACQUIRE / STOP` 等状态标签，只记录摄像头检测到的事实：

```text
每个采样帧是否有人；
每个采样帧有几个人；
每个检测框的 bbox / confidence / crop_path；
人工标记的 target_left / target_return / occlusion / distractor 事件。
```

这样 PC 端可以用同一份时序数据复现目标离开、遮挡、返回、干扰者进入等场景，而不是继续依赖随机抽样 rows。

### 7.2 新增文件

| 文件 | 作用 |
|------|------|
| `sequencecollector/PersonSequenceCollectorFragment.java` | 独立 CameraFragment 页面，复用 OpenBot Detector 检测 person，写入连续时序日志。 |
| `sequencecollector/PersonSequenceCaptureConfig.java` | 管理 frame log / crop / overlay 采样间隔、置信度、是否保存 crop 等配置。 |
| `sequencecollector/PersonSequenceSession.java` | 创建 `cartfollow_sequences/<session_id>/`，初始化 CSV 和 `session_info.json`。 |
| `sequencecollector/PersonSequenceSaver.java` | 单线程异步写 `frame_log.csv`、`detections.csv`、`events.csv` 和可选 crops。 |
| `res/layout/fragment_person_sequence_collector.xml` | Sequence 采集 UI。 |

入口集成：

| 文件 | 修改内容 |
|------|----------|
| `FeatureList.java` | 新增 `PERSON_SEQUENCE_COLLECTOR` 主菜单项。 |
| `MainFragment.java` | 新增跳转到 `personSequenceCollectorFragment`。 |
| `nav_graph.xml` | 注册 `personSequenceCollectorFragment`。 |
| `strings.xml` | 新增 Sequence Collector 标题、Start/Stop、idle 文案。 |

### 7.3 输出目录与文件

输出目录位于 App 外部图片目录：

```text
/sdcard/Android/data/org.openbot/files/Pictures/cartfollow_sequences/
└── <person_id>_seq_<yyyyMMdd_HHmmss>/
    ├── frame_log.csv
    ├── detections.csv
    ├── events.csv
    ├── session_info.json
    ├── crops/
    └── overlays/
```

CSV 字段：

```text
frame_log.csv:
session_id,frame_id,timestamp_ms,elapsed_ms,image_width,image_height,num_persons,raw_frame_path,overlay_path,event_tag,note

detections.csv:
session_id,frame_id,det_id,timestamp_ms,confidence,bbox_left,bbox_top,bbox_right,bbox_bottom,bbox_width,bbox_height,bbox_area_ratio,center_x,center_y,edge_touch,crop_path

events.csv:
session_id,timestamp_ms,frame_id,event_type,note
```

当前默认参数：

| 参数 | 默认值 |
|------|--------|
| `frameLogIntervalMs` | 200 ms |
| `cropIntervalMs` | 500 ms |
| `overlayIntervalMs` | 1000 ms |
| `minConfidence` | 0.5 |
| `saveCrops` | true |
| `saveOverlays` | false |
| `jpegQuality` | 90 |

说明：`frameLogIntervalMs` 与 `cropIntervalMs` 可在页面中通过 +/- 控件调整。第二条 sequence `yrc2_seq_20260707_152237` 实测使用 `cropIntervalMs=300 ms`，用于提高 PC 端 ReID replay 的帧密度。

### 7.4 当前验证状态

已完成静态构建验证：

```powershell
$env:JAVA_HOME='D:\Java\jdk-17'
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过。默认 JDK 24 会触发 Android Gradle `jlink` 兼容问题，需使用本机 `D:\Java\jdk-17` 构建。

已完成真机验证：

```text
主菜单能看到 Person Sequence Collector；
进入页面后能显示 person bbox；
Start 后创建 cartfollow_sequences/<session_id>/；
无人帧写入 frame_log.csv，num_persons=0；
多人帧在 detections.csv 写多行；
事件按钮能追加 events.csv；
Stop 后显示 frames / detections / crops / events 和导出路径；
adb pull 后 PC sequence replay 可以读取并扩展使用。
```

已采集数据：

| sequence | 说明 | PC 侧结论 |
|----------|------|-----------|
| `yrc_seq_20260707_140056` | 首条真实 sequence，包含目标离开、返回、干扰者、遮挡事件。 | 安全性成立，未出现错误恢复 FOLLOW；高 over-stop 主要来自终态 STOP 后尾段。 |
| `yrc2_seq_20260707_152237` | 更结构化 sequence：正常跟随、目标离开、无人帧、返回、干扰者进入/离开、遮挡。 | 暴露“看到了目标但恢复条件太严”的问题；宽松恢复条件可避免 STOP，并保持 `wrong_recovery_count=0`。 |

### 7.5 对 FollowStateMachine 的最新启发

第二条 sequence 表明：目标返回后，系统经常能看到连续稳定 bbox 和中等偏高 ReID 分数，但如果恢复条件只接受很强的 `strong + strict` 连续证据，就会长期卡在 `IDENTITY_UNCERTAIN`，最后超时进入 `STOP`。

后续 Android 状态机不应把 `STOP` 当成唯一安全动作，而应区分：

```text
motion_stop:
  线速度为 0，不继续前进，但仍观察、原地搜索、尝试重捕获。

hard STOP:
  搜索失败、风险过高或安全异常后的终态停车，等待人工重新开始。
```

建议新增或细化状态：

```text
FOLLOW
FOLLOW_CAUTION
IDENTITY_UNCERTAIN
LOCAL_SEARCH
REACQUIRE_TARGET
STOP
```

目标丢失时应先进入 `motion_stop + LOCAL_SEARCH`，根据最后 bbox 方向做原地低速搜索；目标返回后若连续多帧满足 `ReID + bbox + prediction` 稳定证据，再进入 `REACQUIRE_TARGET`，最后恢复 `FOLLOW`。只有搜索超时、干扰风险过高、障碍/急停/通信异常时才进入 hard `STOP`。

---

## 8. 调试提示

- 使用 `/dev/OpenBot/android` 在 Android Studio 中打开工程
- 主菜单 → "Cart Simulator" 进入本模块
- 打开 Start 开关开始检测，关闭开关回到 IDLE
- 调试信息面板显示实时 state / forward / turn / persons / fps
- 中文指令文本仅供调试参考，实际不会发送给底盘
- 主菜单 → "Person Crop Collector" 进入 ReID 数据采集页
- 输入 Person ID，保持 `Single Only` 打开，点击 Start Session 后采集真实 person crop
- 采集目录导出到 PC 后，用 `tools/reid_pc_test/prepare_openbot_crops_dataset.py` 整理为 `images_openbot_clean/`
- 主菜单 → "Person Sequence Collector" 进入连续时序采集页
- Sequence 采集时事件按钮只需在事件开始/结束时各按一次，PC replay 会用容忍窗口处理人工反应延迟

---

## 9. Phase B：ReID 身份证据接入与安全重捕获闭环（2026-07-08）

本阶段根据 Human Cart Simulator 阶段 A 的手机验收反馈推进：阶段 A 的行为层基本可用，但目标重新进入、干扰者进入等场景仍可能因为旧 `TargetMatcher` 单帧匹配而出现“跟错人”。因此 Phase B 的首要目标是切断 `LOST / SEARCH -> FOLLOW` 的单帧恢复路径，并把 ReID 作为身份置信度证据接入状态机。

已完成代码改动：

- 新增 `ReIDMatchResult`、`BboxContinuityEvidence`、`TfliteReIDFeatureExtractor`、`ReIDCoordinator`。
- `FollowState` 新增 `FOLLOW_CAUTION` 与 `IDENTITY_UNCERTAIN`。
- `TargetMemory` 增加 previous bbox 记录，用于 bbox 连续性和简单 prediction 计算。
- `IdentityEvidence` 扩展为同时携带 legacy score、ReID score / margin、bbox gate、稳定帧数和候选切换次数。
- `FollowStateMachine` 改为支持外部 `IdentityEvidence` 输入；`LOST / SEARCH` 不再允许单帧匹配直接恢复 `FOLLOW`，而是先进入 `REACQUIRE_TARGET`，再经多帧稳定证据恢复。
- `ActionArbitrator` 增加 `IDENTITY_UNCERTAIN` 和 `FOLLOW_CAUTION` 的动作解释。
- `HumanCartSimulatorFragment` 接入 `ReIDCoordinator`，在 debug 面板显示 `reidAvailable / gallerySize / bestScore / secondScore / margin / weak-mid-strong / bboxDefault-bboxStrict-prediction / stableMatchCount / candidateSwitchCount / reidLatencyMs / reidReason`。

TFLite 路线说明：

- 首版复用当前工程已有 TensorFlow Lite 2.4，不新增 ONNX Runtime 依赖。
- 默认模型路径为 `assets/networks/reid/osnet_x0_25_market1501.tflite`。
- 该模型文件属于本地测试资产，`.gitignore` 已忽略 `*.tflite`，默认不提交。
- 如果模型不存在或加载失败，App 不崩溃，debug 显示 `reidAvailable=false`，状态机退回更保守的 bbox / color / motion 逻辑。

构建验证：

```powershell
$env:JAVA_HOME='D:\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过，生成 `robot/build/outputs/apk/debug/robot-debug.apk`。当前本机输出过 Android SDK XML version warning，但未影响构建结果。

后续手机验收重点：

- 无 ReID 模型时：Human Cart Simulator 应正常打开，debug 显示 `reidAvailable=false`，目标丢失后不应单帧恢复 `FOLLOW`。
- 放入 TFLite 模型后：确认目标后 `gallerySize` 应逐步达到 8 或实际可用数量；多人、目标离开、目标返回、遮挡场景下观察 `bestScore / margin / stableMatchCount` 是否符合预期。
- 目标返回时允许 `IDENTITY_UNCERTAIN -> REACQUIRE_TARGET -> FOLLOW`，但不允许单帧高分直接恢复 `FOLLOW`。

---

## 10. Phase B 实机验收更新：ReID 已跑通，但需要 TargetTrack / IdentityBelief（2026-07-08）

### 10.1 当前实机状态

最新 APK 已安装到手机，Human Cart Simulator 中 ReID 首版链路已经成功运行：

- TFLite 模型路径：`assets/networks/reid/osnet_x0_25_market1501.tflite`
- 模型输入：`[1,3,256,128]`
- 模型输出：`[1,512]`
- debug 面板显示的 ReID 字段基本正常
- 实机帧率约 30 FPS，首轮性能可接受，后续如有必要仍可继续压榨调度策略

当前结论：ReID Android 接入已经从“能否加载/能否推理”进入“如何安全使用推理结果”的阶段。

### 10.2 当前体验问题

手机实测仍暴露两个关键问题：

1. 目标离开画面或多人接近时，虽然统计上比纯 bbox / color 匹配更安全，但仍可能识别错目标并跟着别人走。
2. 目标重新回到画面后，重捕获有时偏慢，甚至无法及时确认。

这说明当前 `ReIDMatchResult + bbox gate + stable frame` 仍偏“单帧候选驱动”。ReID 分数和 margin 有帮助，但不能直接等价于目标身份。

### 10.3 下一步代码方向

下一步建议在 `ReIDCoordinator` 与 `FollowStateMachine` 之间新增轻量轨迹和身份信念层：

```text
Detector persons
  -> TargetTrackManager
  -> IdentityBeliefAccumulator
  -> FollowStateMachine
  -> ActionArbitrator
```

建议新增或细化类型：

- `TargetTrack`
  - `trackId`
  - `lastBbox`
  - `previousBbox`
  - `ageFrames`
  - `missedFrames`
  - `lastSeenTimestampMs`
  - `isNearPredictionRegion`

- `TargetTrackManager`
  - 用 bbox IoU、center distance、area ratio 做短时 person bbox 关联。
  - 只维护最近几秒的轻量 track，不做复杂多目标跟踪。
  - 输出当前候选 tracks，供 ReID 和状态机使用。

- `IdentityBelief`
  - `trackId`
  - `targetBelief`
  - `reidContribution`
  - `bboxContribution`
  - `predictionContribution`
  - `switchPenalty`
  - `stableFrames`
  - `beliefReason`

- `IdentityBeliefAccumulator`
  - 对每个 track 累计身份信念。
  - ReID strong/mid、bbox 连续、prediction 命中时加分。
  - 候选切换、bbox 跳变、面积突变、margin 低、多人与目标混淆时扣分。

### 10.4 状态机接入原则

后续恢复跟随不应再由“单帧候选高分”触发，而应由“稳定 track + 稳定 belief”触发：

```text
IDENTITY_UNCERTAIN / SEARCH
  -> 疑似目标 track 连续稳定
  -> REACQUIRE_TARGET
  -> FOLLOW_CAUTION
  -> FOLLOW_CONFIDENT
```

安全边界保持不变：

- 身份不确定时线速度为 0。
- 搜索阶段可以更积极地原地扫描和提高 ReID 频率，但不能向前跟随。
- 已锁定目标时，干扰者一帧 ReID 高分不能直接抢走目标。
- hard `STOP` 仍只作为搜索失败、风险过高或安全异常后的兜底终态。

### 10.5 下一轮手机验收新增观察项

debug 面板建议新增：

```text
trackId
trackAge
missedFrames
targetBelief
beliefReason
activeTrackCount
lockedTrackId
```

验收场景：

1. 单人正常跟随：trackId 应稳定，targetBelief 应逐步升高。
2. 目标离开：进入 motion_stop / search，lockedTrack 不应被立即替换。
3. 干扰者进入：干扰者可形成新 track，但 targetBelief 不应快速超过恢复阈值。
4. 目标返回：先成为疑似目标，连续稳定后恢复 `REACQUIRE_TARGET -> FOLLOW_CAUTION / FOLLOW_CONFIDENT`。
5. 目标在场且干扰者穿越：lockedTrack 应尽量保持，必要时进入 `FOLLOW_CAUTION / IDENTITY_UNCERTAIN`，不冒进切换。

---

## 11. Phase C：目标轨迹与身份信念层首版实现（2026-07-08）

### 11.1 新增代码

| 文件 | 作用 |
|------|------|
| `TargetTrack.java` | 记录短时 track 的 `trackId / lastBbox / previousBbox / ageFrames / missedFrames / stableFrames`。 |
| `TargetTrackManager.java` | 用 bbox IoU、中心距离、面积比例将连续检测框关联为 track，并维护 `lockedTrackId / suspectedTrackId`。 |
| `IdentityBelief.java` | 定义 `BELIEF_CONFIRM=0.75 / BELIEF_CAUTION=0.55 / BELIEF_LOST=0.30` 和 belief debug 字段。 |
| `IdentityBeliefAccumulator.java` | 融合 ReID、bbox continuity、prediction、locked target、track age、candidate switch 和 missed frame，输出带 belief 的 `IdentityEvidence`。 |

### 11.2 接入点

- `HumanCartSimulatorFragment` 每帧检测后先调用 `TargetTrackManager.update()`。
- ReID 单帧输出不再直接交给状态机，而是先通过 `IdentityBeliefAccumulator.update()` 转换为累计身份信念。
- 用户点击 Confirm 时调用 `lockClosest(memory.getLastBbox())`，建立 `lockedTrackId`。
- overlay 现在显示 `T<trackId> b=<belief>`；locked track 绿色，suspected track 黄色。
- debug 面板新增 `activeTrackCount / trackId / lockedTrackId / suspectedTrackId / trackAge / missedFrames / belief / beliefReason`。
- `FollowStateMachine` 在 `IdentityEvidence.hasBelief()` 时优先使用 `targetBelief + beliefStableFrames + bbox/prediction/lockedTrack` 判断 `FOLLOW / FOLLOW_CAUTION / REACQUIRE_TARGET / IDENTITY_UNCERTAIN`。

### 11.3 构建验证

构建命令：

```powershell
$env:JAVA_HOME='D:\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过，生成 debug APK。构建日志仍有 TensorFlow Lite manifest namespace warning 和 Kotlin/Javac target warning，均未阻塞构建。

### 11.4 手机验收重点

1. 单人正常跟随：`trackId` 应稳定，`targetBelief` 应逐步升高并保持。
2. 目标离开画面：动作应进入 motion stop / local search，不继续前进。
3. 目标离开后干扰者进入：干扰者可形成新 track，但不应快速获得恢复 `FOLLOW` 的 belief。
4. 目标返回：应先成为 suspected track，经多帧 belief 稳定后恢复到 `REACQUIRE_TARGET / FOLLOW_CAUTION`。
5. 目标在场时干扰者穿越：locked track 不应被一帧高 ReID 分数抢走；必要时进入 `FOLLOW_CAUTION / IDENTITY_UNCERTAIN`。

---

## 12. Phase C 诊断采集与 UI 简化（2026-07-08）

### 12.1 实现目的

阶段 C 首轮实机验收后，仍观察到两类问题：

- 目标回到画面后长期停留在黄框，迟迟不转为绿框；
- 非目标人物偶发转绿，表现为疑似跟错人。

本轮没有继续修改 ReID、belief、relock 或状态机阈值，而是先在 Human Cart Simulator 中加入诊断采集能力，用真实日志解释问题发生在哪个环节。

### 12.2 新增代码

| 文件 | 作用 |
|------|------|
| `cartfollow/diagnostics/CartFollowDiagnosticConfig.java` | 管理 frame log、crop、overlay 采样间隔和 JPEG 参数。 |
| `cartfollow/diagnostics/CartFollowDiagnosticSession.java` | 创建 `cartfollow_diagnostics/<session_id>/`，初始化 CSV、gallery/crops/overlays 目录和 `session_info.json`。 |
| `cartfollow/diagnostics/CartFollowDiagnosticSaver.java` | 单线程异步写 `frame_log.csv`、`identity_log.csv`、`events.csv`，并低频保存 locked/suspected/best_reid crop 与初始化 gallery snapshot。 |
| `HumanCartSimulatorFragment.java` | 接入诊断 session 生命周期、人工事件按钮、低频日志保存和简洁/完整 debug 切换。 |
| `fragment_human_cart_simulator.xml` | 新增 `调试详情` 按钮和 `目标离开画面 / 目标回到画面` 事件按钮。 |

### 12.3 输出目录与文件

输出位置：

```text
/sdcard/Android/data/org.openbot/files/Pictures/cartfollow_diagnostics/
└── cart_diag_<yyyyMMdd_HHmmss>/
    ├── frame_log.csv
    ├── identity_log.csv
    ├── events.csv
    ├── session_info.json
    ├── crops/
    ├── gallery/
    └── overlays/
```

`frame_log.csv` 记录每 200 ms 左右的状态机、行为动作、人类指令和人数：

```text
session_id,frame_id,timestamp_ms,elapsed_ms,fps,num_persons,
follow_state,selected_action,action_reason,safety_block_reason,command_text
```

`identity_log.csv` 记录每 200 ms 左右的 track、ReID、bbox gate、belief 和 crop 路径：

```text
session_id,frame_id,timestamp_ms,
track_id,locked_track_id,suspected_track_id,active_track_count,
track_age,missed_frames,best_score,second_score,margin,gallery_size,
weak_ok,mid_ok,strong_ok,bbox_default_ok,bbox_strict_ok,prediction_ok,
target_belief,belief_stable_frames,belief_uncertain_frames,
candidate_switch_count,belief_reason,reid_reason,
locked_crop_path,suspected_crop_path,best_reid_crop_path
```

`events.csv` 记录人工事件：

```text
session_id,timestamp_ms,frame_id,event_type,note
```

当前人工事件只包括：

- `target_left`
- `target_return`
- `session_stop`

### 12.4 UI 行为

- 默认左上角只显示简洁 debug：`fps / state / action / persons / track / locked / suspected / belief / best / margin`。
- 点击 `调试详情` 后显示原完整 debug，再次点击 `收起详情` 回到简洁显示。
- `目标离开画面` 按钮在目标确认前禁用。
- 用户点击 `确认` 后诊断 session 正式启用，事件按钮可用。
- 第一次点击事件按钮写入 `target_left`，按钮文本切换为 `目标回到画面`。
- 第二次点击写入 `target_return`，按钮文本切回 `目标离开画面`。
- 该按钮只写日志，不改变状态机、ReID、track、belief 或 action。

### 12.5 构建验证

构建命令：

```powershell
$env:JAVA_HOME='D:\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过，生成 debug APK。构建日志仍包含 TensorFlow Lite manifest namespace warning、Kotlin/Javac target warning 和若干 deprecated API warning，均未阻塞构建。

### 12.6 手机验收重点

1. Start 后、确认目标前，事件按钮应禁用。
2. 确认目标后，事件按钮启用，默认显示 `目标离开画面`。
3. 目标离开时点击一次，`events.csv` 应出现 `target_left`。
4. 目标返回时再点击一次，`events.csv` 应出现 `target_return`。
5. 默认左上角不再显示大块完整 debug；点击 `调试详情` 后可展开完整字段。
6. Stop / Cancel / Retake / 页面暂停后，诊断 session 应关闭，按钮禁用并复位。
7. 导出诊断目录后，应能用 `events.csv` 附近的 `frame_log.csv` 和 `identity_log.csv` 判断黄框不转绿或非目标转绿的原因。

---

## 13. Phase C ReID 输入方向修正（2026-07-08）

### 13.1 修正原因

PC 侧 `cartfollow_diagnostics` 复盘发现，旧版诊断 gallery 全部被标记为 `landscape_or_rotated`。代码检查确认旧版 `ReIDCoordinator` 直接在原始 `workingFrame` 上按 bbox 裁剪，并立即送入 `TfliteReIDFeatureExtractor`，没有按 `sensorOrientation` 将 person crop 转正。

这意味着旧版 Android ReID 虽然可以运行并输出分数，但 gallery 与候选 crop 都可能以横向姿态进入模型，影响目标返回和多人干扰场景下的稳定性。

### 13.2 本轮代码变化

- `ReIDCoordinator.collectInitializationCandidate()` 增加 `sensorOrientation` 参数，gallery candidate crop 裁剪后旋转为 upright 再提取 embedding。
- `ReIDCoordinator.evaluate()` 和内部 ReID candidate 推理同样使用 upright crop。
- `HumanCartSimulatorFragment` 调用 ReIDCoordinator 时传入当前 `sensorOrientation`。
- 诊断 `gallery/` 中保存的初始化候选 crop 改为 upright 版本。
- `session_info.json` 写入 `reid_crop_upright=true` 和 `sensor_orientation`。
- debug 简洁面板和完整面板增加 `reidCrop=upright`，用于实机确认新版路径已生效。

本轮没有修改 ReID 阈值、belief 阈值、bbox gate、状态机恢复规则或真实底盘控制路径。

### 13.3 构建验证

构建命令：

```powershell
$env:JAVA_HOME='D:\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过。构建日志仍有既有 TensorFlow Lite manifest namespace warning、Kotlin/Javac target warning 和 deprecated Gradle warning，均未阻塞构建。

### 13.4 下一轮手机验收重点

1. Human Cart Simulator debug 应显示 `reidCrop=upright`。
2. 新采集的 `session_info.json` 应包含 `reid_crop_upright=true`。
3. 新采集的 `gallery/` 不应再全部被 PC 分析脚本标记为 `landscape_or_rotated`。
4. 对比旧数据，观察 `target_return` 后 `frames_to_reacquire / frames_to_follow` 是否缩短。
5. 若 upright crop 后仍大量出现 `belief_high_bbox_failed`，下一轮再处理分状态 bbox gate 和 recoverable stop。

---

## 14. Phase C 新旧诊断数据对比结论（2026-07-08）

### 14.1 分析输入

本轮没有继续改 Android 策略，而是先对新旧 `cartfollow_diagnostics` 做 PC 离线对比：

```text
old: tools/reid_pc_test/images/cartfollow_diagnostics_old/
new: tools/reid_pc_test/images/cartfollow_diagnostics/
```

新版数据来自 ReID crop upright 修正后的 APK，`session_info.json` 中应出现：

```text
reid_crop_upright=true
sensor_orientation=90
```

PC 分析命令：

```powershell
cd tools/reid_pc_test
python analyze_cartfollow_diagnostics_v1.py ^
  --compare-roots old=images/cartfollow_diagnostics_old,new=images/cartfollow_diagnostics ^
  --output outputs/cartfollow_diagnostics_analysis/compare
```

### 14.2 对比结果

| 数据 | sessions | target_return | recovered_rate | recovered_fast | recovered_slow | not_recovered | hard_stop | best_mean | margin_mean | bbox_default_rate | gallery_candidate_landscape_rate |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| old | 4 | 11 | 0.5455 | 5 | 1 | 3 | 2 | 0.5017 | 0.3431 | 0.4451 | 1.0000 |
| new | 2 | 16 | 0.8750 | 11 | 3 | 2 | 0 | 0.5992 | 0.4611 | 0.5485 | 0.0000 |

结论：

- upright crop 修正确实生效：新版 `gallery_candidate_landscape_rate=0.0000`。
- ReID 分数整体提高：`best_mean` 和 `margin_mean` 都高于旧版。
- `target_return` 后恢复率提升，且新版暂未出现 `hard_stop_before_return`。
- 这轮结果说明 ReID 输入方向已不是主要问题。

### 14.3 当前剩余问题

新版主要 blocker：

```text
candidate_switch_penalty: 15
belief_high_bbox_failed: 10
```

含义：

- `candidate_switch_penalty` 说明目标返回或多人干扰时，suspected track 仍容易切换，trackId / lockedTrackId 保护还不够稳。
- `belief_high_bbox_failed` 说明 ReID / belief 已经有较强证据，但 bbox default/strict gate 不通过，导致黄框迟迟不能转绿。

因此下一轮 Android 工作不应继续优先调 ReID 模型、TFLite 性能或 `bestScore / margin` 阈值，而应聚焦：

1. `TargetTrackManager` 的 track association 稳定性。
2. locked track 的保留与 suspected track 升级规则。
3. `FOLLOW` 与 `REACQUIRE/IDENTITY_UNCERTAIN/SEARCH` 使用不同 bbox gate。
4. recoverable stop 与 hard `STOP` 的边界。

### 14.4 下一轮验收口径

下一轮策略改动后继续采集 `cartfollow_diagnostics`，重点比较：

```text
recovered_rate
mean_ms_to_follow
not_recovered_in_window
candidate_switch_penalty
belief_high_bbox_failed
非目标转绿是否增加
gallery_candidate_landscape_rate 是否保持 0
```

如果 `candidate_switch_penalty` 和 `belief_high_bbox_failed` 下降，同时非目标转绿不增加，才说明策略改动真正改善了“目标返回后迟迟不转绿”和“干扰者偶发转绿”两个问题。

---

## 15. Phase C 下一轮策略版本：track/bbox gate 小步修正（2026-07-08）

### 15.1 开发定位

阶段 C 首版已经接入 `TargetTrackManager + IdentityBeliefAccumulator`，下一轮不再新增大架构，而是优化 track 粘性、locked/suspected 保护和 bbox gate 的状态化使用。

状态更新：本节为 2026-07-08 的实施计划，2026-07-09 已完成代码接入；最新实现状态见第 16 节。

本轮不做：

```text
更换 ReID 模型
启用 dynamic gallery
全局放宽 bbox gate
接通真实底盘前进
优先做复杂避障
```

### 15.2 计划代码点（已在第 16 节落地）

| 文件 | 改动 |
|------|------|
| `TargetTrackManager.java` | 增加 locked track ghost memory、suspected track 最小滞回、疑似目标替换门槛。 |
| `BboxContinuityEvidence.java` | 在 default / strict 之外新增 loose admission gate。 |
| `IdentityBeliefAccumulator.java` | 分离 identity belief 与 motion permission；belief 高但 bbox failed 时保留 suspected，不直接清零。 |
| `FollowStateMachine.java` | 在 `IDENTITY_UNCERTAIN / SEARCH / REACQUIRE_TARGET` 使用 admission/confirm/motion 分层恢复。 |
| diagnostics/debug 字段 | 输出 `loose_admission_only / motion_gate_failed / suspected_dwell_hold / locked_ghost_reference` 等 reason。 |

### 15.3 策略口径

恢复链路固定为：

```text
loose admission
  -> suspectedTrack
  -> default confirm
  -> REACQUIRE_TARGET
  -> strict/default motion gate
  -> FOLLOW_CAUTION / FOLLOW
```

具体原则：

- loose gate 只允许进入 suspected/reacquire，不允许直接触发前进。
- `FOLLOW / FOLLOW_CAUTION` 继续使用 default/strict bbox gate、prediction 或 locked track 保护，保持运动门槛保守。
- `IDENTITY_UNCERTAIN / SEARCH` 可以接纳 loose admission 的疑似目标，但 action 仍为 `MOTION_STOP / LOCAL_SEARCH`。
- `belief 高 + bbox failed` 表示身份可能正确但运动暂不安全，应保留 belief 或轻微衰减，并保持 `REACQUIRE_HOLD / MOTION_STOP`。
- hard `STOP` 仍只由搜索超时、急停、通信异常、高风险障碍或人工取消触发。

### 15.4 复测场景

下一轮 APK 构建后采集 4 段短日志，每段 30-60 秒：

1. 目标离开后原目标返回。
2. 目标离开后干扰者进入。
3. 目标在场时干扰者穿越。
4. 目标短遮挡、蹲下或局部可见。

PC compare 验收指标：

```text
recovered_rate 不下降；
mean_ms_to_follow 下降或不恶化；
not_recovered_in_window 减少；
candidate_switch_penalty 下降；
belief_high_bbox_failed 下降；
非目标转绿不增加；
hard_stop_count 不增加；
gallery_candidate_landscape_rate 保持 0。
```

---

## 16. Phase C relock、空间支持门控与日志开关实现（2026-07-09）

### 16.1 本轮实现状态

本轮 Android 代码已完成接入，目标是修正 2026-07-09 新采集数据暴露的两个问题：目标返回后新 track 没有晋升为 locked track，以及干扰者在没有 bbox / ghost 空间支持时仍可能靠 ReID 抬高 belief。

本轮仍保持边界不变：

```text
不更换 ReID 模型
不启用 dynamic gallery
不开放真实底盘前进
不改 ControlGenerator 的真实底盘控制路径
不提前引入复杂避障
```

### 16.2 恢复后 relock

- 非 locked track 在 `REACQUIRE_TARGET / READY_TO_FOLLOW / FOLLOW_CAUTION / FOLLOW` 的安全恢复路径中，如果连续通过 motion gate，并在 `FOLLOW_CAUTION / FOLLOW_SLOW` 等保守动作下稳定至少 2 帧，可以晋升为新的 `lockedTrackId`。
- relock 成功后同步调用 `IdentityBeliefAccumulator.lockTrack(newTrackId)`，清空旧 suspected track。
- debug / diagnostic reason 输出 `relock_after_recovery`，用于 PC 侧确认目标返回后是否真正完成重新锁定。
- 普通 `IDENTITY_UNCERTAIN` 中单帧高 ReID 分数不允许直接 relock。

### 16.3 非 locked 空间支持门控

- 非 locked track 只有满足 `bboxLoose / bboxDefault / prediction / nearLockedGhost` 之一，才允许成为 suspected target。
- 如果只有 ReID 高分但空间支持全 false，记录 `reid_interest_no_spatial_support` 或 `spatial_support_missing`。
- 无空间支持时允许保留低强度观察信号，但 belief 不得升到 caution / confirm 阈值，也不能触发 suspected 或 FOLLOW。
- `candidateSwitchCount` 只在真正选中或切换 suspected / selected track 时增长，减少“观察到高分干扰者”导致的噪声。

### 16.4 诊断日志开关

- `fragment_human_cart_simulator.xml` 底部控制区新增 `diagnostic_switch`，文案为“记录日志”，默认关闭。
- `HumanCartSimulatorFragment` 新增 `diagnosticEnabled` 状态。
- 日志关闭时，`startDiagnosticSession()` 和 `activateDiagnosticSession()` 直接返回，不创建 `cartfollow_diagnostics` session，不写 `session_info.json`，不保存 confirmed snapshot、gallery、crop、CSV、events 或 session_stop。
- 运行中关闭日志会立即停止 active session，并禁用“目标离开 / 返回”事件按钮。
- 日志开启并 Confirm 激活 session 后，事件按钮才可用；跟随推理、状态机和 debug 文本不受日志开关影响。

### 16.5 构建验证

构建命令：

```powershell
$env:JAVA_HOME='C:\Users\ysyys\.jdks\jbr-17.0.14'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :robot:assembleDebug
```

结果：构建通过，生成 debug APK。构建日志仍包含既有 TensorFlow Lite namespace、Kotlin/Javac target 和 deprecated API warning，未阻塞构建。

### 16.6 下一轮手机验收重点

1. 日志开关关闭时，Start、Confirm、运行、Retake、Cancel 都不应新增 `cartfollow_diagnostics/cart_diag_*` 目录。
2. 日志开关开启后，Confirm 才激活 session，并正常生成 `frame_log.csv / identity_log.csv / events.csv / session_info.json / gallery / crops`。
3. 目标离开后原目标返回：期望恢复到新 track 后自动 relock，后续 `trackId == lockedTrackId`。
4. 蹲下、弯腰或局部遮挡：期望检测器丢框后可恢复，恢复成功后 relock 到新 track。
5. 目标离开后干扰者进入：期望干扰者无空间支持时不进入 suspected，不恢复 FOLLOW。
6. 目标在场时干扰者穿越：期望 `candidateSwitchCount` 明显下降，非 locked FOLLOW 行数减少或归零。
7. PC compare 继续检查 `candidate_switch_penalty / belief_high_bbox_failed / recovered_rate / hard_stop_count / gallery_candidate_landscape_rate`，合格后再讨论极低速真实底盘联调。

---

## 17. BLE 真实小车跟随模块首版（2026-07-12）

### 17.1 实现定位

新增主菜单入口 `Real Cart Follow`，将 Human Cart Simulator 的相机、检测、ReID、
track/belief、状态机、ActionArbitrator 和诊断能力抽取到
`BaseCartFollowFragment`。Simulator 继续只显示人肉模拟指令；真实模块才允许向
`Vehicle` 输出控制。

### 17.2 BLE 会话与安全边界

- 严格匹配 OpenBot BLE Service、RX 和 TX UUID，Notify 订阅完成后才认为串口可用。
- BLE Ready 后启动幂等 `h750` 心跳并发送 `f`，只有收到 `fCART_AT8236` 和 `r`
  后才允许运动。
- 页面暂停、模式切换、BLE 断开、推理超过 400 ms 无更新时立即发送零控制。
- 手机急停发送 `!S,<seq>`，ESP32 锁存急停后必须重启才能恢复。
- 下位机还独立检查 500 ms 非零运动命令保鲜，心跳不能掩盖控制线程卡死。

### 17.3 手动与自动模式

手动模式为 dead-man 控制，每 100 ms 重发当前命令：

```text
前进 28
后退 24
原地转向 20
松手 / CANCEL / 退后台 -> 0,0
```

自动模式每次进入页面都需要长按 2 秒解锁，最大协议输出为 32，
`FOLLOW_CAUTION` 进一步降速。`LOCAL_SEARCH` 固定为低速原地旋转，连续运动最多
2 秒，超时后停车并撤销自动解锁。

近场传感器和物理急停尚未接入，因此自动模式只用于空旷场地、有人准备物理断电的
实验，不视为正式避障能力。

### 17.4 自动化验证

使用 JDK 17 完成：

```powershell
.\gradlew.bat :robot:testDebugUnitTest --no-daemon
.\gradlew.bat :robot:assembleDebug --no-daemon
```

结果：

```text
RealCartSafetyControllerTest  6/6
全部单元测试                 11/11
Debug APK                    android/robot/build/outputs/apk/debug/robot-debug.apk
```

### 17.5 真机验收顺序

1. 不接电机验证 BLE 扫描、`f/r/h`、急停和重启解除。
2. 车轮悬空验证四方向、松手停车、断连停车和双超时停车。
3. 空载落地只开放手动模式。
4. 手动全部通过后，才在空旷场地长按解锁自动实验。
5. 传感器和物理急停到位前，不进入货架、拥挤或无人看护场景。

---

## 18. BLE 手动控制抢占与低速调参（2026-07-12）

### 18.1 问题与修复

真机发现方向按钮切换后仍可能继续执行旧动作。第一版 `ManualControlArbiter` 只处理
方向状态，没有覆盖多 pointer 触摸和异步 GATT 连续写入。

本轮加入单在途 BLE 串口写队列：停车、急停、运动、心跳和查询统一排队；周期运动
采用 latest-wins；方向切换以 `c0,0 -> 新方向` 有序事务提交，心跳和周期重发不能拆开
该事务。关键停车写失败会重试一次，再失败则撤销 BLE ready 并禁止继续运动。

手动仲裁现在同时记录方向、pointer ID 和 generation。新手指按下后取得唯一控制权，
旧手指迟到的 `UP/CANCEL` 不得停止或恢复旧方向。页面同时显示当前方向、输出、BLE
写入状态和构建版本。

### 18.2 低速参数

```text
手动前进      14
手动后退      12
手动原地转向   5
自动最大输出  14
自动搜索速度   5
```

ESP32 的 500 ms 运动保鲜、40 ms 控制周期和步长 6 保持不变。

### 18.3 自动化验证

```text
robot 单元测试  26/26
checkStyle       通过
assembleDebug    通过
Debug APK        android/robot/build/outputs/apk/debug/robot-debug.apk
```

真机仍需在车轮悬空状态验证单指换向、双指抢占、旧手指迟放、最终松手、退后台和 BLE
断开停车。手动验收通过前不得恢复自动跟随测试。

---

## 19. BLE 控制证据日志与统一触摸路由（2026-07-12）

### 19.1 修改目的

真机换向问题仍未验收，原有日志只能看到 GATT 写成功 payload，不能对齐触摸、pointer、
generation、队列入队和实际 dispatch。四个按钮各自监听也不能可靠覆盖单指从一个按钮
滑到另一个按钮的事件路径。

### 19.2 实现

- “记录日志”开关现在同时控制 `CartControl` Logcat 诊断；默认关闭。
- 触摸日志记录 elapsed realtime、事件、pointer ID、generation 和当前方向。
- BLE 队列记录 enqueue、transition、dispatch、success、failure、retry、clear、类型、
  generation、pending 数量和 payload。
- 四个方向按钮改为由 `manual_drive_controls` 统一接收 MotionEvent，再按子控件坐标路由。
- 当前 pointer 可在按住期间从 A 滑到 B，并触发有序 `c0,0 -> 新方向`。
- 第二个 pointer 按下可抢占；旧 pointer 后续移动或释放不能覆盖当前方向。
- 页面暂停、窗口失焦、模式切换、BLE 断开和 CANCEL 继续停车。
- 速度保持手动 `14/12/5`、自动 `14/5`；本轮不修改 ESP32 或继续降低速度。

日志查看：

```powershell
adb logcat -c
adb logcat -s CartControl:I
```

ESP32 同时通过 USB 发送 `!D,1`，即可对齐 Android `CartControl` 与下位机
`motion_rx / motion_target / drive_output / $MSPD`。

### 19.3 验证

```text
robot 单元测试  30/30
checkStyle       通过
assembleDebug    通过
Debug APK        android/robot/build/outputs/apk/debug/robot-debug.apk
```

自动化测试不能替代车轮悬空验收。若 Android 日志顺序正确，而 ESP32 `$spd` 或 AT8236
`$MSPD` 仍显示四轮换向不同步，应转入下位机换向过零联锁和逐轮最低可靠速度测试。

---

## 20. 首版真实车自动跟随输出整形（2026-07-13）

### 20.1 当前定位

Real Cart Follow 已经复用 Human Cart Simulator 的检测、目标确认、ReID、track/belief、
距离估计和行为仲裁。真车手动遥控基本验收后，首版自动模式不再直接缩放连续
`Control`，而是在 BLE 输出前经过 `RealCartAutoDriveController`。

该整形器只允许四种命令：

```text
直行       c14,14
向左缓弯   c12,14
向右缓弯   c14,12
停车       c0,0
```

两轮始终同向且不倒车。首版不输出原地转向，也不把 `LOCAL_SEARCH_LEFT/RIGHT` 映射为
运动，避免触发下位机原地转向约 `24..40` 的助推输出和长时间换向等待。

### 20.2 运动准入与停车

- 停车状态下，只有可信 `FOLLOW + FOLLOW_SLOW`、`heightScale <= 0.80` 且目标连续 3 帧
  居中时才允许从 `c0,0` 起步。
- 横向控制使用 `alpha=0.25` 的 EMA；滤波转向量不超过 `0.15` 时直行，`0.15..0.45`
  时同向差速缓弯，超过 `0.45` 时停车。
- 直行与缓弯切换不插入 `c0,0`，避免不必要地触发下位机停稳保护。
- 距离 `OK / TOO_CLOSE / UNKNOWN`、身份谨慎或不确定、中心受阻、系统安全门失败均停车。
- `IDENTITY_UNCERTAIN / LOST / SEARCH` 只允许静止重捕；2 秒内未恢复则撤销自动解锁，
  关闭 Start，并要求重新采集和确认目标。
- Start 关闭、页面失焦/暂停、BLE 断开、握手丢失、推理超过 400 ms 和急停都会立即
  将最后输出替换为 `c0,0`。

### 20.3 诊断与验证

真实页面新增 `LOCKED / WAIT_TARGET / WAIT_CENTER / MOVING_STRAIGHT / CURVE_LEFT /
CURVE_RIGHT / RECOVERY_STOP` 状态显示。打开“记录日志”后，`CartControl` 每次状态变化或
每 250 ms 记录原始 turn、滤波 turn、heightScale、行为动作、整形输出和拒绝原因；
默认关闭时不增加日志负担。

本轮自动化验证应包含 `RealCartAutoDriveControllerTest`，并继续运行全部 robot 单元测试、
Android lint/check 和 `assembleDebug`。自动跟随只能按“车轮悬空 -> 空旷直线 -> 人物缓慢
横移缓弯”的顺序验收，超声波/ToF 接入前不得进入货架、拥挤区域或无人看护测试。

本轮实际验证结果：

```text
RealCartAutoDriveControllerTest  5/5
全部 Debug 单元测试             35/35
:robot:check                    通过
:robot:assembleDebug            通过
Debug APK                       android/robot/build/outputs/apk/debug/robot-debug.apk
```

### 20.4 自动解锁闪退修复

首版真机测试发现：长按自动解锁后 Start 短暂亮起，随后立即变灰。根因是推理 watchdog
在 Start 尚未打开、第一帧推理尚未产生时，就把 `lastInferenceMs=-1` 判为超时并撤销解锁。

现改为区分“已解锁”和“Start 已启动”：仅长按解锁不会启动推理 watchdog；用户真正打开
Start 后才从当前时刻开始 400 ms 首帧等待。自动运行开始后，超过 400 ms 没有新推理仍会
停车并撤销解锁，原安全保护没有放宽。

### 20.5 Start 后推理线程闪退修复

真机连接后再次测试发现：Start 可以正常打开，但第一帧人物检测会在 inference 线程闪退。
Logcat 明确显示 `BaseCartFollowFragment.processFrame()` 使用空的 `croppedBitmap` 创建
`Canvas`。根因是页面进入后台时检测器被保留，恢复页面时却只清空了裁剪位图；后续代码
看到检测器仍存在便跳过初始化，形成“检测器有效、推理缓冲区为空”的不一致状态。

现改为页面恢复时保留匹配检测器的推理缓冲区，并在每帧推理前同时检查裁剪位图及正反
坐标变换。任一对象缺失时先重新配置检测器，仍未成功则跳过该帧，不再进入推理线程。
推理任务同时捕获当帧使用的检测器、位图和坐标变换快照，避免生命周期字段变化影响
已经排队的任务。

### 20.6 拍照确认与 BLE 半连接修复

真机继续测试发现，Start 打开后虽然可以完成 15 帧目标采集并显示拍照确认面板，但
400 ms 推理 watchdog 在静止采集和等待确认阶段仍会运行。当某帧检测或 ReID 超过
400 ms 时，真实车页面会关闭 Start 并取消状态机，却没有同步隐藏旧确认面板，造成
“照片仍在、确认按钮无反应”的假窗口。

现将 watchdog 改为只保护最近一次自动输出为非零的运动阶段。采集目标、等待确认、
重新识别、倒计时和等待居中时车辆输出均为零，可以等待用户操作；一旦车辆已收到
非零命令，超过 400 ms 没有新推理结果仍会立即停车并撤销解锁。所有自动结束路径
统一重置 Start、确认面板、倒计时、ReID、track/belief、诊断会话和状态机。确认与
重拍按钮也会校验当前确实处于 `LOCKED_PENDING_CONFIRM`，过期窗口不能锁定目标。

BLE 侧确认 EasyBLE 2.0.2 存在 MTU 失败后不回调失败的路径。旧实现只有 MTU 64
成功后才订阅 Notify，因此会永久停在半连接状态，只能通过重启 App 重建 BLE 栈。
当前控制、心跳及 `f/r` 握手报文均适配默认 MTU，现改为服务发现后直接订阅 Notify，
并加入：

- 3 秒 Notify 初始化超时；
- 每次连接独立 generation，忽略旧连接回调；
- 失败后完整断开并等待 750 ms，自动重试一次；
- 第二次失败后销毁并重新初始化 EasyBLE GATT 栈，允许再次点击连接；
- 主动断开不自动重连；
- BLE 列表显示“连接中 / 初始化串口 / 自动重试 / 连接失败”等中文状态。

BLE 生命周期使用 `Bluetooth_Connection` 标签持续记录低频状态；控制与写队列明细仍
由“记录日志”开关控制，默认关闭。

本轮实际验证结果：

```text
全部 Debug 单元测试             41/41
FollowSessionGuard Robolectric  2/2
:robot:check                    通过
:robot:assembleDebug            通过
Debug APK                       android/robot/build/outputs/apk/debug/robot-debug.apk
```

### 20.7 可见人物重捕与静止缓弯修复

悬空自动跟随测试又发现两个容易混淆的现象：Start 关闭后最后一帧绿框仍停在画面上；
视觉层显示“左转 / 右转”时，真实车轮速仍可能是 `c0,0`。前者是会话清理没有清空
Overlay，后者是公共视觉建议与真实车安全整形输出使用了不同口径。

真实车现改为：

- 身份不确定但画面仍检测到人物时，持续输出 `c0,0` 并保持 Start，继续 ReID 和轨迹重捕；
- 只有连续 2 秒完全检测不到人物时，才以 `target_missing_timeout` 结束会话；
- 会话结束时同步清空检测框、目标标签和最后一帧绘制状态；
- 停车状态下，可信目标连续 3 帧处于可缓弯范围后可直接输出 `c12,14` 或 `c14,12`；
- 横向偏差超过 `0.45` 时仍停车，不开放倒车或原地转向；
- 主提示改为真实输出对应的“直行 / 左缓弯 / 右缓弯 / 停车等待 / 停车重捕”，
  公共视觉建议只保留在详细诊断中；
- 页面保留 `last_end`，并用始终开启的 `CartFollow_Session` 低频日志记录 Start 和结束原因。

Human Cart Simulator 的 18 秒身份不确定/搜索窗口不变；只有真实车实例关闭共享终态计时，
由连续无人 2 秒的安全门负责结束自动会话。

本轮实际验证结果：

```text
RealCartAutoDriveControllerTest  7/7
FollowSessionGuardTest          3/3
全部 Debug 单元测试             45/45
:robot:check                    通过
:robot:assembleDebug            通过
Debug APK                       android/robot/build/outputs/apk/debug/robot-debug.apk
```

## 21. Human Cart Simulator 连续转向证据（2026-08-31）

### 21.1 目的与边界

本轮只增强 Human Cart Simulator 的视觉侧表达：原来连续的目标横向偏差会被压缩为
“左转 / 右转”文字，现在保留为可查看、可记录的连续转向需求。该需求是图像平面上的
归一化证据，不是未经底盘标定的转向角、曲率或米制半径；真实车的 BLE 输出和 ESP32
固件均未修改。

### 21.2 算法定义

`SteeringDemandEstimator` 以目标框中心计算横向误差：画面左侧为 `-1`，中央为 `0`，
右侧为 `+1`。它用 `alpha=0.55`、`beta=0.10` 的 alpha-beta 滤波器估计平滑位置和
横移速度，并分别给出 `0 / 400 / 800 ms` 的短期预测。目标 track 改变、目标丢失或
相邻样本间隔超过 `500 ms` 时，滤波器会重置，避免把旧目标的速度带给新目标。

当目标框靠近对应画面边缘的 `15%` 区域时，边缘紧迫度会抬高需求，避免“人物框尚未
完全离屏，提示却仍然偏小”。需求等级带滞回，分为居中、轻微、中等、大幅和接近边缘，
降低人物轻微晃动造成的提示跳变。

方向统一使用手机屏幕坐标：目标位于左侧时 Simulator 显示向左转，目标位于右侧时显示
向右转。`HumanCommandInterpreter`、连续仪表和真实车差速均使用这一口径，并有左右对称测试。

### 21.3 页面、日志与后续使用

Simulator 页面新增固定尺寸的转向仪表：白色标记表示滤波后的当前位置，蓝色描边标记
表示预测位置。主提示显示需求百分比、方向、等级、当前偏差和预测偏差；预测提前量
只在 Simulator 页面可切换。身份不确定、距离过近、受阻或目标丢失时，安全停车提示
仍优先，转向证据仅保留在诊断信息中。

`cartfollow_diagnostics` 的 CSV 在既有列末尾追加 raw/filter/prediction/edge urgency/
demand/direction/level 等字段，旧日志读取方式不受影响。后续应先用人肉模拟记录三档
预测提前量，再用真车实测的延迟、起转阈值和轮速反馈建立多档曲率映射；在没有轮速
反馈前，上位机不能猜测某一个轮子的补偿量。

### 21.4 验证状态

已添加 `SteeringDemandEstimatorTest`、`HumanCommandInterpreterTest` 和
`SteeringDemandViewTest`，覆盖左右对称、预测提前、停止后回落、边缘紧迫度、重置、
预测档位、等级滞回、方向语义及仪表绑定。首次运行时 Google Maven 依赖下载曾发生 TLS
握手失败；依赖恢复后已在 JDK 17 下重新执行验证：

```text
全部 Debug 单元测试             57/57
:robot:check                    通过
:robot:assembleDebug            通过
Debug APK                       android/robot/build/outputs/apk/debug/robot-debug.apk
```

## 22. 真实车连续动态缓弯（2026-09-01）

真实车不再根据旧 `Control` 的正负号单独推测左右，而是直接复用与 Human Cart Simulator
相同的 `SteeringEvidence`。因此 Simulator 显示“向左”时，真实车必定发送左轮较慢、右轮
较快的前进差速；显示“向右”时则相反。真实车固定使用 `400 ms` 预测提前量，预测档位
不会出现在真实车页面。

自动模式仍保持低速上限 `14`，但将原先固定的 `12,14 / 14,12` 扩展为连续量化差速：

```text
需求 0% 或居中      c14,14
需求 1%..100%       外侧轮 14，内侧轮 14 平滑降至 10
安全门失败          c0,0
```

所有自动运动命令保持双轮正向，不倒车、不原地转向、不使用单侧停轮。目标丢失、身份不确定、
距离状态不允许、BLE 异常和推理 watchdog 仍优先发送停车。真实车主提示和转向仪表同时显示
需求、方向、等级及实际 `left,right` 输出，便于将手机日志与悬空轮速观察对应。

原有 ESP32 的直行起步辅助会把低速输出托到相同最低值，导致 `c10,14` 在刚起步时仍可能
近似直行。本轮固件新增内部 `STARTUP_ASSIST_CURVE`，不改变 BLE 协议；曲线起步的 kick
与保持输出按左右目标比例放大。精确直行 `c14,14` 的同步起步、trim、保鲜、急停和换向
保护路径保持不变。

## 23. 真实车确认、转向标定与模型生命周期修复（2026-09-01）

真实车页面的目标框、确认面板和主提示现在以带会话版本与帧序号的单次 UI 更新提交。旧帧
不能再覆盖确认后的状态：待确认人物使用黄色框和“待确认”标签；只有完成确认且身份可信的
目标才显示绿色框。真实车主提示只显示实际安全整形后的输出，不再被公共视觉建议异步覆盖。

转向误差统一为手机屏幕坐标：左侧为负、右侧为正。估计器按 `0/90/180/270` 度旋转将 bbox
先转换到显示水平轴，再计算预测和边缘紧迫度，因此人物在屏幕右侧时，仪表圆点、方向文字和
真实车差速均为“向右”。

自动实验模式增加“转弯强度”滑条，范围 `20%..200%`、步长 `5%`，默认 `100%`。当前取值保存
在手机设置中，重新进入页面会恢复；最大转弯从 `c10,14 / c14,10` 可扩展到
`c6,14 / c14,6`，但始终不产生倒车、单侧停车或原地转向。滑条释放、会话开始/结束及手动
记录会写入无图片的：

```text
Android/data/org.openbot/files/Documents/cartfollow_tuning/steering_strength_history.csv
```

进入 BLE 设备选择页会暂停自动会话，返回后仍需重新解锁和启动。模型配置改为由第一张有效
相机帧提供宽高，在推理线程内原子发布 Detector、Bitmap 和变换矩阵；页面暂停或相机规格变化
会使旧配置任务失效，避免旧任务访问已释放的相机缓冲区并弹出误导性的“模型配置失败”。

## 24. 手动直行速度标定（2026-09-02）

下位机提速固件仍使用既有 BLE 报文 `c<left>,<right>`：正负号表示前后方向，绝对值已经是
逻辑速度，而不是单纯的方向开关。固件将 `c9/c14/c18/c21` 分别映射为约
`103/160/206/240 mm/s`；`21` 是当前 240 mm/s 硬上限。因而本轮没有新增 Android BLE
报文，也没有更改自动跟随输出。

真实小车页面的手动模式新增直行速度滑条，范围为 `9..21`、步长为 `1`、首次默认 `14`。
前进直接发送所选档位；后退继续按历史 `12/14` 比例缩放，例如前进 `c18` 对应后退
`c-15`。原地左/右转仍固定为 `c-5,5 / c5,-5`，不受滑条影响。滑条值保存在手机本地，重新
进入页面后恢复。

按住前进或后退时调整滑条，只会替换下一次 100 ms 周期发送的同方向运动目标，不会插入
`c0,0`。松手、失焦、退后台、BLE 断开、模式切换和急停的立即停车路径不变。测试必须按
`c9 -> c14 -> c18 -> c21` 逐档通过悬空、制动和空旷地面短距离验证后再升档。

当前协议尚不能表达麦轮横移。速度标定完成后，下一阶段再定义兼容旧 `c` 指令的车体运动
接口；横移首版只用于手动验证，不直接加入自动跟随。

## 25. Human Cart Simulator 动态 Gallery 与拟真跟随验证（2026-09-02）

本轮将身份适应、遮挡恢复和自动调档先放入 Human Cart Simulator 验证，真实车自动输出与
ESP32 固件均保持不变。目标初始化仍自动采集 15 帧并由用户确认一次；确认照片现在与 ReID
crop 使用相同的 `sensorOrientation` 转正，不再出现竖放手机时照片横置的问题。

ReID Gallery 拆分为两层：确认时从最多 12 个候选中选出的 8 个 Anchor 在会话内只读；动态
模式最多再维护 8 个 Adaptive 特征。Adaptive 只在单人、locked track、高 belief、强 ReID、
轨迹连续且 crop 完整时更新，新外观必须连续复核 3 次。多人、身份不确定和重捕阶段冻结学习，
并保留 `anchorScore >= 0.70` 的最低身份约束，避免 Adaptive 自身形成错误确认闭环。模拟器可在
Start 前切换“固定 / 动态 Gallery”，Start 后锁定本轮设置。

遮挡恢复实验增加全局一对一 bbox 关联，并在恢复状态把 ReID 候选上限从 3 提高到 5。目标在
ghost 区域之外重新出现时，必须连续 3 次满足 `bestScore >= 0.85`、`margin >= 0.08` 才允许
重新锁定；整个过程始终模拟 `c0,0`。画面中仍有人时持续静止重捕，完全无人时按用户选择的
2 秒或 5 秒窗口结束会话。

模拟控制使用三档前进速度：低档 `c14`、中档 `c18`、高档 `c21`。距离越远允许档位越高，
转向需求超过 30% 时最高中档，超过 60% 时固定低档；升档需连续 3 帧，降档立即生效。页面
同时显示流程、belief、Anchor/Adaptive/候选数量、距离状态、档位、左右轮模拟输出和停车原因。
绿色框仅在 locked track 的 belief 达到 caution 门槛时显示，不能再被解释为车辆一定运动。

诊断日志追加 Gallery 分离分数、更新事件、模拟阶段、档位和 `c<left>,<right>`；只有打开
“记录日志”时才保存晋升的 upright crop。新增动态 Gallery、全局重捕和三档控制测试后，
Debug 单元测试为 `73/73`，`:robot:check` 通过；其中包含多人检测顺序变化时的全局一对一轨迹配对回归测试。真机自动输出尚未采用这些实验参数。

## 26. Human Cart Simulator 姿态恢复与定向主动重捕（2026-09-02）

> 后续修订见第 27 节。此节为历史实现记录，其测试结果不覆盖 2026-09-03 修订。

本轮继续只在 Human Cart Simulator 验证，不向 BLE 发送旋转命令，也不改变真实车自动控制
和 ESP32 固件。首次确认仍保留 3 秒倒计时；已经进入过跟随的会话在遮挡后恢复时，需要连续
3 次新鲜且稳定的 ReID 证据，随后直接进入 `FOLLOW_CAUTION`。若恢复前正在模拟旋转，先保持
`c0,0` 约 300 ms，再恢复低档跟随，不重复倒计时。

人物检测增加高低置信度两级输入。界面设置值仍是高阈值，低阈值按
`max(0.05, min(0.25, high - 0.05))` 计算。低分框只能续接已经存在的 locked/suspected
轨迹，不能创建新 track、触发运动或更新普通 Gallery；页面以灰色虚线框显示。任何高低分
多人同时出现都会冻结 Gallery 更新。

动态 Gallery 新增最多 4 个样本的 Quarantine 区，用于隔离蹲下、坐下、正背面突变等
`0.60 <= ReID < 0.85` 的连续外观变化。只有单人、locked track、空间连续、crop 完整且
连续特征相似的样本才能进入；3 次一致证据进入隔离区，5 次一致且持续至少 1.5 秒后才晋升
Adaptive。Quarantine 只能帮助当前轨迹续接，不能单独确认远处候选；多人、换 track、全局
重捕或连续性中断会立即清空。

`DIRECTED_REACQUIRE` 只由可信目标从左右边缘离屏触发：离屏前 belief 至少 0.75，预测偏差
至少 0.65，人物框进入边缘 15% 区域且横移速度方向一致，随后高低分候选连续 3 帧均无法
续接。左侧离屏显示 `c-speed,+speed`，右侧离屏显示 `c+speed,-speed`；任意人物候选重现
都会先显示 `c0,0` 并静止做 ReID。居中消失、多人交叉和方向证据过期仍只做静止重捕。

页面增加模拟旋转档位 `5..21`、最大角度 `30..180°` 和最大时间 `1..10 s` 三个滑条，Start
开启后锁定。`YawTurnTracker` 将陀螺仪角速度投影到重力轴，只累计搜索方向上的相对转角；
方向相反时不增加进度。无陀螺仪时页面明确显示“转角不可测”，并仅按时间上限结束模拟搜索。
诊断 CSV 追加双阈值、低分续接、Quarantine、搜索方向、模拟输出、转角和结束原因字段。

设计思想参考：

- [ByteTrack](https://github.com/ifzhang/ByteTrack)：低分检测二阶段关联；
- [Deep OC-SORT](https://github.com/GerardMaggiolino/Deep-OC-SORT)：运动连续性与外观证据结合；
- [D-Robotics edge-turn](https://github.com/D-Robotics/rdk_model_zoo)：边缘离场后按方向搜索的工程启发。

这些项目仅提供设计参考；本模块仍沿用 OpenBot 当前检测、ReID、轨迹和安全仲裁代码，没有
直接引入其完整跟踪器或控制代码。

本轮验证结果：

```text
全部 Debug 单元测试             86/86
:robot:check                    通过
:robot:assembleDebug            通过
Debug APK                       android/robot/build/outputs/apk/debug/robot-debug.apk
```

## 27. 离屏重捕与 Gallery 精确采样修订（2026-09-03，自动化通过、手机待测）

状态：主实现任务已提供最终验证结果，Debug 和 Release 各 128/128，均为 0 失败、0 错误、
0 跳过。离线执行 `:robot:testDebugUnitTest :robot:check :robot:assembleDebug` 全部成功，
耗时 1 分 19 秒；变更 Java 的 google-java-format 1.7 dry-run 及 diff check 通过。
手机测试未执行，本轮 APK 未安装。范围仍限 Human Cart Simulator；
`RealCartAutoDriveController`、真实车 Safety、RealFragment、BLE 和固件均未修改。

本轮验收契约：

- 获准推理帧使用不可变快照，Detector 与 ReID 使用一致的 generation / seq；旧会话完成
  回调不得污染新会话。ReID 缓存绑定 track、源帧和时间，不跨轨迹使用，不重复计作新证据。
- 用实际完成次数统计 FPS，并区分 Detector / ReID / 管线耗时和结果年龄。旧 FPS 是
  Detector 耗时换算值，可能停止后仍保留，不代表完整闭环速度。
- 允许正常安装视角造成的上下截断；upright 可见高度占转正后画面高度的比例，普通 Gallery
  >= 18%，Quarantine >= 12%，不是面积比例；upright crop >= 32 x 64 像素。当前未实现
  模糊或曝光检测，验收只区分几何与身份拒绝，不评估模糊分类能力。
- 满足单人、locked track、身份与质量门控时，静止也能学习。Quarantine 不得提高当前
  身份评分来反过来批准自身；3 次新鲜一致证据入隔离区，5 次且至少 1.5 秒才晋升。
  局部连续轨迹 Anchor >= 0.60，全局重捕 Anchor >= 0.70，仍需其他重捕安全门。
- 定向离屏取最近 800 ms 的单个可信目标证据，belief >= 0.75、对应左右边缘 15%、
  至少 2 次向外运动观测；连续缺失至少 2 帧且至少 100 ms 后才可触发。
- 定向搜索的独立 owner 管理 1..10 秒总窗口，覆盖旋转、停车验证及等待各阶段，不因
  候选出现或阶段往返重置。普通静止重捕的 2/5 秒计时独立，不争夺定向搜索的结束权。
- 普通无人计时从首次缺失开始，即使仍停留在 `FOLLOW` 也不能延迟起算；低置信的可能
  人物在场时保持普通等待，不按完全无人结束。`DIRECTED_REACQUIRE` 已纳入全局 ReID
  恢复路径，使用最多 5 个恢复候选的预算。
- 陀螺仪可在 Start 关闭时独立测试左/右/停止/重置；数据须在 500 ms 内新鲜，角度使用
  有符号净转角，反向抵消；重力数据同样须在 500 ms 内新鲜。无传感器或任一路数据陈旧
  时不得呈现为有效转角。
- 新增距离诊断不改变原跟随/调档阈值，现有模拟器仍使用距离参与调档，不宣称米制测距。
  搜索期间仍先 motion_stop，失败
  或安全异常后 hard STOP；独立陀螺仪测试不产生真实底盘命令。

“记录日志”默认关闭，关闭时不创建诊断 session、CSV、crop、gallery 或 event。
开启后图片必须对应真正进入采样流程的源帧，能关联 generation / seq / track 与采样、拒绝、
隔离及晋升事件；绿框不等于学习，每个绿框帧也不必保存图片。不能用晋升时的当前画面替代
此前实际被采样的 crop。

诊断目录使用 `Environment.DIRECTORY_PICTURES`，实际路径为
`Android/data/org.openbot/files/Pictures/cartfollow_diagnostics/`。本轮已追加字段如下：

- `frame_log`：`frame_received_ms`、`sensor_timestamp_ns`、`detector_ms`、`reid_ms`、
  `pipeline_ms`、`result_age_ms`、`dropped_frames`、`observation_source`、`observation_track_id`、
  `screen_left`、`screen_top`、`screen_right`、`screen_bottom`、`distance_diagnostic`。
- `identity_log`：`reid_observation_id`、`reid_observation_ms`、`reid_source_frame`、
  `reid_scored_track`、`reid_fresh`、`crop_visible_width_px`、`crop_visible_height_px`、
  `crop_height_ratio`、`crop_normal_reason`、`crop_quarantine_reason`。
- `events` 中的 `frame_presented`：`source_age_ms` 与 `generation`。

详细验收见主仓库 `design/模拟器离屏重捕与Gallery采样测试指南.md`，统一方案见
`design/自主跟随购物车上位机软件开发计划.md` 第 22 节。补录结果时分开记录自动化结果、
安装包版本和手机场景结果；失败也保留，不把待测场景标成通过。

### 27.1 本地验证产物

- APK：`E:/THU/2026Summer/AutoFollowShoppingCart/dev/OpenBot/android/robot/build/outputs/apk/debug/robot-debug.apk`
- 大小：42,371,792 字节；文件修改时间：`2026-09-03 11:36:48+08`。
- 应用 ID：`org.openbot`；版本：`v0.8.0`；versionCode：`800`。
- SHA256：`93E5652C492DB8150BFB298C5C0C482D5C6E447547807673040FE716EF2EE02B`。

用户可见版本未变，不能仅凭 v0.8.0/800 判断是否为本轮包；以完整路径及 SHA256 标识本地
构建，不虚构提交或分支标识。本次文档任务未安装 APK、未做手机测试、未提交或推送。

## 28. 独立测角左右符号修正（2026-09-03）

用户已验证传感器工作正常，但独立测角实际左右与所选方向相反。修正
`YawTurnTracker` 的期望符号映射：重力轴投影为正时对应左转，为负时对应右转。
独立测角与模拟定向搜索共用此修正；视觉转向、模拟轮速映射、真实车控制及 BLE/固件不变。

左右转与不同手机朝向的合成输入验证：顺向角度增加、反向抵消并提示方向错误；同步修正
定向搜索角度上限测试的右转输入。Debug、Release 单元测试各 129/129 通过，
`:robot:check`、`:robot:assembleDebug` 及修改文件格式检查通过。修正后尚待手机复测。

新 APK 仍为 `android/robot/build/outputs/apk/debug/robot-debug.apk`，大小 42,248,431 字节，
文件时间 `2026-09-03 14:13:28+08`，SHA256 为
`E77223696DA1620EC4F49D16CF622A022465AC2F339FFD284CDBF40F4C27FE72`。
未提交、未推送。

## 29. 模拟器帧过期与误重锁修复（2026-09-03）

### 29.1 手机复现与修改

复现日志 `cart_diag_20260903_142516`：慢帧检测平均 28.8 ms、ReID 36.1 ms，
处理平均 1094.3 ms，显示源年龄约 1106.8 ms。原目标相似度仍约 0.97，说明无框不能
直接解释为识别不到人。镜头转向用户指出的路人后，T1 被 T2 替代并输出模拟 `c21,21`；
Adaptive 始终为 0，因此不归因于动态 Gallery 污染。

- `TargetMemory` 增加模拟器专用有界采样，上、下各最多 4096 个像素；原 HSV 分箱与
  归一化不变，初始化和比较一致。`FollowStateMachine.onFrame` 接收同帧预计算匹配，
  模拟器不再重复遍历原尺寸人体区域。真实车保留原统计与调用配置。
- `SimulatorIdentityGuard` 统一控制模拟器身份授权。缓存不推进复核，3 次独立、新鲜、
  同 track 强 ReID 才可局部恢复；连续性不足、缺少原锁或已知干扰者要求重新采集确认。
  旧两帧重锁及全局自动重锁不再在模拟器生效，未授权候选不更新目标记忆或 Gallery。
- 同场明确分离的其他高置信 track 作为会话内干扰者排除；绿色表达授权后的身份，
  未授权候选改为黄色和 `c0,0`。确认按钮之外新增“重新采集并确认目标”，显式重建会话。
- 正常模拟器 ReID 调度也上限 300 ms，配合 500 ms 证据保鲜。`IdentityBeliefAccumulator`
  严格模式不再重复累计缓存 ReID 的贡献。其余轨迹几何证据仍可连续更新，但不能替代授权。
- 慢帧保留采集/确认/倒计时阶段提示，单列处理健康状态，不关闭 Start、不延长旧框寿命。
  过期结果停止模拟运动、冻结学习与重锁；搜索截止时间额外由 UI 定时轮询兜底。
- `frame_log` 追加 copy/legacy_match/initialization/decision 耗时；`frame_presented`
  事件追加日志提交、UI 等待、授权原因与复核次数，采集阶段也可追踪呈现耗时。

### 29.2 自动化与安装包

- Debug 和 Release 各 **145/145** 测试通过，0 失败、0 错误、0 跳过，包含原有真实车控制测试。
- 新测试覆盖有界采样与基准一致、同帧匹配复用、日志 T1/T2 数值序列、缓存与来源隔离、
  黄色候选绘制、过期确认面板及新鲜帧恢复、无新推理时搜索截止时间。
- 启用 Robolectric Android 资源加载，以普通 Application/API 28 测试真实布局，避免启动
  整个应用的硬件服务；这项配置只影响 JVM 测试。
- `:robot:testDebugUnitTest :robot:check :robot:assembleDebug --offline` 成功。
  项目 `abortOnError=false`；Lint 仍报告 10 个现有错误（Manifest、RecyclerView、真实车
  Slider API、control_buttons tint），涉及本轮未修改的文件，不能把 check 成功写成零错误。
- APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`；42,257,153 字节；
  修改时间 `2026-09-03 15:05:27+08`；SHA256：
  `93275DAA7E254F65D90E8291AB555874EE1322FA308C8DD6F3ED1F9A7E8F6737`。

已于 `2026-09-03 15:08:31+08` 通过 ADB 覆盖安装到 FOA_AL00，返回 `Success`，未卸载、
未清除应用数据。变更 Java 格式检查与两个仓库的 `git diff --check` 通过。
该安装包的手机结果与后续补丁见 29.3。满屏人物处理耗时是否持续低于 500 ms、
路人是否持续停车，需要跨场景复测。三次强 ReID 不保证绝无误识别，
干扰者 track 丢失后重建也可能失去原排除身份；保留人工确认与模拟器验证边界。
未修改 BLE、固件或真实车控制参数，未提交、未推送。原日志位于 ignored 的
`android/robot/build/diagnostics/cart_diag_20260903_142516/`，不纳入版本库。

### 29.3 手机复测与多人歧义补丁

首轮修复包实测日志 `cart_diag_20260903_151115` 已结束。191 条 `frame_presented` 采样
记录中，源帧年龄中位数 68 ms、P95 107 ms、最大 119 ms，超过 500 ms 为 0。这是该轮
采样记录的改善，不代表所有帧、所有场景均通过性能验收。

用户反馈：前几次转向路人正常，最后一次路人出现绿框，转回原目标未恢复。日志有
T1 -> T7 -> T9 锁定变化及非零模拟输出；缺少现场时间对应，不能仅凭编号指定哪次是路人。
身份验收未通过，相似衣着能解释 ReID 混淆，但不能作为错误身份获得运动许可的通过理由。

233 条身份日志中的 Gallery 原因：身份不足 71、多人物冻结 66、远处或歧义候选 41、
新鲜证据复核 28、样本重复 20、当前目标缺失 6、缺少锁定 1；Adaptive 最大数量为 0。
因此不是所有拒绝都来自图像质量，本轮也没有证据显示动态样本污染。

补丁在 `SimulatorIdentityGuard` 保存多人歧义历史，高低分候选均计入。多人出现后，新
track 即使未登记为干扰者且连续高分，也不能接管，保持停车并请求重新确认。只有原
locked track 连续 3 次单人新鲜强身份观测，或显式重建会话，才能清除歧义历史。
新增两个回归测试覆盖新 ID 路人和原目标清除歧义；不改变真实车控制或 BLE。

补丁 Debug、Release 各 **147/147** 通过，0 失败、0 错误、0 跳过；离线 `check` 与
`assembleDebug` 成功。现有 10 个 Lint 错误仍存在，不能将任务成功视为 Lint 零错误。
最新 APK 为原 debug 路径，42,389,016 字节，修改时间 `2026-09-03 15:21:22+08`，SHA256：
`5D459E9988BFCEF403C7B31B88F4CA98377C160F22D0E0FCFE47C3F2829316AB`。
已于 `2026-09-03 15:22:03+08` 覆盖安装到 FOA_AL00，ADB 返回 `Success`；未清除数据。
该补丁手机误接管复测尚未完成；同一 track 内身份交换仍是待验证风险，不宣称彻底解决。
新日志仅文本副本位于 ignored 的 `android/robot/build/diagnostics/cart_diag_20260903_151115/`。
本轮不提交、不推送。

## 30. 连续身份保持与 Recent Gallery（2026-09-03）

本轮保留既有未提交实验，只修改模拟器策略及共享代码中的可选入口，真实车保持默认路径。
上一轮统一身份授权会在低分时清空 Quarantine，造成转身后无法学习；本轮将保持目标、
允许运动与允许采样分离。

- 新增 `SimulatorContinuityTracker`，检查相邻框中心/宽高变化、3 帧连续观测及跟踪配对竞争；
  不以 track ID 相同代替连续性。保留源帧 500 ms 期限。
- `SimulatorIdentityGuard` 增加 VERIFIED/CONTINUITY_HOLD/ADAPTING/RECONFIRM：短时低分可
  保持关联，最多 1 秒低档模拟续行。驾驶控制器另查绝对截止时间，且停车后不能靠宽限重启。
  强证据恢复仍需 3 次新鲜观测，缓存与 Recent 不重置时限。
- `continuityFrame` 显式计算当前距离，只更新动态框，不改初始化尺度或确认外观。
  行为仲裁接收可选的连续性许可，保留硬安全、距离和前方人物阻塞检查，不伪造 ReID 分数。
- ReID 优先保留可见 locked track 的分数、特征与实际 upright crop；其他候选独立评分，
  缓存仍关联来源帧。`updateSimulatorGallery` 是页面和集成测试共用入口，低分停车不再阻断
  连续姿态隔离，多人和歧义仍冻结。
- 新增 16 特征/5 秒/300 ms 的 Recent 窗口，接受相似的新鲜强身份样本，top-3 均值仅辅助
  当前目标局部匹配；不替代 Anchor，不用于新 track 授权，不参与强身份续签。
- 近期记忆开关默认开启，Start 后锁定，固定 Gallery 不更新。仅开启诊断时额外保存
  被 Recent 接纳的真实 crop，文件名含源帧号；普通运行只持有特征，不保存每帧图片。
- frame CSV 在原有 timing 字段后追加 identity_state、retain_target、motion_permit、
  sampling_permit、hold_remaining_ms、continuity_reason、identity_reason、recent_enabled、
  recent_size、recent_score、recent_reason。现有列顺序不变。

测试包含实际授权到 Gallery 入口的隔离晋升、重复样本近期记忆、locked 非最高分时的 crop
归属、低档宽限不重启、硬安全优先和开关锁定。
手机验收尚未完成，不将自动化结果描述为已解决所有姿态/遮挡/相似衣着场景。

### 30.1 本地验证与交付

- Debug、Release 各 **163/163**，均为 0 失败、0 错误、0 跳过；包含真实车既有测试。
- 离线 `:robot:testDebugUnitTest :robot:check :robot:assembleDebug` 成功；相较本轮前 147 项
  新增 16 项测试。Lint 保留 10 个已有错误，均位于未修改文件；check 成功不代表 Lint 零错误。
- APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,607,204 字节；
  修改时间 `2026-09-03 16:09:31+08`，应用 ID `org.openbot`、v0.8.0/800。
- SHA256：`86A7C63F1E4F3E19CC9B426C16906925A004E3648A15A4CE59E2D9F112B2198A`。
- 本轮未安装新包、未完成手机场景复测，未提交、未推送。图片、诊断日志及 APK 均不纳入版本库。

## 31. Gallery 延后学习与静止自动重捕（2026-09-03）

### 31.1 当前口径与历史记录

本节是 Human Cart Simulator 当前实施口径，替代第 21 至 30 节中与之冲突的贴边拒绝、
仅凭候选自相似晋升、恢复必须 RECONFIRM、历史歧义永久阻断，以及普通等待/搜索超时结束
会话的实验规则。上述章节的测试数量、APK 哈希、安装记录和手机失败事实均保留为历史，
不表示本轮已经验收。真实车、BLE、固件、距离与速度参数不在本轮修改范围。

用户对上一构建的最新手机反馈是整体体验已有改善，但观察到 Gallery 长期为 8，长时间
离开后仍不能恢复。该数字未分清 Anchor、Adaptive、Recent、Quarantine，不能仅凭“8”
判定没有学习；长时返回失败仍是本轮需要复测的问题，不写成已解决。

### 31.2 几何与延后学习

- 四边贴合或裁切均不再单独拒绝采样，包括同时贴近左右边缘的大框。统一使用转正后 bbox
  与画面交集的可见尺寸：至少 32 x 64 px，普通高度比例 >= 18%，候选 >= 12%，可见
  宽/高 >= 0.15。裁切仅记录取景；持续侧移出画或身体变窄属于离屏/连续性风险，冻结学习。
- Recent 默认开启，最多 16 特征、源采集时间起 5 秒、源时间采样间隔至少 300 ms；可靠的
  相似样本仍可写入。固定 Gallery 不更新动态区域，Recent 不参与远处或新轨迹身份授权。
- 新增纯辅助类 `DeferredGallerySegment`。只有此前验证过的原目标，在单人、高置信实测框、
  可靠局部关联、无竞争且尺寸合格时，才能以原始分数 >= 0.60 且 < 0.85 开启候选片段。
  相邻新鲜特征 cosine >= 0.85；不一致、多人、关联歧义、轨迹交接或 > 500 ms 中断废弃本段。
- 片段最多 16 个特征，整段从首个源时间起最多 5 秒，采样间隔至少 300 ms。保留 session、
  track、观测 ID、源帧、源时间、bbox、独立分数及 Gallery 版本。开启时深复制 Anchor 与
  具备全局资格的既有 Adaptive；之后新增的样本、Recent、本段候选都不能用于自证。
- 批准必须同时满足累计至少 5 个新鲜样本、源时间跨度至少 1.5 秒，以及弱片段之后连续
  3 个不同的新鲜强端点；每个端点冻结快照分数 >= 0.85、Anchor >= 0.70、margin >= 0.08。
  缓存、持续绿框、单次高分或纯自相似不能晋升；身份门仍在计数也不阻止独立端点被复核。
- 批准只回溯学习，不授予当前帧身份或运动许可。仍有效样本按原采集时间进入 Recent；
  Adaptive 保持最多 8 项与确定性多样性重选，延后样本仅支持原轨迹局部姿态，不进入全局
  身份分数。新样本最早从下一帧参与匹配。旧 Quarantine 自相似晋升入口不再旁路放行。
- 多人、丢帧等冻结后需重新取得可信原目标，不能马上用弱分样本重新开始片段。非 ReID 帧
  不因没有新特征而清空片段。日志默认关闭，不创建诊断目录/CSV/crop；内存学习不依赖日志。
  仅日志开启时保留实际源帧 crop，延后接纳不能以批准帧图像替代旧样本。

### 31.3 静止恢复与离屏

- 首次采集确认及用户主动重拍保留；恢复阶段采用 `AUTO_VERIFY`，局部仍需 3 次新鲜强证据。
  长时丢失或新轨迹采用全局静止验证：同一候选连续 5 次新鲜证据、跨度 >= 1200 ms，
  每次分数 >= 0.85、margin >= 0.08、Anchor >= 0.70，相邻证据间隔 <= 500 ms。
- 全局只用 Anchor 和全局合格 Adaptive。多人/当前歧义暂停或重置，候选变化或证据失败
  重新计数；历史歧义不再永久锁死，已知干扰者仍禁止接管。候选预算最多 5 个，高低置信
  均参与竞争检查，但只有高置信候选可授权；超预算保持停车，不能声称已排除其余人。
- 普通等待 2/5 秒、定向搜索绝对期限或角度上限结束后进入 `PARKED_WAIT`：模拟 `c0,0`，
  保留 Start、Anchor 和已接纳 Adaptive，继续静止等待。不强迫重拍，不重复自动旋转；
  必须先重新获得可信跟随，再形成新的离屏事件。用户关闭 Start、退出页面和硬安全异常
  仍结束会话。保留记忆不延长旧帧证据、宽限或运动许可。
- 离屏历史使用最近 800 ms 实测框，最后强身份年龄 <= 1 秒；连续保持不刷新强身份时间。
  至少 2 次位置观测、累计向外 >= 画面宽度 2%、进入对应侧 15% 区域，允许一次 < 1%
  中性抖动，明显反向/歧义/过期清除。宽框同时贴左右边缘时结合中心、边缘和宽度变化判向。
- 目标连续至少 2 帧、100 ms 无高低置信续接才判失联；有其他未排除人物不旋转。候选出现
  立即停车复核，搜索/验证共用原截止时间，普通等待不能提前终止 10 秒定向搜索。
  已跟随会话恢复不重复首次 3 秒倒计时；旋转后仍保留 300 ms 停车稳定。

### 31.4 验证状态

本轮 Debug、Release 各 **270/270**，0 失败、0 错误、0 跳过；较第 30.1 节的 163 项增加
107 项。离线 `:robot:testDebugUnitTest :robot:check :robot:assembleDebug` 全部完成，Java
格式检查及两层仓库 `git diff --check` 通过。保留
10 个已有 Lint 错误，check 成功不代表 Lint 零错误；真实车控制文件、BLE、固件没有改动。

- APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,384,958 字节。
- 文件时间：`2026-09-03 17:45:07 +08:00`。
- SHA256：`24B03C1DB683E2BF6D50667BF62844D8667048C30C09A75913897F01ACF23B31`。
- 本轮未安装手机，未提交、未推送。手机端处理耗时、识别与离屏效果待实测，不能以桌面
  测试通过替代手机验证。步骤见主仓库 `design/模拟器离屏重捕与Gallery采样测试指南.md`。

集成回归还核对了首次倒计时、长时返回新轨迹、独立分数与 crop 归属、已知干扰者拒绝、
迟到 UI 不恢复搜索输出、历史 crop 像素与源帧一致。超过 3 秒没有强身份验证时，即使
tracker 仍复用了原 ID，也按全局验证处理，不能靠旧 ID 绕过长时恢复门。

另补三项调用链保护：当前 locked 检测刚刷新的 ghost 位置不能反过来作为历史空间支持；
相机接纳时固定 ReID 会话编号，重置前已开始或尚未执行的旧特征任务均不得写入新会话；
当前帧先更新离屏历史再判断 Gallery 学习，后续显示不重复推进同一帧的搜索计数。
会话检查只在短临界区内进行，神经网络提取不持有该锁，避免退出页面被推理阻塞。

frame CSV 在原有列后增加 `anchor_count,adaptive_count,quarantine_count,deferred_review,`
`recent_matching_support,recovery_type,recovery_matches,recovery_required`。
重点记录四边贴合、黄框后恢复、长时返回、相似路人交叉、快速左右离屏的样本接纳、停车与
恢复耗时、搜索触发和错误身份输出；Gallery 数量增长不能独立作为成功指标。未提交、未推送。

## 32. 真实车动态 Gallery、自动调档与定向重捕迁移（2026-09-03）

### 32.1 范围与身份

用户反馈上一模拟器版本整体体验改善，但未完成专项验收。本轮接入真实车仍属于受控实验，
不代表整车身份或搜索安全已经验证。保留既有本地成果；不修改固件、BLE 报文、手动遥控
参数或距离标定，不提交、不推送。

`FollowPolicy` 分开增强身份、动态 Gallery、连续保持运动与搜索权限。两页共享同帧检测/
ReID、低置信续接、历史空间关联、500 ms 感知期限、会话隔离和身份验证。
真实车默认动态 Gallery + Recent，Start 前可对照固定/动态和近期记忆，启动后锁定。
四区、尺寸门、冻结快照和延后复核沿用第 31 节，不降低身份阈值。

真实车只在 `VERIFIED` 获得前进授权。`CONTINUITY_HOLD / ADAPTING / AUTO_VERIFY` 零输出，
但保留可靠关联和候选学习。模拟器原有 1 秒低档宽限保持不变，未迁入真实运动。
局部恢复需 3 次新鲜强证据；全局需 5 次且跨度至少 1.2 秒，保持 Anchor/margin、多人及
已知干扰者限制。普通无人 2 秒静止等待，保留 Start、Anchor、已接纳 Adaptive，不强制重拍。
首次确认保留倒计时，已跟随会话恢复不重复倒计时；硬安全异常仍结束会话。

### 32.2 自动调档与发送门

- `AutoGearSelector` 提取原模拟器距离分档、0.03 滞回与 3 帧升档，模拟器阈值不变。
- 最高档首次默认 21，可在 Start 前选 14/18/21 并保存。起步或恢复要求有效 TOO_FAR、
  heightScale <= 0.80、3 个不同新鲜帧，先输出 c14；降档立即执行。
- 转向 >30% 最高中档，>60% 或可信 FOLLOW_CAUTION 只用低档。强度 20%–200% 保持原保存值。
  内侧 `max(6, gear-round(delta*demand/100*strength/100))`，三档 delta 为 4/5/6。
- 显式区分 FOLLOW/PIVOT/STOP，正常跟随逐轮正向 <=21，仅搜索许可可产生等幅反向命令。
- 决策和每次 100 ms 非零提交前核验连接、握手、前台、Start、解锁、会话与动作许可。
  非零要求源帧年龄 <=400 ms，不用处理完成时间续期；过慢帧不能起步，运动超时撤销解锁。
- 同向缓弯/变速不插入 STOP；模式切换、Start 关闭、退后台、失焦、断连和急停仍优先停车。

### 32.3 定向搜索

`RealCartSearchController` 在共享离屏状态机外增加真实运动门，不直接转发模拟器轮速。
页面每次进入搜索开关关闭；Start 前可选档位 5–21、角度 30°–180°、总时间 1–10 秒，首次
默认 5/90°/5 秒。数值保存、开关不保存，启动后锁定，手机必须与车体固定。

可信侧边离屏且没有其他候选才可搜索。先提交 STOP，至少 300 ms 零指令后允许旋转；
总期限从接纳离屏事件计时，包含软件等待、固件制动与验证。提交/GATT 成功不等于物理停稳。
真实车第一次提交 PIVOT 才重置测角，制动阶段不消耗目标角度，验证暂停继续积分；恢复搜索
不清零。陀螺仪与重力必须存在、注册成功且最近 500 ms 有效，不允许时间降级。开始前不可用
则静止等待，运动中失效或净反向 >=5° 则停车并撤销解锁。

候选出现立即停车验证，持续错误候选不触发转/停抖动。恢复后稳定停车至少 300 ms；角度或
总时限耗尽进入 PARKED_WAIT，不重复搜索。定时调度器独立检查，不依赖下一帧推理才停车。
搜索进入只消费一次新事件，不能每帧清空状态机恢复计数。

### 32.4 显示、诊断与验收

真实页显示四区、身份/恢复进度、档位、实际输出和原因，绿色身份与运动许可分开。
转弯标定放入滚动设置区；紧凑横屏滚动辅助面板，保留 Start 与急停入口，手动布局不变。
帧 CSV 末尾追加 `real_intent,real_phase,real_gear,real_left,real_right,real_reason`，不改变旧列。
这是决策而非电机反馈，应结合 CartControl、BLE 写入与现场记录检查实际执行。

当前推荐 `firmware/esp32_at8236_velocity_ble` 速度闭环固件，不套用旧 trim/kick 说明。
当前名义侧速度 c14/c18/c21 约为 160/206/240 mm/s；纯旋转 c5 有 80 mm/s 最低保护。
步骤见主仓库 `design/真实小车动态跟随与定向搜索测试指南.md`：关闭搜索悬空检查，再三档
落地跟随，再核对手动左右与测角，最后 c5 定向搜索并逐档验证。

### 32.5 最终本地验证

- Debug、Release 各 **306/306**，0 失败、0 错误、0 跳过；相对上一轮 270 项增加 36 项。
  包含真实身份调用链 4 项、真实迁移/调档 11 项、搜索门 15 项及配置/UI 6 项。
- `:robot:testDebugUnitTest :robot:check :robot:assembleDebug --offline` 完成，Java 格式检查及
  两层仓库 `git diff --check` 通过。搜索恢复测试经过候选验证、300 ms 稳定和低档起步门；
  UI 检查包含 360 x 720、720 x 360 布局与模式恢复，不等于手机截图或真机操作验收。
- Lint 保留 **10 个已有错误**：MissingClass 2、RecyclerView 1、RestrictedApi 4、UseAppTint 3。
  RestrictedApi 是原有手动/转弯强度 Slider 监听接口；本轮没有新增错误项。项目当前设置下
  check 成功不代表 Lint 零错误，另有原有 Kotlin/Java target 和弃用 API 警告。
- APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，**42,461,795 字节**。
- 文件时间：`2026-09-03 19:01:54 +08:00`。
- SHA256：`444448A135FAE9BED63A25051012F985C85328BBAB084BA558AED77735FCD50B`。

没有安装新包、没有操控实车、没有提交或推送。手机处理耗时、制动距离、搜索过冲、误身份
非零输出尚未验收；不能将桌面单元测试耗时当作手机完整流程帧率。使用上述哈希区分本地
未提交构建，不只依赖界面中未改变的短提交号。图片、模型、诊断输出与 APK 不进入版本库。

## 33. 连续远离时身份维持修复（2026-09-03）

### 33.1 范围与机制

本节更新第 32 节的真实车身份运动口径：两页均允许可靠连续目标低档维持，不再把所有
非 VERIFIED 一律视为禁止运动。未使用旧 1 秒弱身份宽限，不改变固件、BLE、手动控制、
距离标定、强授权三档或搜索速度。此前本地改动全部保留，不提交、不推送。

代码确认原维持门复用 0.85 强验证阈值，且超过 3 秒无强授权会切换全局；部分几何证据
依赖最后授权时的 bbox。现场首次降级原因尚无本轮日志，不能宣称已确认全部现场根因。

连续性改用同会话、同 track 的更新前实测历史，检查相邻中心、宽高变化及三次稳定观测；
身份/距离不允许运动时仍可更新可靠单人实测历史。ReID 的 locked 几何也使用 track 更新
前保存的框，不从刚刷新的 ghost 自证。新帧对象、旧会话迟到、空间突跳与竞争均有回归。
原确认外观和距离 setpoint 不变。真正观测中断超过 3 秒、新 track 或空间断裂仍全局验证。

### 33.2 许可、学习与运动

新增 TRACK_MAINTAINED，authorized 字段仍仅代表强身份。首次实验门槛为独立 best >=0.80、
Anchor >=0.70、margin >=0.08；只适用于已经强验证并进入过 FOLLOW 的原目标，且单人、
高置信、无丢帧/竞争、连续几何可靠。Recent 和隔离候选不提供维持授权。

从强授权可直接降为连续维持；不足门槛立即停车但保留可靠学习上下文。三次新鲜维持证据
可恢复低档，三次原有强证据才恢复强授权。缓存不刷新许可时间/计数。多人歧义、低分续接、
已知干扰者、track 交接取消资格；多个分离人物时原目标的严格强验证及干扰者记录仍保留。

两页仅允许 c14 及现有低档差速；停车起步的三次证据可与身份复核同帧满足，不叠加倒计时。
距离、方向、障碍、会话门继续生效。真实车新决策及重发保留 400 ms 源帧期限，并另查
500 ms 身份证据时效。连续维持不刷新搜索的强身份时间、不批准自己的 Gallery 候选。

绿框标注“连续跟踪 · 低档”，强验证标注“身份已验证”，停车显示具体不足门槛。真实页
增加独立身份分数、Anchor、margin、连续性原因；帧 CSV 末尾追加 maintenance_evidence_ms、
maintenance_observation_id，原字段顺序不变。已有身份 CSV 保留实际可见像素尺寸和采样原因。

### 33.3 本地验证

- 原记 Debug / Release 各 315 项、较 306 项新增 9 项；该数量与接续时存留的各 277 项报告不符，现撤回本条验收数量。当前重新执行的结果以第 35 节为准。
- 新增回归包含：12 秒连续縮小的真实检测/ReID/状态机/Gallery/双页控制调用链；
  0.84/0.82/0.81 的持续局部维持；低分、Anchor、margin 拒绝；缓存不续期；
  旧会话、同 ID 突跳和竞争；第三次新鲜维持证据同帧恢复起步；重发独立检查身份期限。
  原有路人、全局恢复、定向搜索、手动控制及生命周期测试继续通过。
- `:robot:testDebugUnitTest :robot:check :robot:assembleDebug --offline` 通过。
  Java 格式检查、`git diff --check` 通过；Lint 仍为已有 10 项错误：MissingClass 2、
  RecyclerView 1、RestrictedApi 4、UseAppTint 3，check 通过不代表零 Lint 错误。
- APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，**42,299,205 字节**。
- 构建文件时间：`2026-09-03 19:54:05 +08:00`。
- SHA256：`A0642616F82E6B7A1D6B0166C9A1A6B8E3552BA1867F64DAF73D1BB5F10EF799`。

手机尚未安装，本轮无实车操作；手机处理耗时、黄绿切换次数、制动与误身份输出仍待测。
专项步骤见主仓库 `design/连续远离身份维持复测指南.md`。自动化通过不等于真机身份验收通过。

## 34. 单人连续跟随优先与全局动态 Gallery（2026-09-03）

第 33 节的 `TRACK_MAINTAINED` 仍把 Anchor 分数和旧图库分数当作连续目标的逐帧否决条件，
实际手机测试中会让远离、侧身、转身的同一人反复落入黄框。本节将“持续跟踪已确认目标”和
“重新认出已经丢失的目标”明确分开：只有消失、track 交接、空间跳变、多人竞争或已知干扰者
才进入严格的 `REACQUIRE_VERIFY`。

### 34.1 单人连续身份

已经强验证并进入过跟随的原目标，在单人高置信实测框、同一 locked track、历史空间连续且
没有竞争时，进入 `TRACK_STABLE` 或 `APPEARANCE_TRANSITION`。局部外观支持使用本帧相邻
特征相似度或 Recent top-3 分数，门槛为 0.75；不再要求 Anchor >= 0.70 或旧图库 >= 0.80。
一次不支持显示“外观变化中”并限低档，连续两次新鲜不支持才停车进入 `ADAPTING`。缓存不能
计作新鲜证据或续期。

`TRACK_STABLE` 可按距离、转向和三帧升档门使用 c14/c18/c21；`APPEARANCE_TRANSITION` 只允许
c14 与同向低档差速。真实车仍要求源帧 <=400 ms、BLE/会话/距离/安全门全部有效；停止后仍先低档
起步。绿色只表示身份连续，实际轮速与停车原因仍独立显示。

### 34.2 动态记忆与全局恢复

Anchor 保持只读。Recent 保留 16 项、5 秒、300 ms 间隔，使用 top-3 支持当前 locked track，不能
确认新 track。连续单人片段在先匹配、再决策、最后写入的顺序下，收集 3 个不同新鲜样本并持续
至少 600 ms；相邻特征相似度至少 0.75，即可回溯写入 Adaptive。四边贴合仅为取景诊断，采样只
要求转正后至少 32 x 64 px 且宽高比至少 0.15。离屏、多人、关联断裂立即冻结片段与 Recent。

这是有意的实验取舍：连续姿态的新样本可获得全局 Adaptive 资格，不必回到初始姿态；因此仍要
单独检查相似路人是否被误入库。全局恢复可使用 Anchor 或已批准 Adaptive 的 best，但仍需 5 次
新鲜证据、至少 1.2 秒、best >=0.85、margin >=0.08；已知干扰者和当前多人歧义不得接管。

### 34.3 本轮边界

不修改固件、BLE、手动控制、距离标定或最高自动档 c21，也没有安装 APK、操控实车、提交或推送。
手机复测应先在模拟器完成“静止 30 秒、远离返回、连续转身/侧身、路人经过、遮挡换人”，再依次
进行真实车悬空和空旷低速测试。任何错误身份的非零输出或路人进入 Adaptive 都单独记为失败，
不能以 Gallery 增长或跟随更流畅抵消。


## 35. 连续跟踪主导与手机离线日志（2026-09-03 接续实现）

本节替代第 34 节中仍以局部外观分数否决单人运动的规则。本次接续时工作区未提交修改保留；
实际存留报告为 Debug/Release 各 277 项，第 33 节和旧复测指南的 315 项不能作为接续版本的验收基线。

### 35.1 跟踪、恢复与记忆

- 新增共用 `TrackingDecision`，将实测位置/帧/会话、检测等级、运动、学习与档位上限独立于强 ReID。
- locked target 唯一连续时不再等待 Gallery 或新鲜 ReID；双向配对差距 <0.15 判竞争，中心/预测门
  使用画面对角线 0.35/0.18，预测使用真实时间差。尺寸只参与关联评分。
- 低置信 >=min(0.25,高阈值) 可续接且最高 c18，不学习。正常连续恢复 c14/c18/c21，保留距离和转向限制。
- 漏检立即取消前进；500 ms 内唯一返回，三帧实测恢复。位置帧必须与控制结果的源帧和会话完全匹配。
- 多人唯一关联继续运动、冻结学习；最多五人、500 ms 核验周期，两次显著身份冲突或超过 1 秒未完成核验停车。
- 全局重捕五次新鲜证据、至少 1.2 秒、best>=0.85、margin>=0.08；批准的 Adaptive 可独立提供 best。
- 连续片段三样本/600 ms/相邻 cosine>=0.75 批准全局 Adaptive，独立保存来源与 globalEligible，批准帧不能自用。
- 状态机、仲裁器、起步门和两页控制器同时修复；首次确认、距离标定、400 ms 实车源帧、硬安全与定向搜索边界保留。

### 35.2 手机离线记录

打开日志即记录，文字/图片分开有界排队、每秒刷新文字。关闭停止收新数据、异步写完已接收内容；
Start 关闭先提交车辆停止，再最多等待 500 ms 收集最后的 BLE 回调。关闭日志开关立即结束观察。
原 CSV 列保留，新增控制日志、样本来源清单、构建/策略信息、结束摘要与关键场景图。页面显示丢弃/错误。
两页“测试记录”可在停止后保存 ZIP 到系统文件选择器指定位置，或系统分享；异常中断记录可导出。
没有修改 BLE 协议、调度决策或固件；新增队列替换事件仅作旁路观测。GATT 成功不代表电机执行。

### 35.3 自动化与交付

- JDK：`C:\Users\ysyys\.jdks\jbr-17.0.14`（17.0.14）。
- 本次重新执行 Debug / Release 各 **293/293**，每个变体 42 个测试类，0 失败、0 错误、0 跳过。
  接续时存留各 277 项；新增 16 项，原测试按新行为要求修订，未通过删除测试或改成停车断言来取得通过。
- `:robot:testDebugUnitTest :robot:testReleaseUnitTest :robot:check :robot:assembleDebug --offline` 全部通过。
- 77 个改动/新增 Java 文件通过 google-java-format 1.7 格式检查；主仓库、子仓库 `git diff --check` 通过。
- 最终 Lint 报告仍有 **10 项 Error、670 项 Warning、6 项 Information**：原有 MissingClass 2、RecyclerView 1、
  RestrictedApi 4、UseAppTint 3。本轮新增的 API 21 兼容问题已修复。项目 `abortOnError=false`，
  所以 check 通过不代表 Lint 零错误。较早一轮还出现 Timber 4.7.1 检测器与 AGP 7.4.2 的 LintError，
  最终报告未重现；该工具兼容问题仍需留意。Java 17 / Kotlin 1.8 target 与旧 Gradle API 的构建警告保留。
- Debug APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，**42,418,640 字节**。
- APK 文件时间：`2026-09-03T22:11:04+08:00`。
- SHA256：`55EBEB670CCBFD64C6EC846C7C764CECA4E0B44392DE3A48BA91E2547CAF6B3A`。
- 本次输出：`android/robot/build/continuity-validation.log`；测试 XML 在 `robot/build/test-results/`，
  Lint 报告在 `robot/build/reports/lint-results-debug.html`（构建产物不提交）。
新增测试检查 30 FPS/200 ms ReID 交错输入、真实非零恢复、低置信中档、全局 Adaptive 实际匹配、多人检查、
缓存来源不变、延迟 BLE 回调源帧、存储失败、有界队列、异步收尾、ZIP 内容及系统文件选择器取消；
原硬安全及干扰者回归保留。导出取消通过 Robolectric 界面路径验证，仍需手机实测系统文件提供方。
未安装手机、未操控实车，手机帧率、误跟、导出选择器与真实停车响应仍待现场复测。
主仓库和子仓库均在 `codex/continuity-offline-diagnostics` 分支，本轮不提交、不推送。

## 36. 现场阶段结论与下一轮日志复测（2026-09-04）

本次完成连续跟踪主导与手机离线记录的代码合并前整理。已观察到的现场结论是：单人始终
可见时，远离、侧身或短时 ReID 低分不再像旧版本那样频繁把目标降为黄框，跟随连续性已有
改善。这是体验观察，不是对所有场景的通过声明。

当前仍未解决的行为必须单独记录：

- **货架拐角**：普通跟随只有同向低速差速缓弯；目标在拐角离屏后，系统没有已验证的转角
  路径推断或通道选择能力，不能宣称会绕角继续跟随。
- **目标走到车后**：普通跟随禁止倒车和原地旋转。仅当满足既有可信侧边离屏、传感器和安全
  条件时，才可能进入受限的定向搜索；这不是“人走到身后即自转”的能力，且尚未实车验收。
- **真实执行**：GATT 写入成功只表示手机写入成功，不能证明车轮执行、制动距离或转向方向正确。

下一轮手机测试打开“记录日志”，每段保留完整 ZIP，不删原记录：直线远离/返回、缓弯、
拐角离屏、目标从前方绕到身后，以及路人交叉。复盘时至少对照 `frame_log.csv` 的 tracking/
实际档位/限制原因、`control_log.csv` 的请求与 GATT 写入、`identity_log.csv` 的候选与 ReID
来源、`summary.json` 的丢弃和写入错误。现场视频或人工记录要标注是否悬空、地面、搜索开关
和最高档位；没有这些证据，不把“未转向”归因于身份策略，也不把“已转向”归因于车轮实际执行。

本节前的 Debug/Release 各 293 项自动化结果仍有效，但不覆盖上述物理行为。明天的实测和
导出日志将决定转弯/身后问题是视觉侧缺少候选方向、控制映射不足、BLE/底盘执行差异，还是
安全门按预期停车；在证据出来前不扩大自动旋转或拐角试探范围。

## 37. 单人被原始低置信假框误判为多人（2026-09-04 现场修复）

手机记录 `cart_diag_20260904_114737_602_2fc3290f` 显示，目标静止且画面中实际只有一人时，
199 帧中有 155 帧只有一个高置信人物，但其中 136 帧还带有原始低置信 proposal。locked T1
始终可见、无 missed，ReID best 约 0.85～0.98，目标配对分差约 2.68，仍有 108 帧被
`multi_check_timeout` 阻断。根因是 ReID 只评估“高置信 + 已续接低置信”候选，身份门和 Gallery
却使用“高置信 + 所有原始低置信”的人数；未被跟踪、也没有 ReID 分数的假框因此被当成第二个人，
核验永远无法完成。

本轮新增不可变 `IdentityCandidateSet`，只包含绑定当前实测轨迹的高置信检测及成功续接既有轨迹的
低置信检测。ReID、多人身份门、目标观察和 Gallery 现在共用该集合。原始低置信 proposal 仅显示和
记录，不能触发多人超时、候选超预算、学习冻结或运动阻断；真正的多轨迹候选、关联竞争、漏检、
过期帧、断连和急停规则保持不变。新增 `candidate_log.csv`，逐候选记录 tier、bbox、轨迹绑定、
身份资格、关联分数和竞争状态；`frame_log.csv` 末尾追加有效候选数、多人状态和首要限制原因，
日志版本升至 3。

使用 JDK 17 完成 Debug/Release 各 **296/296**、43 个测试类，0 失败、0 错误、0 跳过；
`:robot:check` 和 `:robot:assembleDebug --offline` 通过，修改文件通过 google-java-format 与
`git diff --check`。新增 10 秒等效回归持续输入一个真实目标和六个未绑定低置信假框，要求始终
允许运动、保留学习资格，且不出现 `multi_check_timeout` 或 `candidate_budget_exceeded`。
Lint 报告为 11 Error、670 Warning、6 Information：原有 10 项外，Timber 4.7.1 检测器与
AGP 7.4.2 的已知 `LintError` 再次出现；本轮文件没有新增 Lint 条目。

Debug APK 为 `android/robot/build/outputs/apk/debug/robot-debug.apk`，42,327,634 字节，SHA256
`BA4CFE0DD5D45C4F860EB897160A93540B07EDDFD21A8186F7A1DCD00A593644`。安装前已备份手机现有
6 份记录；2026-09-04 12:24 使用 `adb install -r` 覆盖安装成功，应用数据和原记录保留，主界面
冷启动通过后已强制停止，未启动跟随或向车辆发送控制指令。

本轮桌面验证只证明“未续接低置信假框不能冒充多人”。远距离检测完全丢失、真实车轮响应、货架
拐角转弯及目标走到车后需要自转仍未解决，必须用新日志做手机和实车复测后再判断。

## 38. 持续对准、绕后连续性重现与距离倍率（2026-09-04）

本轮把前进距离控制与摄像机朝向控制拆成 `TranslationDecision` 和 `AimDecision`。目标距离合适或
视觉太近时只禁止前进，不再禁止原地对准；误差 0.18 进入、连续三帧回到 0.08 退出。远距离在
0.35／边缘区改为纯 `c5` 原地转向，其余仍使用直行或现有差速缓弯。真实车和模拟器控制器均覆盖。

定向搜索默认改为启用、`c5 / 180° / 10 秒`，保留 300 ms 制动和陀螺仪角度限制。搜索中的人物
先暂停 500 ms；唯一候选从原离屏侧外侧 25% 入镜、三帧且至少 100 ms 向中央移动、关联唯一且
没有强身份冲突时，可按连续性完成低 ReID 背面重现。恢复后先居中再判断前进，Gallery 到稳定居中
前保持冻结。错误侧候选不会接管，暂停后继续原方向搜索。

距离确认现在收集至少 15 个高置信框并覆盖 500 ms，使用高度中位数；明显顶部／底部裁切会重置
标定。距离状态以人物框高为主，面积分歧只保留诊断。新增持久化“视觉最远距离倍率”1.05～1.40，
默认 1.10，返回滞回为倍率减 0.08（最低 1.00）；开始跟随后设置锁定。逐帧日志末尾新增朝向和
平移许可、中心误差、档位上限及原因，原有定向搜索方向、累计角度和原因继续记录。

根目录新增 `design/购物车Android-ESP32通信协议V2草案.md`，版本 `V2-DRAFT-0.1`。本轮未修改
Android BLE 协议、ESP32 固件或侧移界面；左右红外和中央超声波仍未接入。当前原地旋转没有后侧
防撞覆盖，货架拐角的路径／通道选择也未实现。桌面验证不能替代手机、悬空和空旷落地测试，现场
必须低档、留出完整旋转空间并由人员准备物理断电。

### 38.1 桌面验证与交付物

- 使用 `D:\Java\jdk-17`（17.0.12）执行最终构建。
- Debug／Release 各 **306/306**，44 个测试类，0 失败、0 错误、0 跳过。新增覆盖包括距离合适／
  太近时原地对准、远距离边缘纯旋转、三帧居中退出、同侧背面低 ReID 连续性重现、错误侧候选
  暂停后继续搜索、距离倍率滞回、15 帧／500 ms 中位数标定和裁切提示。
- `:robot:testDebugUnitTest :robot:testReleaseUnitTest :robot:check :robot:assembleDebug --offline`
  全部通过；改动 Java 文件通过 google-java-format 1.7 dry-run，主／子仓库 `git diff --check` 通过。
- Lint 报告仍有 **10 Error、669 Warning、6 Information**。Error 为既有 MissingClass 2、
  RecyclerView 1、RestrictedApi 4、UseAppTint 3；项目 `abortOnError=false`，因此 `check` 通过不表示
  Lint 为零。另保留 Java 17／Kotlin 1.8 target 不一致和旧 Gradle API 警告。
- Debug APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,705,928 字节，SHA256
  `797D8D592E3D16A30F06115CE59AA1631AB58F81841D2092AA8E65C45FA7DA63`。

本轮没有安装 APK、启动自动跟随或向车辆发送指令。模拟器、手机、车轮悬空和空旷落地仍按新版
测试指南待测；V2 协议仍只是草案。

## 39. Start 后目标一直白框、无法拍照确认修复（2026-09-04）

持续距离标定接入后出现采集回归：推理管线传给状态机的 `persons` 已经是达到当前应用高置信阈值
的候选，但状态机又硬编码要求 `confidence >= 0.75`。当用户设置的检测阈值低于 0.75 时，画面可正常
显示白色人物框，候选却会被采集状态机持续拒绝，无法进入 `LOCKED_PENDING_CONFIRM`，因此不会显示
快照和确认按钮。

现已移除重复的 0.75 门槛，采集直接信任上游配置的高置信候选集合；低置信续接列表仍不会进入首次
采集。15 帧／500 ms 距离标定保持不变。采集中的主候选改为黄色框并标注“距离标定中”，主提示显示
当前样本数和覆盖时间；人物顶部／底部裁切时显示重新站位提示。真实车页面不再用固定“正在采集”
覆盖这些原因。

新增 0.60 置信度但已由上游接纳的采集回归。JDK 17 最终 Debug／Release 各 **307/307**、44 个测试类，
0 失败、0 错误、0 跳过；`check` 和 Debug APK 构建通过。APK 为
`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,337,153 字节，SHA256
`5FE7A4898E3FBF944FAE7D8A4EA36AB1516F411F56F692B767AE1E17F1EF479B`。构建结束时 `adb devices`
没有列出设备，因此未覆盖安装到手机。

## 40. 目标确认与距离标定解耦（2026-09-04）

第 39 节只移除了重复的 0.75 置信度门，仍未解决初始化困难。代码对照确认，15 帧／500 ms 距离
标定及人物框裁切检查被错误放在确认面板之前；单帧漏检、低置信或顶部／底部触边会清空全部样本，
导致画面仍有人物框但状态机迟迟不能进入 `LOCKED_PENDING_CONFIRM`。

本轮恢复快速目标确认：同一实测轨迹在最近 2 秒取得 15 个高置信检测即可生成快照；短于 500 ms
的检测间断保留进度，轨迹改变或目标消失超过 500 ms 才重新采集。拍照阶段只保存目标外观、确认框
和 ReID 初始化样本，不再从单帧写入距离基准，也不要求人物框完整。

用户确认后新增 `DISTANCE_CALIBRATION` 状态。该状态只接受已锁定轨迹、唯一、高置信且未明显裁切
的新鲜实测框，仍要求至少 15 个有效样本和 500 ms 跨度并使用人物框高度中位数。无效帧只丢弃
本帧，不清空已接收样本；完成前模拟器与真实车仲裁链均保持 `c0,0`，完成后才进入既有重识别、
倒计时和跟随流程。界面区分“目标采集中／待确认／距离标定”，确认后的标定目标显示绿框及样本
进度。低置信框继续只作浅灰诊断，不参与首次确认或距离标定。

`frame_log.csv` 保持原列顺序并在末尾追加初始化样本数、初始化轨迹、丢弃原因、距离标定样本数和
完成时间；日志版本升至 5。新增回归覆盖短时漏检保留进度、轨迹切换和长间断重置、裁切帧不清空、
标定完成前两套控制器零输出、确认面板不被标定阻塞及新日志字段。

使用 `D:\Java\jdk-17` 完成 Debug／Release 各 **311/311**、44 个测试类，0 失败、0 错误、
0 跳过；`:robot:check`、Debug APK 构建、google-java-format 1.7 dry-run 和 `git diff --check`
通过。Lint 仍有 **11 Error、669 Warning、6 Information**：MissingClass 2、RecyclerView 1、
RestrictedApi 4、UseAppTint 3，以及 Timber 4.7.1 与 AGP 7.4.2 的已知 `LintError` 1；本轮未新增
对应代码问题。

Debug APK 为 `android/robot/build/outputs/apk/debug/robot-debug.apk`，42,531,340 字节，SHA256
`9808BCDECF2015F46897DAC273532162108D6E282D1867ABF737FBEC6FC86F0C`。构建结束时
`adb devices` 没有列出设备，因此未覆盖安装、未启动跟随、未发送车辆控制指令。手机确认交互、
实际标定耗时和实车起步仍待连接设备后复测；拐角转弯、绕后自转和 V2 传感器协议不在本轮范围。

## 41. 传感器版 V1 协议适配与 Android 二次门控（2026-09-04）

同步主仓库后确认，当前 ESP32 并未实施 V2 `m/d/g/a`，而是通过
`fCART_AT8236:s:`、`s100` 和 `s<cm>` 接入左／中／右三路测距。本轮按实装协议开发，取消
侧移。BLE 与 USB 接收增加换行分包／粘包重组，连接清理时丢弃残留半包；Vehicle 保存能力、
三路有效最小距离、单调接收时间、序号和最近 `!ERR`，不再把旧 `SensorReading.age` 当当前年龄。

真实车手动、自动和定向搜索共用 `RangeSafetyGate`：前进 300/400 mm、转向 200/300 mm，
解除均要求 3 个新样本；超过 250 ms 未更新、未声明能力或未收到首帧时禁止前进和转向。停车、
急停始终有效，后退保持可用并标注没有后侧覆盖。由于 V1 只给最小值，Android 无法按来源做
方向判断；ESP32 的单路状态、方向门控和制动仍是最终依据。

实车状态区与完整调试面板新增协议、最小距离、age、fresh、门控锁存、限制原因和固件错误。
日志版本升至 6，`frame_log.csv` 末尾追加相同字段，`control_log.csv` 记录节流后的
`range_state`。固件分段速度映射同步为 `c14=240`、`c16=343`、`c18=446`、`c20=549`、
`c21=600 mm/s`；按用户确认保留自动默认 `c21`，但 600 mm/s 实车跟随仍待逐档验收。

### 41.1 桌面验证与交付物

- JDK 17 Debug／Release 各 **325/325**，46 个测试类，0 失败、0 错误、0 跳过。
- `:robot:check`、`:robot:assembleDebug`、固件静态契约 7 项、google-java-format 1.7 dry-run
  与主／子仓库 `git diff --check` 通过。项目没有 `:robot:spotlessCheck` 任务，格式检查继续使用
  仓库自带 google-java-format。
- Lint 保留 **11 Error、673 Warning、6 Information**：MissingClass 2、RecyclerView 1、
  RestrictedApi 4、UseAppTint 3、Timber/AGP `LintError` 1；`abortOnError=false`。另保留 Java 17／
  Kotlin 1.8 target 不一致和旧 Gradle API 警告。
- Debug APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,491,963 字节，SHA256
  `2CC2351AB80EC228E73F0E78EF57722F63F0B2312529E2C999468347EE7FF2D1`。

本轮尚未安装 APK，也没有向车辆发送控制指令。BLE `s100` 周期、实际最小值显示、传感器断线、
阈值停车和 600 mm/s 分档仍需按联调指南完成手机、车轮悬空与空旷落地测试。

## 42. Android 测距仅记录与遥控日志完善（2026-09-04）

实车遥控复查确认，V1 `s<cm>` 只提供三路有效读数的最小值，Android 无法知道该值来自左前、中央还是右前。上一节的 `RangeSafetyGate` 因而会把侧方近物错误解释为中央障碍，导致前进和转向被改成 `c0,0`；ESP32 仍会再按单路传感器状态拒绝相应方向。

本轮删除 Android 测距运动门控。手动、自动跟随和定向搜索不再因未声明能力、没有首帧、数据过期或最小距离过近而改变输出；断连、急停、前后台、身份、旧帧和自动授权安全链保持不变。V1 握手、`s100`、`s<cm>` 解析、接收时间、新鲜度和最近 `!ERR` 继续用于显示与诊断，兼容字段统一写为 `observation_only`。

离线日志版本升至 7。遥控模式可直接打开“记录日志”，不依赖 Start 或视觉推理；`control_log.csv` 末尾追加 `control_mode`，方向替换补写 `control_submit`，模式切换、请求输出、队列和 GATT 结果继续区分记录。新增 `range_log.csv`，按新测距序列或能力／新鲜度／固件错误状态变化记录最小值、age、模式、最近 Android 请求轮速和 `!ERR`。记录列表标明遥控、自动或混合会话，ZIP 自动包含新文件。

ESP32 本轮没有修改。当前已烧录固件仍可能返回 `!ERR,sensor_*` 并拒绝运动，因此 Android 恢复请求输出不等于车轮必然执行。已新增 [ESP32 传感器仅日志模式修改说明](../../../design/ESP32传感器仅日志模式修改说明.md)：下位机不能只把 `REQUIRE_RANGE_SENSORS_FOR_MOTION` 设为 false，还必须统一旁路三个风险锁存对命令接收和运动中制动的影响。

### 42.1 桌面验证与交付物

- 使用 JDK 17.0.14 完成 Debug／Release 全量测试，各 **322/322**、45 个测试类，0 失败、0 错误、0 跳过；扩展后的测距仅记录、遥控日志和混合会话回归均包含在全量结果中。
- `:robot:check`、`:robot:assembleDebug`、固件既有静态契约 7 项、google-java-format 1.7 dry-run 和主／子仓库 `git diff --check` 通过。
- Lint 为 **11 Error、675 Warning、6 Information**；Error 仍为既有 MissingClass、RecyclerView、RestrictedApi、UseAppTint 和 Timber/AGP `LintError`，项目 `abortOnError=false`。保留 Java 17／Kotlin 1.8 target 与旧 Gradle API 警告。
- Debug APK：`android/robot/build/outputs/apk/debug/robot-debug.apk`，42,490,097 字节，SHA256 `C731728E2C3448488780B6D2FC7CECF9454824BDEB21D2689FE0EB4DCEBEC164`。

`adb devices` 未列出设备，因此本轮未覆盖安装、未启动跟随、未发送车辆指令。新版首次实车测试仍须先悬空；在下位机同步“仅日志模式”前，必须从日志中的 Android 请求、GATT 写入和 ESP32 `!ERR` 三层判断未执行原因。
