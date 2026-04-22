# Contribuer

<p align="center">
  <a href="CONTRIBUTING.md">English</a> |
  <a href="CONTRIBUTING.zh-CN.md">简体中文</a> |
  <a href="CONTRIBUTING.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="CONTRIBUTING.es-ES.md">Español</a> |
  <a href="CONTRIBUTING.ko-KR.md">한국어</a>
</p>

## Processus

1. Soumettez un problème décrivant les modifications que vous souhaitez apporter. S'il s'agit uniquement de modifications mineures/corrections de bogues, vous pouvez passer directement à l'étape 3.
2. Après avoir discuté de la portée dans le problème, assignez-le vous-même. Il devrait apparaître dans la colonne "À faire" du projet OpenBot.
3. Forkez le projet et clonez-le localement :

   `git clone https://github.com/<user_id>/OpenBot.git`

4. Créez une branche :

   `git checkout -b <nom-de-branche>`

   où `<nom-de-branche>` décrit de manière concise la portée du travail.

5. Faites le travail, écrivez de bons messages de commit, poussez votre branche vers le dépôt forké :

   ```bash
   git add <fichier modifié>
   git commit -m <description significative>
   git push --set-upstream origin <nom-de-branche>
   ```

6. Créez une [pull request](https://github.com/ob-f/OpenBot/pulls) sur GitHub et liez-y le problème. Il devrait apparaître dans la colonne "En cours" du projet OpenBot.
7. Travaillez sur les retours de révision de code que vous pourriez recevoir et poussez-les vers votre fork. La pull request se met à jour automatiquement.
8. Prenez une boisson fraîche de votre choix pour vous récompenser d'avoir rendu le monde meilleur.

## Directives

- Utilisez le même style et formatage que le reste du code.
  - Pour le code Java (Android) et Python, voir [ci-dessous](#Formatage).
  - Pour tout autre code, essayez simplement de vous fondre dans le style existant.
- Mettez à jour la documentation associée aux modifications de code que vous avez apportées.
- Si vous souhaitez inclure des dépendances tierces, veuillez en discuter d'abord dans le problème.
- Les pull requests doivent implémenter des fonctionnalités uniques avec le moins de modifications possible.
- Assurez-vous de ne pas inclure de fichiers temporaires ou binaires (les gitignores devraient s'en charger pour la plupart).
- Rebasez/fusionnez master dans votre branche avant de soumettre la pull request.
- Si possible, testez votre code sur Windows, Linux et OSX.

## Formatage

### Java

Nous utilisons un script gradle pour formater le code java. Assurez-vous d'être dans le répertoire `android`.

Vous pouvez vérifier votre code avec :

```bash
./gradlew checkStyle
```

Vous pouvez appliquer le style à tous les fichiers en exécutant :

```bash
./gradlew applyStyle
```

### Python

Nous utilisons [black](https://pypi.org/project/black/) pour formater le code python.

Vous pouvez vérifier votre code dans le répertoire actuel avec :

```bash
black --check .
```

Vous pouvez appliquer le style à tous les fichiers dans le répertoire actuel en exécutant :

```bash
black .
```

## Ressources supplémentaires

Si vous cherchez plus d'informations sur la contribution aux projets open-source, voici deux bonnes références :

- [Comment contribuer à l'open source](http://opensource.guide/how-to-contribute/)
- [Le guide du débutant pour contribuer à un projet GitHub](https://akrabat.com/the-beginners-guide-to-contributing-to-a-github-project/)

Merci beaucoup !
