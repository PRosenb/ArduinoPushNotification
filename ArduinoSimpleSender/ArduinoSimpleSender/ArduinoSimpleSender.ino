#include <Arduino.h>

#if defined(ESP8266)
#include <ESP8266WiFi.h>       // https://github.com/esp8266/Arduino/tree/master/libraries/ESP8266WiFi
#include <ESP8266HTTPClient.h> // https://github.com/esp8266/Arduino/tree/master/libraries/ESP8266HTTPClient
#else
#include <HTTPClient.h>        // https://github.com/espressif/arduino-esp32/tree/master/libraries/HTTPClient
#include <WiFiClientSecure.h>  // https://github.com/espressif/arduino-esp32/tree/master/libraries/WiFiClientSecure
#endif

#define WIFI_SSID "<your wifi ssid>"
#define WIFI_PASSWORD "<your wifi password>"

#define PUSH_URL "https://pdymdkbjtg.execute-api.us-east-1.amazonaws.com/v1/send/"
#define INSTALLATION_ID "<your installationId"

void sendPushNotification(String messageTitle, String messageBody) {
	String payload = "";
	payload += "{\"installationId\":\"";
	payload += INSTALLATION_ID;
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

	WiFiClientSecure secureClient;
	#if defined(ESP8266)
	secureClient.setInsecure();
	#endif

	HTTPClient https;
	int beginResult = https.begin(secureClient, PUSH_URL);
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

void connectWifi() {
	Serial.print("Attempting to connect to SSID: ");
	Serial.println(WIFI_SSID);
	WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

	while (WiFi.status() != WL_CONNECTED) {
		Serial.print(".");
		// wait 1 second for re-trying
		delay(1000);
	}
	Serial.println(" Wifi connected.");
}

void setup() {
	Serial.begin(115200);
	delay(100);

	connectWifi();
	sendPushNotification("Title 1", "Body 1");

	// start deepsleep until external restart
	#if defined(ESP8266)
	ESP.deepSleep(0);
	#else
	esp_deep_sleep_start();
	#endif
}

void loop() {
	// do nothing
}
