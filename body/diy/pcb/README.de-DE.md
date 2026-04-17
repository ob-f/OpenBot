# Custom PCB

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Die Leiterplatte (PCB) dient als Trägerplatine für den Arduino Nano und integriert moderne Motortreiber, die Spannungsteiler-Schaltung und Widerstände für die LEDs. Der Arduino wird einfach in die Stiftleiste gesteckt und alle Sensoren und LEDs werden über Dupont-Kabel mit den entsprechenden Anschlüssen verbunden.

![PCB_2D](../../../docs/images/pcb_2d_v2.png)
![PCB_3D](../../../docs/images/pcb_3d_v2.png)

Die neueste Version der PCB ist [Version 2](v2). Hier sind die Änderungen im Vergleich zu [Version 1](v1):

- Der rechte Geschwindigkeitssensor wird auf Pin D3 verschoben, um die Interrupt-Funktionalität zu ermöglichen
- Hinzufügen einer Power-LED für die Hauptbatterie
- Aktualisierung einiger Komponenten, die häufiger verfügbar sind
- Aktualisierung des Spannungsteilers auf 20k/10k für bessere Präzision
- Änderung der Motoranschlüsse auf eine aufrechte Version für besseren Zugang

Wenn Sie bereits [Version 1](v1) der PCB bestellt haben ([2D-Ansicht](../../../docs/images/pcb_2d_v1.png), [3D-Ansicht](../../../docs/images/pcb_3d_v1.png)), machen Sie sich keine Sorgen, sie wird einwandfrei funktionieren. Stellen Sie nur sicher, dass Sie das richtige Flag in der Firmware setzen.

Die kundenspezifische PCB umfasst die folgenden Schritte:

1) **Bestellen der PCB**: Laden Sie die [Gerber](v2/gerber_v2.zip) Dateien herunter und bestellen Sie die PCB bei einem Anbieter Ihrer Wahl. Sie können die PCB auch direkt bei [PCBWay](https://www.pcbway.com/project/shareproject/OpenBot__Turning_Smartphones_into_Robots.html) bestellen, wo wir ein Projekt für OpenBot geteilt haben.
2) **Bestellen der Komponenten:** Laden Sie die [BOM](v2/BOM_v2.csv) herunter und bestellen Sie die Komponenten bei einem Anbieter Ihrer Wahl, zum Beispiel [LCSC](https://lcsc.com).
3) **Montage der PCB:** Sie können die PCB entweder selbst montieren oder von einem Anbieter montieren lassen. Für die automatisierte Montage benötigen Sie die [Centroid-Datei](v2/centroid_file_v2.csv). Wenn Sie die PCB bei [JLCPCB](https://jlcpcb.com/) bestellen, können Sie deren SMT-Montageservice nutzen. Dann müssen Sie nur noch die Durchsteckkomponenten selbst bestellen und löten. Wir fanden dies die bequemste, günstigste und schnellste Option. In [Version 2](v2) der PCB haben wir die Komponenten aktualisiert, um sicherzustellen, dass alle direkt bei [JLCPCB](https://jlcpcb.com/) verfügbar sind.

Sie können auch Anbieter finden, die Ihnen eine TurnKey-Lösung anbieten, die alle 3 Schritte abdeckt. Sie werden die PCB herstellen, die Komponenten beschaffen und die PCB montieren. Dies ist sehr bequem und auch nicht zu teuer. Allerdings sind die Lieferzeiten oft sehr lang (1-3 Monate).

Wenn Sie ein Angebot bei [PCBWay](https://www.pcbway.com/orderonline.aspx) anfordern, können Sie nach dem Hochladen der Gerber-Datei den Montageservice auswählen.
![Assembly Service](../../../docs/images/assembly_service.jpg)
Im nächsten Schritt müssen Sie die [BOM](v2/BOM_v2.csv) und die [Centroid-Datei](v2/centroid_file_v2.csv) hochladen. Ihr Angebot wird dann innerhalb weniger Tage überprüft und aktualisiert. Sie können dann nach Überprüfung der Kosten und Lieferzeit mit der Zahlung fortfahren.
