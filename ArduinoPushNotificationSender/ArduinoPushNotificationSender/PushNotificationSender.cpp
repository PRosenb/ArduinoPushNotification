#include "PushNotificationSender.h"

#if defined(ESP8266)
#include <ESP8266WiFi.h>       // https://github.com/esp8266/Arduino/tree/master/libraries/ESP8266WiFi
#include <ESP8266HTTPClient.h> // https://github.com/esp8266/Arduino/tree/master/libraries/ESP8266HTTPClient
#else
#include <HTTPClient.h>        // https://github.com/espressif/arduino-esp32/tree/master/libraries/HTTPClient
#include <WiFiClientSecure.h>  // https://github.com/espressif/arduino-esp32/tree/master/libraries/WiFiClientSecure
#endif

PushNotificationSender::PushNotificationSender(String url, String installationId)
	: url(url), installationId(installationId) {
}

void PushNotificationSender::send(String messageTitle, String messageBody) {
	String payload = "";
	payload += "{\"installationId\":\"";
	payload += installationId;
	payload += "\", \"message\":{";
	payload += "\"notification\":{";
	payload += "\"title\":\"";
	payload += messageTitle;
	payload += "\",";
	payload += "\"body\":\"";
	payload += messageBody;
	payload += "\"";
	payload += "}"; // end notification
	payload += "}"; // end message
	payload += "}";
	Serial.println(payload);

	WiFiClientSecure secureClient;
	#if defined(ESP8266)
	secureClient.setInsecure();
	#endif

	HTTPClient https;
	int beginResult = https.begin(secureClient, url);
	if (beginResult) {
		int httpCode = https.POST(payload);
		if (httpCode > 0) {
			Serial.print("Response, HTTP Code: ");
			Serial.print(httpCode);
			Serial.println(", payload:");
			Serial.println(https.getString());
		} else {
			Serial.print("GET failed, error: ");
			Serial.println(https.errorToString(httpCode));
		}
		https.end();
		secureClient.stop();
	} else {
		Serial.println("Connection failed");
	}
}
