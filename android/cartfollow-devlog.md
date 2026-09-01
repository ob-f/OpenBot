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
| **近场传感器安全门** | 高 | 超声波 / ToF 尚未接入，真实自动模式只能在空旷受控场地测试 |
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

方向文字以项目既有 `ControlGenerator.FLIP_TURN=true` 的控制口径为准：目标图像偏左时，
Simulator 显示向右转；目标图像偏右时，显示向左转。`HumanCommandInterpreter` 与连续
仪表共用这一口径，避免新旧页面的左右提示相反，并增加左右对称测试。

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
