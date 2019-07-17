#include <Arduino.h>

#include <HTTPClient.h>       // https://github.com/espressif/arduino-esp32/tree/master/libraries/HTTPClient
#include <WiFiClientSecure.h> // https://github.com/espressif/arduino-esp32/tree/master/libraries/WiFiClientSecure

#define WIFI_SSID "<wifi ssid>"
#define WIFI_PASSWORD "<wifi password>"

#define PUSH_URL "https://18l25pjesb.execute-api.us-east-1.amazonaws.com/dev/send/"
#define INSTALLATION_ID "<installation id>"

WiFiClientSecure client;

void sendPushNotificationByGet(String messageTitle, String messageBody, String installationId, String baseUrl) {
	String url = baseUrl;
	url += "?";
	url += "installationId=";
	url += installationId;
	url += "&title=";
	url += messageTitle;
	url += "&body=";
	url += messageBody;

	HTTPClient https;
	if (https.begin(client, url)) {
		int httpCode = https.GET();
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
	} else {
		Serial.println("Connection failed");
	}
}

void sendPushNotificationByPost(String messageTitle, String messageBody, String installationId, String url);

void setup() {
	Serial.begin(115200);
	delay(100);

	Serial.print("Attempting to connect to SSID: ");
	Serial.println(WIFI_SSID);
	WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

	while (WiFi.status() != WL_CONNECTED) {
		Serial.print(".");
		// wait 1 second for re-trying
		delay(1000);
	}
	Serial.println(" Wifi connected.");

	String messageTitle = "title2";
	String messageBody = "body2";
	sendPushNotificationByGet(messageTitle, messageBody, INSTALLATION_ID, PUSH_URL);

	// start deepsleep until external restart
	esp_deep_sleep_start();
}

void loop() {
	// do nothing
}

void sendPushNotificationByPost(String messageTitle, String messageBody, String installationId, String url) {
	String payload = "";
	payload += "{\"installationId\":\"";
	payload += installationId;
	payload += "\", \"message\":{";
	payload += "\"title\":\"";
	payload += messageTitle;
	payload += "\", \"body\":\"";
	payload += messageBody;
	payload += "\"";
	payload += "}";
	payload += "}";
	Serial.println(payload);

	HTTPClient https;
	if (https.begin(client, url)) {
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
	} else {
		Serial.println("Connection failed");
	}
}
