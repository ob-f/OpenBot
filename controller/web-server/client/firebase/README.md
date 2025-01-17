## Firebase Google Sign-In Authentication

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

- #### Usage
  In our web application, we use Firebase for Google sign-in authentication to identify each unique client and prevent cross-connections between the robot app and the web server. If you clone this project and run on your device, you must set up your own Firebase project because the
  firebase configuration is required for sign-in authentication.
- #### About  Google Sign-In
  Firebase Google Sign-In Authentication is a feature of the Firebase platform that allows users to sign in to mobile or
  web apps using their Google credentials. This service provides a secure and convenient way for users to access apps
  without having to remember and manage separate login credentials. Firebase manages the entire authentication process,
  from verifying the user's identity with Google to providing a unique user ID that can be used to personalize the user'
  s experience within the app. This feature also includes additional security measures, such as two-factor
  authentication, to help protect user accounts from unauthorized access.

**Note** - Please follow the OpenBot playground [documentation](../../../../open-code/src/services/README.md) to set up your Firebase project and enabling google authentication. No need to enable Google Drive API for now.

### Setting up Environment Variables

Using Environment Variables When using Firebase Authentication, you may need to store sensitive information such as API keys, database credentials, and other secrets. To do this securely, you can use environment variables to store this information outside your code. by doing following steps.

1. Create a new file in web-server called .env.

   <img src="../../images/firebase_web_server_env_variable.png" width="30%"/>

2. Add following environment variables to the .env file that will be used in authentication.js file.
      ```bash REACT_APP_FIREBASE_API_KEY=<REACT_APP_FIREBASE_API_KEY>
       SNOWPACK_PUBLIC_FIREBASE_API_KEY=<SNOWPACK_PUBLIC_FIREBASE_API_KEY>
       SNOWPACK_PUBLIC_AUTH_DOMAIN=<SNOWPACK_PUBLIC_AUTH_DOMAIN>
       SNOWPACK_PUBLIC_PROJECT_ID=<SNOWPACK_PUBLIC_PROJECT_ID>
       SNOWPACK_PUBLIC_STORAGE_BUCKET=<SNOWPACK_PUBLIC_STORAGE_BUCKET>
       SNOWPACK_PUBLIC_MESSAGING_SENDER_ID=<SNOWPACK_PUBLIC_MESSAGING_SENDER_ID>
       SNOWPACK_PUBLIC_APP_ID=<SNOWPACK_PUBLIC_APP_ID>
       SNOWPACK_PUBLIC_MEASUREMENT_ID=<SNOWPACK_PUBLIC_MEASUREMENT_ID> 
   ```






