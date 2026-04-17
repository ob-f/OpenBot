# 기여하기

<p align="center">
  <a href="CONTRIBUTING.md">English</a> |
  <a href="CONTRIBUTING.zh-CN.md">简体中文</a> |
  <a href="CONTRIBUTING.de-DE.md">Deutsch</a> |
  <a href="CONTRIBUTING.fr-FR.md">Français</a> |
  <a href="CONTRIBUTING.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

## 과정

1. 구현하고 싶은 변경 사항을 설명하는 이슈를 제출합니다. 사소한 변경이나 버그 수정인 경우 3단계로 건너뛰어도 됩니다.
2. 이슈에서 범위가 논의된 후, 자신에게 할당합니다. OpenBot 프로젝트의 "해야 할 일" 열에 나타나야 합니다.
3. 프로젝트를 포크하고 로컬로 클론합니다:

   `git clone https://github.com/<user_id>/OpenBot.git`

4. 브랜치를 생성합니다:

   `git checkout -b <branch-name>`

   여기서 `<branch-name>`은 작업의 범위를 간결하게 설명합니다.

5. 작업을 수행하고, 좋은 커밋 메시지를 작성한 후, 포크한 저장소에 브랜치를 푸시합니다:

   ```bash
   git add <modified file>
   git commit -m <meaningful description>
   git push --set-upstream origin <branch-name>
   ```

6. GitHub에서 [풀 리퀘스트 요청](https://github.com/ob-f/OpenBot/pulls)을 생성하고 이슈에 연결합니다. OpenBot 프로젝트의 "진행 중" 열에 나타나야 합니다.
7. 받을 수 있는 코드 리뷰 피드백을 반영하여 포크한 저장소에 푸시합니다. 풀 요청은 자동으로 업데이트됩니다.
8. 세상을 더 나은 곳으로 만들었다는 보상으로 원하는 음료를 마십니다.

## 가이드라인

- 나머지 코드와 동일한 스타일과 포맷을 사용합니다.
    - Java (Android) 및 Python 코드에 대한 내용은 [아래](#Formatting)를 참조하세요.
    - 다른 코드의 경우, 가능한 한 잘 어울리도록 노력하세요.
- 변경한 코드와 관련된 문서를 업데이트합니다.
- 제3자 종속성을 포함하고 싶다면, 먼저 이슈에서 논의하세요.
- 풀 요청은 가능한 한 적은 변경으로 단일 기능을 구현해야 합니다.
- 임시 파일이나 바이너리 파일이 포함되지 않도록 합니다 (gitignore가 대부분 이를 처리해야 합니다).
- 풀 요청을 제출하기 전에 마스터 브랜치를 자신의 브랜치에 리베이스/머지합니다.
- 가능하다면 Windows, Linux 및 OSX에서 코드를 테스트합니다.

## 초기화

### Java

Java 코드를 포맷하기 위해 gradle 스크립트를 사용합니다. `android` 디렉토리에 있어야 합니다.

코드를 확인하려면 다음을 사용하세요:

```bash
./gradlew checkStyle
```

모든 파일에 스타일을 적용하려면 다음을 실행합니다:

```bash
./gradlew applyStyle
```

### Python

Python 코드 포맷에는 [black](https://pypi.org/project/black/)을 사용합니다.

현재 디렉토리의 코드를 확인하려면:

```bash
black --check .
```

현재 디렉토리의 모든 파일에 스타일을 적용하려면:

```bash
black .
```

## 추가 자료

오픈 소스 프로젝트에 기여하는 방법에 대한 더 많은 정보가 필요하다면, 다음 두 가지 참고 자료를 추천합니다:

- [오픈 소스에 기여하는 방법](http://opensource.guide/how-to-contribute/)
- [GitHub 프로젝트에 기여하는 초보자 가이드](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

감사합니다!