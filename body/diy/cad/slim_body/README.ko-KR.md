# Slim Body

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

몇몇 3D 프린터는 OpenBot 몸체를 원래 크기로 출력하기엔 빌드 볼륨이 너무 작아요.  
이 폴더에는 OpenBot 몸체의 슬림 버전이 들어 있어요.  
부품을 45도로 회전하면 220mmx220mm 빌드 플레이트로 출력할 수 있답니다.

![슬림 몸체](../../../../docs/images/slim_body.jpg)

## 부품

1) `slim_body_bottom` ([STL](slim_body_bottom.stl), [STEP](slim_body_bottom.step))
2) `slim_body_top` ([STL](slim_body_top.stl), [STEP](slim_body_top.step))

최대 출력 영역을 확보하기 위해 다음 설정을 조정해야 할 수도 있어요:

- *Build Plate Adhesion Type*을 "None"으로 설정하기 (Brim, Skirt, Raft는 출력 크기를 늘려요)
- 프라임 블롭(prime blob)을 비활성화하기 (*Build Plate* 섹션에서)
- 프린터에 두 번째 익스트루더가 있다면 비활성화하기

만약 약간의 추가 공간이 있다면 (223mmx223mm) `slim_body_top_rim`도 출력할 수 있어요.  
([STL](slim_body_top_rim.stl), [STEP](slim_body_top_rim.step))  
이 부품은 테두리가 살짝 더 커서 윗부분을 분리하기 더 쉽답니다!