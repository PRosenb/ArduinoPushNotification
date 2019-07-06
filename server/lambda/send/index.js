const http = require('http');
const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();
// const firebase = require('firebase-admin');

exports.handler = (event, context, callback) => {
    // if (!event.requestContext.authorizer) {
    //   errorResponse('Authorization not configured', context.awsRequestId, callback);
    //   return;
    // }

    console.log('Received event ', event);

    if (event.body != null) {
        const requestBody = JSON.parse(event.body);
        var installationId = requestBody.installationId;
    }
    if (installationId == undefined || installationId == null) {
        if (event.queryStringParameters != null) {
            installationId = event.queryStringParameters.installationId
        }
    }
    if (installationId == undefined || installationId == null) {
        errorResponse("parameter installationId missing", context.awsRequestId, callback);
        return;
    }
    console.log("installationId: ", installationId);
    // deviceToken = "dyoo-g45DbE:APA91bGx6NWpjqp9Min5O3GAkyO3FGcJSF-cKNtEQSiaxX60-DFzOIuYfpeSTQd_Hwhp-Bg7MWbDY707STpJdgWqrSS6NvsFrSX9aeD8uupql9cMgfbFJX9h0bBpHeEQvx0PRjMCzDt6";
    // console.log("deviceToken: ", deviceToken);

    // var params0 = {
    //   TableName: 'ArduinoPushNotification',
    //   Item: {
    //     'id' : {S: id},
    //   }
    // };

    // // Call DynamoDB to add the item to the table
    // ddb.putItem(params0, function(err, data) {
    //   if (err) {
    //     console.log("Create Error", err);
    //   } else {
    //     console.log("Create Success", data);
    //   }
    // });

    var params = {
        TableName: 'ArduinoPushNotification',
        Key: {
            'InstallationId': { S: installationId },
        },
        // ProjectionExpression: 'id,deviceToken'
    };
    // Call DynamoDB to read the item from the table
    ddb.getItem(params, function(err, result) {
        if (err) {
            console.log("Error", err);
            errorResponse("DB read error", context.awsRequestId, callback);
        }
        else {
            console.log("Success", result);
            if (result.Item !== undefined && result.Item !== null) {
                console.log("found2");
                var deviceToken = result.Item["DeviceToken"].S;
                console.log("token: ", deviceToken);
                // const deviceToken = "dyoo-g45DbE:APA91bGx6NWpjqp9Min5O3GAkyO3FGcJSF-cKNtEQSiaxX60-DFzOIuYfpeSTQd_Hwhp-Bg7MWbDY707STpJdgWqrSS6NvsFrSX9aeD8uupql9cMgfbFJX9h0bBpHeEQvx0PRjMCzDt6";
                sendPushNotification(deviceToken, event, context, callback).then(() => {
                    console.log("send push success");
                }).catch((err) => {
                    console.error(err);

                    // If there is an error during processing, catch it and return
                    // from the Lambda function successfully. Specify a 500 HTTP status
                    // code and provide an error message in the body. This will provide a
                    // more meaningful error response to the end client.
                    errorResponse(err.message, context.awsRequestId, callback);
                });
            }
            else {
                errorResponse("unknown deviceToken", context.awsRequestId, callback);
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

function errorResponse(errorMessage, awsRequestId, callback) {
    callback(null, {
        statusCode: 500,
        body: JSON.stringify({
            Error: errorMessage,
            Reference: awsRequestId,
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

function sendPushNotification(deviceToken, event, context, callback) {
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
                    sendResponse(callback, JSON.stringify({
                        success: parsedData.success,
                        message_id: parsedData.results[0].message_id
                    }));
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

        const reqBody =
            '{' +
            '  "to":"' + deviceToken + '", ' +
            '  "priority" : "high", ' +
            // '  "data" : {"value":"7.5"}, ' +
            '  "data" : {' +
            '    "message": "Portugal vs. Denmark message",' +
            '    "title": "Portugal vs. Denmark",' +
            '    "body": "great match!"' +
            '  }' +
            '}';

        req.write(reqBody);
        req.end();
    });
}
