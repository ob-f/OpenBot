# 휴대폰 거치대

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

[DIY](../diy/README.ko-KR.md), [RC truck](../rc_truck/README.ko-KR.md), [Compact](../compact/README.ko-KR.md) 차체가 함께 쓰는 두 부품짜리 휴대폰 거치대예요. 슬라이드 그립이 휴대폰을 받침에 눌러 고정하고, 고무줄이나 가벼운 스프링이 장력을 줘요. 선택 사항인 각도 조절 버전은 휴대폰을 0°, 15°, 30°로 고정해서 카메라가 위쪽을 볼 수 있게 해요(예: 로봇 앞에 서 있는 사람). 180° 돌려서 장착하면 카메라가 아래쪽을 봐요.

## 부품

| 부품 | 수량 | 설명 |
|---|---:|---|
| `phone_mount_bottom` ([STL](phone_mount_bottom.stl), [STEP](phone_mount_bottom.step)) | 1 | 받침과 기둥; M3 × 25 mm 나사 두 개와 너트로 차체에 고정 |
| `phone_mount_top` ([STL](phone_mount_top.stl), [STEP](phone_mount_top.step)) | 1 | 슬라이드 그립; 두 받침 모두에 사용 |
| `phone_mount_tilt_base` ([STL](phone_mount_tilt_base.stl), [STEP](phone_mount_tilt_base.step)) | 1 *(선택)* | 각도 조절 거치대: 힌지 측판 두 개가 달린 받침, `phone_mount_bottom` 대신 사용 |
| `phone_mount_tilt_column` ([STL](phone_mount_tilt_column.stl), [STEP](phone_mount_tilt_column.step)) | 1 *(선택)* | 각도 조절 거치대: 측판 사이에서 회전하는 기둥 |

## 출력

파일은 이미 출력 방향으로 놓여 있어요. 모든 부품을 그대로 세워서 출력하세요. PLA, 0.2 mm 레이어, 벽 4겹, 채움 30 %면 충분해요. `phone_mount_tilt_base`는 서포트가 필요 없어요. `phone_mount_bottom`, `phone_mount_top`, `phone_mount_tilt_column`은 고무줄 돌기 테두리 아래에만 서포트가 필요해요. 트리 서포트를 쓰고 “작은 오버행 제거”를 끄면 그곳에 작은 트리 하나가 생기고, 홈의 열린 뒤쪽으로 빼내면 돼요. 테두리가 돌기 전체를 둘러싸서 고무줄이 느슨해져도 빠지지 않아요.

## 각도 조절 휴대폰 거치대

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="각도 조절 휴대폰 거치대의 힌지" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="0°, 15°, 30°의 각도 조절 휴대폰 거치대, 위는 기본 장착, 아래는 돌려서 장착" width="64%" />
</p>

각도 조절 받침은 기본 받침과 같은 두 개의 장착 구멍을 쓰고, 같은 홈에 들어가요: DIY 상판, RC truck 프레임과 카메라 높임판, Compact 덮개. 측판은 받침 바닥에서 5 mm 위부터 시작해서 이 홈의 가장자리에 닿지 않아요. 휴대폰은 기본 거치대보다 10.5 mm 높게 놓여요. 30°에서는 기둥 윗부분이 뒤로(돌려서 장착하면 앞으로) 약 22 mm 움직이니, 차체에 걸리는 것이 없는지 확인하세요. 받침을 180° 돌려서 고정할 수도 있어요. 그러면 기둥이 앞으로 기울어져요. 화면이 기둥을 향하게 휴대폰을 넣으면 카메라가 앞쪽 아래를 봐요.

장착 나사 외에 M3 × 10 mm 둥근머리 나사 세 개와 M3 열압입 인서트(외경 4.6 mm, 길이 5.7 mm) 다섯 개가 필요해요. 황동 나사산이라 각도를 자주 바꿔도 헐거워지지 않아요.

1. 기둥에 M3 열압입 인서트(외경 4.6 mm, 길이 5.7 mm) 세 개를 넣으세요. 뒤쪽 회전축 구멍의 양쪽 끝에 하나씩, 한쪽만 열려 있는 앞쪽 고정 구멍에 하나를 넣어요.
2. 받침 발 바닥의 구멍 두 개에 M3 인서트를 하나씩 넣으세요. 기본 받침과 같은 장착 구멍 두 개를 통해 아래에서 차체에 고정하세요. 나사는 받침 안으로 약 5 mm 들어가야 해요 (5 mm 두께의 Compact 덮개에는 M3 × 10 mm).
3. 열린 고정 구멍이 구멍 네 개짜리 측판을 향하도록 기둥을 두 측판 사이에 넣으세요. 양쪽 측판을 통해 회전축 인서트에 M3 × 10 mm 나사를 하나씩 조이고, 기둥이 유격 없이 돌아갈 때까지 조이세요. 두 회전축 나사에 중강도(파란색) 나사 고정제를 한 방울씩 발라 풀리지 않게 하세요.
4. 기둥의 고정 인서트가 측판의 구멍 세 개 중 하나와 맞을 때까지 기둥을 기울이세요. 가장 아래 구멍은 0°, 가운데는 15°, 가장 위는 30°예요. 세 번째 M3 × 10 mm 나사로 고정하세요.
5. 기본 거치대처럼 `phone_mount_top`과 고무줄을 끼우세요.
