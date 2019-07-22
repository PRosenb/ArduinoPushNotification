# Arduino Push Notification
https://github.com/PRosenb/ArduinoPushNotification

This project implements a small solution to send push notifications from an `Arduino` based `ESP32` or `ESP8266` to 
your `Android` device.
It shows how to set up `AWS` with the `Serverless` framework, contains a small `Arduino` sample implementation and the
`Android` app that receives the push notification.

## Features
- `AWS` based backend solution
- Simple `Lambda` functions written in `node.js`
- Database configuration using `DynamoDB`
- `API Gateway` configuration
- `Android` app written in `Kotlin`
- `Arduino` example code for `ESP32` and `ESP8266`
- Good starting point for similar projects

## Components
- [Serverless configuration](serverless/serverless.yml)
- [Lambda functions](serverless/src/)
- [Android App](app)
- [Arduino Sample](ArduinoPushNotificationClient/src/main.cpp)

## Setup Instructions

### AWS
- Install `Serverless` according to their [Getting Started](https://serverless.com/framework/docs/getting-started/) guide
- Give `Serverless` access to your `AWS` account by following the [AWS - Credentials](https://serverless.com/framework/docs/providers/aws/guide/credentials/) guide
  - I recommend initially doing [Quick Setup](https://serverless.com/framework/docs/providers/aws/guide/credentials/#quick-setup) in chapter [Using AWS Access Keys](https://serverless.com/framework/docs/providers/aws/guide/credentials/#using-aws-access-keys)
  - Ignore the note about `self-signed certificate`, we don't need them for this setup
- Clone this repository
  - Open a command shell and navigate to your desired directory. The clone command will create a subfolder `ArduinoPushNotification` in it
  - Execute the git clone command `git clone git@github.com:PRosenb/ArduinoPushNotification.git`
- Deploy the `AWS` configuration
  - Inside of the repository directory structure, navigate to the the directory `serverless`
  - Don't forget to set the credentials environment variables if you haven't done yet (on Windows use `set`)
    - `export AWS_ACCESS_KEY_ID="<your key id>"`
    - `export AWS_SECRET_ACCESS_KEY="<your key>"`
  - Execute the deploy command `serverless deploy`
  - On success, it will print multiple links of the following type:  
    `https://xxxxx.execute-api.us-east-1.amazonaws.com/v1/xxxx`  
  - One of the links is a `GET` endpoint. That one is the easiest to test the setup with
    - Copy the `GET` endpoint link and put it in a browser
    - When it shows `error: "Missing installationId"`, deployment was successful
  - From one of the links, copy the part until and including `v1/`, you'll need it later

### Android App
- Download and install [Android Studio](https://developer.android.com/studio)
- Clone this repository if you haven't done it
- Open `Android Studio`
  - Click Menu `File`->`Open...`
  - Choose the the directory ArduinoPushNotification and open it
- Open the file `app/build.gradle` and change the `applicationId` to your own
- Follow the instructions [Add Firebase to your Android project](https://firebase.google.com/docs/android/setup)
  - use `Option 1`
  - Follow the guide until `Step 3: Add a Firebase configuration file` including  
    `Add the Firebase Android configuration file to your app:`
  - Put the file `google-services.json` here: `app/google-services.json`
- Open the file `‎app/src/main/java/ch/pete/arduinopushnotification/service/Registration.kt`
  - Set the variable `SERVER_URL` to your URL from the `AWS` setup
  - Remove the part after `v1/` and ensure it has a trailing `/`
- Install the app on your Android device with the `Run` button
- Check if it connects to the API correctly by tapping on `Register`
  - It should do an API call, show a generated `Installation Id` and change the button to `Unregister`

### Arduino Setup
- Install [PlatformIO IDE for Atom](https://platformio.org/install/ide?install=atom)
- Open `Atom`
  - Click Menu `File`->`Open...` and choose the directory `ArduinoPushNotificationClient`
- Click the tick icon `Build` on the left to check if it builds
- Open the file `src/main/main.cpp`
  - Set the variable `PUSH_URL` to your `POST` URL from the `AWS` step that ends with `send`
  - set the variable `INSTALLATION_ID` to the value the app shows as `Installation Id`
- Connect your ESP32 or ESP8266 to your PC via `USB`
- Upload the code to it with the arrow icon `Upload`
- On success of all steps above it sends a push notification and the app receives it

## Contributions
Enhancements and improvements are welcome.

## License
```
Arduino Push Notification
Copyright (c) 2019 Peter Rosenberg (https://github.com/PRosenb).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```