# Contributing

<p align="center">
  <span>English</span> |
  <a href="CONTRIBUTING.zh-CN.md">简体中文</a> |
  <a href="CONTRIBUTING.de-DE.md">Deutsch</a> |
  <a href="CONTRIBUTING.fr-FR.md">Français</a> |
  <a href="CONTRIBUTING.es-ES.md">Español</a> |
  <a href="CONTRIBUTING.ko-KR.md">한국어</a>
</p>

## Process

1. Submit an issue describing the changes you want to implement. If it's only minor changes/bug-fixes, you can skip to step 3.
2. After the scope was discussed in the issue, assign it to yourself. It should show up in the "To do" column in the OpenBot project.
3. Fork the project and clone it locally:

   `git clone https://github.com/<user_id>/OpenBot.git`

4. Create a branch:

   `git checkout -b <branch-name>`

   where `<branch-name>` concisely describes the scope of the work.

5. Do the work, write good commit messages, push your branch to the forked repository:

   ```bash
   git add <modified file>
   git commit -m <meaningful description>
   git push --set-upstream origin <branch-name>
   ```

6. Create a [pull request](https://github.com/ob-f/OpenBot/pulls) in GitHub and link the issue to it. It should show up in the "In progress" column in the OpenBot project.
7. Work on any code review feedback you may receive and push it to your fork. The pull request gets updated automatically.
8. Get a cold drink of your choice to reward yourself for making the world a better place.

## Guidelines

- Use same style and formatting as rest of code.
  - For the Java (Android) and Python code see [below](#Formatting).
  - For any other code, just try to blend in.
- Update documentation associated with code changes you made.
- If you want to include 3rd party dependencies, please discuss this in the issue first.
- Pull requests should implement single features with as few changes as possible.
- Make sure you don't include temporary or binary files (the gitignores should mostly take care of this).
- Rebase/merge master into your branch before you submit the pull request.
- If possible, test your code on Windows, Linux and OSX.

## Formatting

### Java

We use a gradle script for formatting java code. Make sure you are in the `android` directory.

You can check your code with:

```bash
./gradlew checkStyle
```

You can apply the style to all files by running:

```bash
./gradlew applyStyle
```

### Python

We use [black](https://pypi.org/project/black/) for formatting python code.

You can check your code in the current directory with:

```bash
black --check .
```

You can apply the style to all files in the current directory by running:

```bash
black .
```

## Further resources

If you are looking for more information about contributing to open-source projects, here are two good references:

- [How to Contribute to Open Source](http://opensource.guide/how-to-contribute/)
- [The beginner's guide to contributing to a GitHub project](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

Thank you very much!
