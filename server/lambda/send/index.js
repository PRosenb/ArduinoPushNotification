const http = require('http');
const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();
// const firebase = require('firebase-admin');

exports.handler = (event, context, callback) => {
    var installationId = null;
    var message = null;
    if (event.body != null) {
        const requestBody = JSON.parse(event.body);
        installationId = requestBody.installationId;
        message = requestBody.message;
        console.log(JSON.stringify(message));
    }
    // allow get parameter as well
    if (installationId == undefined || installationId == null) {
        if (event.queryStringParameters != null) {
            installationId = event.queryStringParameters.installationId
        }
    }
    if (installationId == undefined || installationId == null) {
        errorResponse("parameter installationId missing", callback);
        return;
    }

    var params = {
        TableName: 'ArduinoPushNotification',
        Key: {
            'InstallationId': { S: installationId },
        },
    };
    ddb.getItem(params, function(err, result) {
        if (err) {
            console.log("DB read error", err);
            errorResponse("DB read error", callback);
        }
        else {
            if (result.Item !== undefined && result.Item !== null) {
                var deviceToken = result.Item["DeviceToken"].S;
                sendPushNotification(deviceToken, message, callback).then(() => {
                    console.log("send push success");
                }).catch((err) => {
                    console.error(err);

                    // If there is an error during processing, catch it and return
                    // from the Lambda function successfully. Specify a 500 HTTP status
                    // code and provide an error message in the body. This will provide a
                    // more meaningful error response to the end client.
                    errorResponse(err.message, callback);
                });
            }
            else {
                errorResponse("unknown deviceToken", callback);
            }
        }
    });
};

function sendResponse(callback, body) {
    callback(null, {
        statusCode: 201,
        body: body,
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    });
}

function errorResponse(errorMessage, callback) {
    callback(null, {
        statusCode: 500,
        body: JSON.stringify({
            Error: errorMessage,
        }),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    });
}

// function sendPushNotificationNew() {
// var message = {
//   data: {
//     score: '850',
//     time: '2:45'
//   },
//   token: "dyoo-g45DbE:APA91bGx6NWpjqp9Min5O3GAkyO3FGcJSF-cKNtEQSiaxX60-DFzOIuYfpeSTQd_Hwhp-Bg7MWbDY707STpJdgWqrSS6NvsFrSX9aeD8uupql9cMgfbFJX9h0bBpHeEQvx0PRjMCzDt6"
// };

// // Send a message to the device corresponding to the provided
// // registration token.
// firebase.messaging().send(message)
//   .then((response) => {
//     // Response is a message ID string.
//     console.log('Successfully sent message:', response);
//   })
//   .catch((error) => {
//     console.log('Error sending message:', error);
//   });
// }

function sendPushNotification(deviceToken, message, callback) {
    return new Promise((resolve, reject) => {
        const options = {
            host: "fcm.googleapis.com",
            path: "/fcm/send",
            method: 'POST',
            headers: {
                'Authorization': process.env.authorization,
                'Content-Type': 'application/json'
            }
        };

        const httpCallback = function(response) {
            var data = "";
            response.on('data', (chunk) => {
                data += chunk;
            });
            response.on('end', () => {
                console.log("data: ", data);
                var parsedData = JSON.parse(data);
                if (parsedData.total_pages !== null) {
                    console.log("success: ", parsedData.success);
                    console.log("message_id: ", parsedData.results[0].message_id);
                    if (parsedData.success == 1) {
                        sendResponse(callback, JSON.stringify({
                            success: parsedData.success,
                            message_id: parsedData.results[0].message_id
                        }));
                    }
                    else {
                        var error = parsedData.results[0].error;
                        if (error == null || error == undefined) {
                            error = "unknown error";
                        }
                        errorResponse(error, callback);
                    }
                }
                else {
                    sendResponse(callback, JSON.stringify({
                        error: "unknown response"
                    }));
                }
            });

            response.on('error', (e) => {
                reject(e.message);
            });
        }
        var req = http.request(options, httpCallback);

        message.to = deviceToken;
        if (message.priority == undefined) {
            message.priority = "high";
        }

        req.write(JSON.stringify(message));
        req.end();
    });
}
