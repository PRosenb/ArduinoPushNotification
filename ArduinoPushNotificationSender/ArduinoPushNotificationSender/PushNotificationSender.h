#include <Arduino.h>

class PushNotificationSender {
public:
PushNotificationSender(String url, String installationId);
virtual ~PushNotificationSender(){
}

void send(String messageTitle, String messageBody);

private:
const String url;
const String installationId;
};
