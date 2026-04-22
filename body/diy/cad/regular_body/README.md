# Regular Body

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

![Assembly](../../../../docs/images/assembly.gif)

## Parts

You will need to print the following parts in order to build your OpenBot.

- `body_bottom` ([STL](body_bottom.stl), [STEP](body_bottom.step))
- `body_top` ([STL](body_top.stl), [STEP](body_top.step))

For the above parts, your build plate needs to be at least 240mmx150mm.

## Print Settings

On an Ultimaker S5, we achieved good results with the following settings:

- layer height: 0.2mm
- wall thickness: 1.5mm
- infill density: 20%
- infill pattern: grid
- print speed 80 mm/s
- no support

We were able to print the chassis with PLA, ABS and CPE. In our experience the print was not affected very much by the print settings. However, if you have the patience, printing slower and with smaller layer height will improve the print. Also adding a support structure can improve the print, but it will require additional work to remove afterwards.
