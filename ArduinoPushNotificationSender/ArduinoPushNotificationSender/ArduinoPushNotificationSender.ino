#include <Arduino.h>

#include "PushNotificationSender.h"

#if defined(ESP8266)
#include <ESP8266WiFi.h>
#else
#include <WiFi.h>
#endif

#define WIFI_SSID "<your wifi ssid>"
#define WIFI_PASSWORD "<your wifi password>"

#define PUSH_URL "https://pdymdkbjtg.execute-api.us-east-1.amazonaws.com/v1/send/"
#define INSTALLATION_ID "<your installationId"

PushNotificationSender pushNotificationSender(PUSH_URL, INSTALLATION_ID);

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

	pushNotificationSender.send("Title 1", "Body 1");

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
