const http = require('http');
const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
    try {
        var installationId = null;
        var message = null;
        const body = event.body;
        if (body != null) {
            const requestBody = JSON.parse(body);
            installationId = requestBody.installationId;
            message = requestBody.message;
        }
        // allow get parameter as well
        const queryStringParameters = event.queryStringParameters;
        if (installationId == undefined || installationId == null) {
            if (queryStringParameters != null) {
                installationId = queryStringParameters.installationId;
            }
        }
        if (installationId == undefined || installationId == null) {
            return getErrorResponse("parameter installationId missing");
        }
        if (message == null || message == undefined) {
            if (queryStringParameters != null) {
                if (queryStringParameters.title != null || queryStringParameters.body != null) {
                    message = {
                        notification: {
                            title: queryStringParameters.title,
                            body: queryStringParameters.body
                        }
                    }
                }
            }
        }
        if (message == null || message == undefined) {
            return getErrorResponse("parameter message, title or body missing: " + event.queryStringParameters);
        }
    }
    catch (err) {
        console.log("parse params error: ", err);
        return getErrorResponse("parse params error");
    }

    try {
        var params = {
            TableName: 'ArduinoPushNotification',
            Key: {
                'InstallationId': { S: installationId },
            },
        };
        const dbResult = await ddb.getItem(params).promise();
        if (dbResult.Item !== undefined && dbResult.Item !== null) {
            var deviceToken = dbResult.Item["DeviceToken"].S;
            return await sendPushNotification(deviceToken, message);
        }
        else {
            return getErrorResponse("unknown deviceToken");
        }
    }
    catch (err) {
        console.log("DB read error", err);
        return getErrorResponse("DB read error");
    }
};

async function sendPushNotification(deviceToken, message) {
    try {
        message.to = deviceToken;
        if (message.priority == undefined) {
            message.priority = "high";
        }

        const httpOptions = {
            host: "fcm.googleapis.com",
            path: "/fcm/send",
            method: 'POST',
            headers: {
                'Authorization': process.env.AUTHORIZATION,
                'Content-Type': 'application/json'
            }
        };
        const postBodyJson = JSON.stringify(message);
        const response = await sendHttpRequest(httpOptions, postBodyJson);

        var parsedData = JSON.parse(response);
        if (parsedData.total_pages !== null) {
            console.log("success: ", parsedData.success);
            console.log("message_id: ", parsedData.results[0].message_id);
            if (parsedData.success == 1) {
                return {
                    statusCode: 201,
                    body: JSON.stringify({
                        success: parsedData.success,
                        message_id: parsedData.results[0].message_id
                    }),
                    headers: {
                        'Access-Control-Allow-Origin': '*',
                    },
                };
            }
            else {
                var error = parsedData.results[0].error;
                if (error == null || error == undefined) {
                    error = "unknown error";
                }
                return getErrorResponse(error);
            }
        }
        else {
            return getErrorResponse("unknown response");
        }
    }
    catch (err) {
        console.log("send push notification error", err);
        return getErrorResponse("send push notification error");
    }
}

function sendHttpRequest(options, body) {
    return new Promise((resolve, reject) => {
        try {
            const httpCallback = function(response) {
                var data = "";
                response.on('data', (chunk) => {
                    data += chunk;
                });
                response.on('end', () => {
                    resolve(data);
                });
                response.on('error', (err) => {
                    console.log("error sendHttpPostRequest", err);
                    reject(err);
                });
            };
            const request = http.request(options, httpCallback);
            if (body != undefined && body != null) {
                request.write(body);
            }
            request.end();
        }
        catch (err) {
            console.log("error sendHttpPostRequest", err);
            reject(err);
        }
    });
}

function getErrorResponse(errorMessage) {
    return {
        statusCode: 500,
        body: JSON.stringify({
            error: errorMessage,
        }),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    };
}
