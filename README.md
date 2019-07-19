# Arduino Push Notification
https://github.com/PRosenb/ArduinoPushNotification

This project implements a simple solution to send push notifications from an `Arduino` based `ESP32` or `ESP8266` to 
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
- [Arduino Sample](ArduinoPushNotificationClient/src/main.cpp)
- [Android App](app)

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