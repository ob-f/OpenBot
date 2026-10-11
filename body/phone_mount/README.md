# Phone mount

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

The two-part phone holder used by the [DIY](../diy/README.md), [RC truck](../rc_truck/README.md) and [Compact](../compact/README.md) bodies. A sliding grip holds the phone against a ledge and is tensioned by a rubber band or a light spring. An optional tilting version holds the phone at 0°, 10°, 20° or 30° so the camera can look up, for example at a person standing in front of the robot.

## Parts

| Part | Qty | Notes |
|---|---:|---|
| `phone_mount_bottom` ([STL](phone_mount_bottom.stl), [STEP](phone_mount_bottom.step)) | 1 | Base and column; screws to the body with two M3 × 25 mm screws and nuts |
| `phone_mount_top` ([STL](phone_mount_top.stl), [STEP](phone_mount_top.step)) | 1 | Sliding grip; used with either base |
| `phone_mount_tilt_base` ([STL](phone_mount_tilt_base.stl), [STEP](phone_mount_tilt_base.step)) | 1 *(optional)* | Tilting holder: foot with two hinge cheeks; replaces `phone_mount_bottom` |
| `phone_mount_tilt_column` ([STL](phone_mount_tilt_column.stl), [STEP](phone_mount_tilt_column.step)) | 1 *(optional)* | Tilting holder: column that pivots between the cheeks |

## Printing

The files are stored in print orientation: print every part standing upright as it is. PLA with 0.2 mm layers, 4 walls and 30 % infill works well. `phone_mount_tilt_base` needs no supports. `phone_mount_bottom`, `phone_mount_top` and `phone_mount_tilt_column` need supports only under the lip of the band stud; tree supports with “Remove small overhangs” switched off give one small tree there, which you pull out through the open back of the channel. The lip runs all round the stud, so the band cannot slip off even when it is slack.

## Tilting phone holder

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="Hinge of the tilting phone holder" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="Tilting phone holder at 0, 10, 20 and 30 degrees" width="64%" />
</p>

The tilting base uses the same two mounting holes as the standard base and fits the same pockets: the DIY top plate, the RC truck frame and camera elevator, and the Compact lid. Its cheeks start 5 mm above the bottom of the foot, so they clear those pockets. The phone sits 10.5 mm higher than on the standard holder. At 30° the top of the column moves about 22 mm backward, so check that nothing on your body is in the way.

Besides the mounting screws, you need three M3 × 10 mm pan-head screws and four M3 heat-set inserts (4.6 mm outer diameter, 5.7 mm long). The brass threads keep their grip however often you change the angle.

1. Melt four M3 heat-set inserts (4.6 mm outer diameter, 5.7 mm long) into the column: one into each end of the pivot hole at the back, and one into each of the two front lock holes, which are open on one side only.
2. Press two M3 nuts into the foot and screw it to the body like the standard base.
3. Set the column between the cheeks with the open lock holes toward the cheek that has five holes. Drive an M3 × 10 mm screw through each cheek into the pivot inserts and tighten them until the column turns without play. Put a drop of medium-strength (blue) threadlocker on both pivot screws so they cannot work loose.
4. Tilt the column back until one of its lock inserts lines up with a cheek hole. The front pair of cheek holes gives 0° (lower) and 20° (upper); the rear pair gives 10° (lower) and 30° (upper). Lock it there with the third M3 × 10 mm screw.
5. Fit `phone_mount_top` and the band as on the standard holder.
