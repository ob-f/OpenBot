# 커스텀 PCB

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

커스텀 PCB는 Arduino Nano를 위한 캐리어 보드 역할을 하며, 최신 모터 드라이버, 전압 분배 회로, LED용 저항을 통합합니다. Arduino는 핀 헤더에 간단히 꽂으면 되고, 모든 센서와 LED는 Dupont 케이블로 적절한 커넥터에 연결됩니다.

![PCB_2D](../../../docs/images/pcb_2d_v2.png)  
![PCB_3D](../../../docs/images/pcb_3d_v2.png)

최신 PCB는 [버전 2](v2)입니다. [버전 1](v1)과 비교한 변경사항은 다음과 같습니다:

- 인터럽트 기능을 사용하기 위해 오른쪽 속도 센서를 핀 D3로 이동
- 메인 배터리용 전원 LED 추가
- 더 흔히 사용 가능한 부품으로 업데이트
- 더 높은 정밀도를 위해 전압 분배기를 20k/10k로 변경
- 모터 커넥터를 직립형으로 변경하여 접근성 개선

이미 [버전 1](v1)의 PCB를 주문하셨다면 걱정하지 마세요. 잘 작동할 것입니다. 펌웨어에서 올바른 플래그를 설정하기만 하면 됩니다. ([2D 보기](../../../docs/images/pcb_2d_v1.png), [3D 보기](../../../docs/images/pcb_3d_v1.png))

커스텀 PCB는 다음 단계를 포함합니다:

1) **PCB 주문:** [Gerber](v2/gerber_v2.zip) 파일을 다운로드하고 원하는 공급업체에서 PCB를 주문하세요. 또한, OpenBot 프로젝트가 공유된 [PCBWay](https://www.pcbway.com/project/shareproject/OpenBot__Turning_Smartphones_into_Robots.html)에서 직접 PCB를 주문할 수도 있습니다.
2) **부품 주문:** [BOM](v2/BOM_v2.csv)을 다운로드하고 원하는 공급업체에서 부품을 주문하세요. 예: [LCSC](https://lcsc.com)
3) **PCB 조립:** PCB를 직접 조립하거나 공급업체를 통해 조립할 수 있습니다. 자동 조립을 원할 경우 [Centroid File](v2/centroid_file_v2.csv)이 필요합니다. [JLCPCB](https://jlcpcb.com/)에서 PCB를 주문하면 SMT 조립 서비스를 이용할 수 있습니다. 그런 경우, 관통형 부품만 따로 주문해서 납땜하면 됩니다. 이 방법은 가장 편리하고, 저렴하며, 빠릅니다. [버전 2](v2) PCB에서는 모든 부품이 [JLCPCB](https://jlcpcb.com/)에서 바로 구할 수 있도록 업데이트되었습니다.

또한, PCB 제조, 부품 구매 및 조립 3단계를 모두 맡아주는 TurnKey 솔루션을 제공하는 공급업체도 찾을 수 있습니다. 이 방법은 매우 편리하며, 비용도 그렇게 비싸지 않습니다. 하지만 배송 시간이 종종 길어질 수 있습니다 (1-3개월).

[PCBWay](https://www.pcbway.com/orderonline.aspx)에서 견적을 요청할 때, Gerber 파일을 업로드한 후 조립 서비스를 선택하세요.  
![Assembly Service](../../../docs/images/assembly_service.jpg)  
다음 단계에서는 [BOM](v2/BOM_v2.csv) 및 [Centroid File](v2/centroid_file_v2.csv)을 업로드해야 합니다. 며칠 내로 견적이 검토 및 업데이트됩니다. 이후 비용과 배송 시간을 확인한 뒤 결제 여부를 결정할 수 있습니다.  
