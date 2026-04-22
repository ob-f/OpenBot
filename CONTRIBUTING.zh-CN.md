# 贡献指南

<p align="center">
  <a href="CONTRIBUTING.md">English</a> |
  <span>简体中文</span> |
  <a href="CONTRIBUTING.de-DE.md">Deutsch</a> |
  <a href="CONTRIBUTING.fr-FR.md">Français</a> |
  <a href="CONTRIBUTING.es-ES.md">Español</a> |
  <a href="CONTRIBUTING.ko-KR.md">한국어</a>
</p>

## 过程

1. 提交一个描述您想要实现的更改的issue。如果只是小的更改/修复bug，可以跳到第3步。
2. 在issue中讨论范围后，将其分配给自己。它应该显示在OpenBot项目的“待办”列中。
3. Fork项目并在本地克隆：

   `git clone https://github.com/<user_id>/OpenBot.git`

4. 创建一个分支：

   `git checkout -b <branch-name>`

   其中`<branch-name>`简洁地描述了工作的范围。

5. 完成工作，写好提交信息，将您的分支推送到fork的仓库：

   ```bash
   git add <modified file>
   git commit -m <meaningful description>
   git push --set-upstream origin <branch-name>
   ```

6. 在GitHub中创建一个[拉取请求](https://github.com/ob-f/OpenBot/pulls)并将issue链接到它。它应该显示在OpenBot项目的“进行中”列中。
7. 处理您可能收到的任何代码审查反馈并将其推送到您的fork。拉取请求会自动更新。
8. 喝一杯您喜欢的冷饮，奖励自己让世界变得更美好。

## 指南

- 使用与其余代码相同的风格和格式。
  - 对于Java（Android）和Python代码，请参见[下面](#Formatting)。
  - 对于其他任何代码，只需尽量融入。
- 更新与您所做的代码更改相关的文档。
- 如果您想包含第三方依赖项，请先在issue中讨论。
- 拉取请求应实现单个功能，并尽可能少地更改。
- 确保不包含临时或二进制文件（gitignore应该大部分处理这个问题）。
- 在提交拉取请求之前，将master分支变基/合并到您的分支中。
- 如果可能，请在Windows、Linux和OSX上测试您的代码。

## 格式化

### Java

我们使用gradle脚本来格式化Java代码。确保您在`android`目录中。

您可以使用以下命令检查您的代码：

```bash
./gradlew checkStyle
```

您可以通过运行以下命令将样式应用于所有文件：

```bash
./gradlew applyStyle
```

### Python

我们使用[black](https://pypi.org/project/black/)来格式化Python代码。

您可以使用以下命令检查当前目录中的代码：

```bash
black --check .
```

您可以通过运行以下命令将样式应用于当前目录中的所有文件：

```bash
black .
```

## 进一步的资源

如果您正在寻找有关为开源项目做贡献的更多信息，这里有两个很好的参考：

- [如何为开源做贡献](http://opensource.guide/how-to-contribute/)
- [初学者指南：如何为GitHub项目做贡献](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

非常感谢！
