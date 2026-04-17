# Beitrag leisten

<p align="center">
  <a href="CONTRIBUTING.md">English</a> |
  <a href="CONTRIBUTING.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="CONTRIBUTING.fr-FR.md">Français</a> |
  <a href="CONTRIBUTING.es-ES.md">Español</a> |
  <a href="CONTRIBUTING.ko-KR.md">한국어</a>
</p>

## Prozess

1. Reichen Sie ein Issue ein, in dem Sie die Änderungen beschreiben, die Sie implementieren möchten. Wenn es sich nur um kleinere Änderungen/Fehlerbehebungen handelt, können Sie direkt zu Schritt 3 springen.
2. Nachdem der Umfang im Issue besprochen wurde, weisen Sie es sich selbst zu. Es sollte in der Spalte "To do" im OpenBot-Projekt erscheinen.
3. Forken Sie das Projekt und klonen Sie es lokal:

   `git clone https://github.com/<user_id>/OpenBot.git`

4. Erstellen Sie einen Branch:

   `git checkout -b <branch-name>`

   wobei `<branch-name>` den Umfang der Arbeit prägnant beschreibt.

5. Erledigen Sie die Arbeit, schreiben Sie gute Commit-Nachrichten und pushen Sie Ihren Branch in das geforkte Repository:

   ```bash
   git add <geänderte Datei>
   git commit -m <aussagekräftige Beschreibung>
   git push --set-upstream origin <branch-name>
   ```

6. Erstellen Sie eine [Pull-Anfrage](https://github.com/ob-f/OpenBot/pulls) auf GitHub und verlinken Sie das Issue damit. Es sollte in der Spalte "In progress" im OpenBot-Projekt erscheinen.
7. Arbeiten Sie an jedem Code-Review-Feedback, das Sie erhalten, und pushen Sie es in Ihr Fork. Die Pull-Anfrage wird automatisch aktualisiert.
8. Gönnen Sie sich ein kaltes Getränk Ihrer Wahl, um sich dafür zu belohnen, die Welt ein Stück besser gemacht zu haben.

## Richtlinien

- Verwenden Sie denselben Stil und dieselbe Formatierung wie der Rest des Codes.
  - Für den Java (Android)- und Python-Code siehe [unten](#Formatierung).
  - Für jeden anderen Code versuchen Sie einfach, sich anzupassen.
- Aktualisieren Sie die Dokumentation, die mit den von Ihnen vorgenommenen Codeänderungen verbunden ist.
- Wenn Sie Drittanbieter-Abhängigkeiten einbeziehen möchten, besprechen Sie dies bitte zuerst im Issue.
- Pull-Anfragen sollten einzelne Funktionen mit so wenigen Änderungen wie möglich implementieren.
- Stellen Sie sicher, dass Sie keine temporären oder Binärdateien einbeziehen (die .gitignore-Dateien sollten dies größtenteils erledigen).
- Rebasen/Mergen Sie den Master in Ihren Branch, bevor Sie die Pull-Anfrage einreichen.
- Testen Sie Ihren Code nach Möglichkeit unter Windows, Linux und OSX.

## Formatierung

### Java

Wir verwenden ein Gradle-Skript zur Formatierung von Java-Code. Stellen Sie sicher, dass Sie sich im Verzeichnis `android` befinden.

Sie können Ihren Code überprüfen mit:

```bash
./gradlew checkStyle
```

Sie können den Stil auf alle Dateien anwenden, indem Sie ausführen:

```bash
./gradlew applyStyle
```

### Python

Wir verwenden [black](https://pypi.org/project/black/) zur Formatierung von Python-Code.

Sie können Ihren Code im aktuellen Verzeichnis überprüfen mit:

```bash
black --check .
```

Sie können den Stil auf alle Dateien im aktuellen Verzeichnis anwenden, indem Sie ausführen:

```bash
black .
```

## Weitere Ressourcen

Wenn Sie nach weiteren Informationen zum Beitrag zu Open-Source-Projekten suchen, sind hier zwei gute Referenzen:

- [How to Contribute to Open Source](http://opensource.guide/how-to-contribute/)
- [The beginner's guide to contributing to a GitHub project](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

Vielen Dank!
