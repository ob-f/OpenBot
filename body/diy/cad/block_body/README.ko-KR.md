# 추가 공간과 레고 지지대가 있는 블록형 본체

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

이 로봇 섀시는 전자 부품을 더 쉽게 넣을 수 있도록 높이를 추가로 제공하며, 레고와 호환되는 상단을 가지고 있어요! 이 버전은 [기본 섀시](../regular_body/)와 동일한 구조적 안정성을 제공하면서, OpenBot 껍질 내부의 추가 공간, 놀이와 학습을 위한 레고 호환 상단, 그리고 범퍼를 유지하면서도 더 작은 출력 베드 크기에 적합한 발판을 갖추고 있어요.

![블록 CAD](../../../../docs/images/block_cad.jpg)

## 부품

아래의 부품들을 출력해야 해요:  
하단 부품과 상단 부품 중 하나를 선택하세요!

- `block_body_bottom`([STL](block_body_bottom.stl), [STEP](block_body_bottom.step)): 몸체의 하단 부품
- `block_body_top`([STL](block_body_top.stl), [STEP](block_body_top.step)): 기본 상단 부품
- `block_body_top_lego`([STL](block_body_top_lego.stl), [STEP](block_body_top_lego.step)): 레고 호환 표면을 가진 기본 상단 부품
- `block_body_top_big`([STL](block_body_top_big.stl), [STEP](block_body_top_big.step)): 전자 부품을 위한 추가 공간이 있는 큰 상단 부품
- `block_body_top_lego`([STL](block_body_top_big_lego.stl), [STEP](block_body_top_big_lego.step)): 레고 호환 표면을 가진 큰 상단 부품

위 부품들을 출력하려면 빌드 플레이트 크기가 최소 221x150mm 이상이어야 해요!

## 출력 설정

최상의 결과를 위해 아래와 같은 출력 설정을 추천드려요:

- 층 두께: 0.2mm
- 벽 라인 개수: 3 (더 큰 표면의 구조적 안정성을 위해 벽을 더 추가하면 좋아요!)
- 상단 레이어: 5
- 하단 레이어: 4
- 채움률: 25%
- 채움 패턴: 동심원형 (이 설정은 시간과 플라스틱을 절약하는 데 효과적이에요!)
- 출력 속도: 50mm/sec
- 지지대 생성: 예
- 지지대 패턴: 동심원형
- 지지대 밀도: 15%
- 지지대 브림 활성화: 예
- 빌드 플레이트 접착 유형: 없음

행복한 로봇 제작 시간이 되길 바랄게요!

![블록 몸체](../../../../docs/images/block_body.jpg)  