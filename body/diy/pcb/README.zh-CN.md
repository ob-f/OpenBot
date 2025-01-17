# 定制PCB

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

定制PCB作为Arduino Nano的载板，集成了现代电机驱动器、电压分压电路和LED电阻。Arduino只需插入针脚头，所有传感器和LED通过杜邦线连接到相应的连接器。

![PCB_2D](../../../docs/images/pcb_2d_v2.png)
![PCB_3D](../../../docs/images/pcb_3d_v2.png)

最新的PCB是[版本2](v2)。与[版本1](v1)相比，这里是一些变化：

- 将右侧速度传感器移动到D3引脚以启用中断功能
- 添加主电池的电源LED
- 更新一些更常见的组件
- 将电压分压器更新为20k/10k以提高精度
- 更改电机连接器为直立版本以便于访问

如果你已经订购了[版本1](v1)的PCB（[2D视图](../../../docs/images/pcb_2d_v1.png)，[3D视图](../../../docs/images/pcb_3d_v1.png)），不用担心，它仍然可以正常工作。只需确保在固件中设置正确的标志。

定制PCB涉及以下步骤：

1) **订购PCB**：下载[Gerber](v2/gerber_v2.zip)文件并在你选择的供应商处订购PCB。你也可以直接在[PCBWay](https://www.pcbway.com/project/shareproject/OpenBot__Turning_Smartphones_into_Robots.html)上订购，我们在这里分享了一个OpenBot项目。
2) **订购组件**：下载[BOM](v2/BOM_v2.csv)并在你选择的供应商处订购组件，例如[LCSC](https://lcsc.com)。
3) **组装PCB**：你可以自己组装PCB，也可以让供应商组装。对于自动化组装，你将需要[Centroid File](v2/centroid_file_v2.csv)。如果你在[JLCPCB](https://jlcpcb.com/)订购PCB，可以使用他们的SMT组装服务。然后你只需自己订购并焊接通孔组件。我们发现这是最方便、最便宜和最快的选择。在[版本2](v2)的PCB中，我们更新了组件，以确保所有组件都可以直接从[JLCPCB](https://jlcpcb.com/)获得。

你也可以找到提供一站式解决方案的供应商，涵盖所有3个步骤。他们将制造PCB、采购组件并组装PCB。这非常方便且价格也不贵。然而，交货时间通常很长（1-3个月）。

在[PCBWay](https://www.pcbway.com/orderonline.aspx)请求报价时，上传Gerber文件后可以选择组装服务。
![Assembly Service](../../../docs/images/assembly_service.jpg)
在下一步中，你需要上传[BOM](v2/BOM_v2.csv)和[Centroid File](v2/centroid_file_v2.csv)。你的报价将在几天内审核并更新。你可以在审核成本和交货时间后选择继续付款。