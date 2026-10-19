# Applications Android

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Fonctionnalités

Cliquez sur les liens ci-dessous pour en savoir plus sur les fonctionnalités des applications.

- [Application Robot](robot/README.md)
- [Application Contrôleur](../controller/flutter/README.fr-FR.md) (Flutter)

## Installer les applications

Le moyen le plus simple d'obtenir l'une des applications est de la télécharger directement sur le téléphone en utilisant le code QR correspondant. Si vous êtes sur le navigateur du téléphone, vous pouvez également cliquer sur le code QR. Vous pouvez ensuite ouvrir le fichier apk sur votre téléphone et [l'installer](https://www.lifewire.com/install-apk-on-android-4177185). Notez que l'apk est uniquement signé avec une clé de débogage.

<table style="width:100%;border:none;text-align:center">
  <tr>
    <td>  <a href="https://app.openbot.org/robot" target="_blank">
    <img alt="🤖 App" width="50%" src="../docs/images/robot_app_qr_code.png" />
  </a>
    </td>
    <td>
  <a href="https://app.openbot.org/controller" target="_blank">
    <img alt="🎮 App" width="50%" src="../docs/images/controller_app_qr_code.png" />
  </a>
      </td>
  </tr>
  <tr>
    <td>🤖 App</td>
    <td>🎮 App</td>
  </tr>
</table>

Alternativement, vous pouvez télécharger les fichiers apk depuis les assets de n'importe quelle [release](https://github.com/ob-f/OpenBot/releases). Si vous souhaitez obtenir la dernière version de l'application à partir de la branche master, vous pouvez également la télécharger depuis les artefacts de build [ici](https://github.com/ob-f/OpenBot/actions?query=workflow%3A%22Java+CI+with+Gradle%22). Notez qu'elle peut ne pas être stable. Si vous souhaitez apporter des modifications à l'application plus tard, suivez les étapes ci-dessous pour compiler l'application et la déployer sur votre téléphone.

> **Remarque :** L'application de contrôle est l'[application Flutter Controller](../controller/flutter/README.fr-FR.md) ; l'ancienne application de contrôle Android qui se trouvait dans ce dossier a été supprimée. Le contrôleur Flutter utilise un identifiant de package différent (`org.openbot.app.controller` au lieu de `org.openbot.controller`) ; il ne mettra donc **pas** à jour une installation existante de l'ancien contrôleur. Si l'ancien contrôleur est encore installé, installez le nouveau séparément et désinstallez l'ancienne application.

## Compiler les applications

### Prérequis

- [Android Studio Electric Eel | 2022.1.1 ou plus récent](https://developer.android.com/studio/index.html) pour compiler et installer les fichiers apk.
- Un appareil Android et un environnement de développement Android avec un API minimum de 21.
- Actuellement, nous utilisons l'API 33 comme SDK de compilation et l'API 32 comme SDK cible. Il devrait s'installer automatiquement, mais si ce n'est pas le cas, vous pouvez installer le SDK manuellement. Allez dans Android Studio -> Préférences -> Apparence et comportement -> Paramètres système -> SDK Android. Assurez-vous que l'API 33 est cochée et cliquez sur appliquer.

![SDK Android](../docs/images/android_studio_sdk.jpg)

### Processus de compilation

1. Ouvrez Android Studio et sélectionnez *Ouvrir un projet Android Studio existant*.
2. Sélectionnez le répertoire OpenBot/android et cliquez sur OK.
3. Si vous souhaitez installer l'[application OpenBot](app/README.md), assurez-vous de sélectionner la configuration *app*. Confirmez la synchronisation Gradle si nécessaire. Pour effectuer une synchronisation Gradle manuellement, cliquez sur l'icône Gradle.
  ![Synchronisation Gradle](../docs/images/android_studio_bar_gradle.jpg)
4. Connectez votre appareil Android et assurez-vous que le débogage USB dans les [options développeur](https://developer.android.com/studio/debug/dev-options) est activé. Selon votre environnement de développement, [d'autres étapes](https://developer.android.com/studio/run/device) peuvent être nécessaires. Vous devriez maintenant voir votre appareil dans la barre de navigation en haut.
  ![Téléphone](../docs/images/android_studio_bar_phone.jpg)
5. Cliquez sur le bouton Exécuter (la flèche verte) ou sélectionnez Exécuter > Exécuter 'android' dans le menu supérieur. Vous devrez peut-être reconstruire le projet en utilisant Build > Rebuild Project.
  ![Exécuter](../docs/images/android_studio_bar_run.jpg)
6. Si on vous demande d'utiliser Instant Run, cliquez sur *Continuer sans Instant Run*.

### Dépannage

#### Versions

Si vous recevez un message tel que `Le projet utilise une version incompatible (AGP 7.4.0) du plugin Gradle Android. La dernière version prise en charge est AGP 7.3.0`, vous devez mettre à jour Android Studio ou rétrograder votre plugin Gradle. Vous pouvez en savoir plus sur la compatibilité des versions entre Android Studio et le plugin Gradle [ici](https://developer.android.com/studio/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility).