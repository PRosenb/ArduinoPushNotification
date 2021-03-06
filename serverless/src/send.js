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
            return getErrorResponse(400, "Missing installationId");
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
            return getErrorResponse(400, "Missing message, title or body");
        }
    }
    catch (err) {
        console.log("parse params error: ", err);
        return getErrorResponse(400, "parameter parsing error");
    }

    try {
        await updateUsage(installationId);
    }
    catch (err) {
        console.log("update usgae error", err);
    }
    try {
        var params = {
            TableName: process.env.DYNAMODB_TABLE,
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
            return getErrorResponse(404, "deviceToken not found");
        }
    }
    catch (err) {
        console.log("DB read error", err);
        return getErrorResponse(500, "DB read error");
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
                'Authorization': process.env.FIREBASE_AUTHORIZATION,
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
                    statusCode: 200,
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
                return getErrorResponse(500, error);
            }
        }
        else {
            return getErrorResponse(500, "Unknown response from FCM");
        }
    }
    catch (err) {
        console.log("send push notification error", err);
        return getErrorResponse(500, "Send push notification error");
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

const MONTH_ABBREVIATIONS = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];

async function updateUsage(installationId) {
    try {
        const now = new Date();
        const month = MONTH_ABBREVIATIONS[now.getMonth()];
        const timestamp = now.toISOString();

        var readParams = {
            TableName: process.env.DYNAMODB_TABLE_USAGE,
            Key: {
                'InstallationId': { S: installationId },
                'Month': { S: month },
            },
        };
        const dbResult = await ddb.getItem(readParams).promise();
        var usageCount = Number(1);
        if (dbResult.Item !== undefined && dbResult.Item !== null) {
            const previousCount = dbResult.Item["Count"].N;
            usageCount += Number(previousCount);
        }

        var writeParams = {
            TableName: process.env.DYNAMODB_TABLE_USAGE,
            Item: {
                'InstallationId': { S: installationId },
                'Month': { S: month },
                'UsageCount': { N: usageCount.toString() },
                'LastUsage': { S: timestamp },
            }
        };
        await ddb.putItem(writeParams).promise();
    }
    catch (err) {
        console.log("DB statistics write error", err);
        return getErrorResponse(500, "DB write error");
    }
}

function getErrorResponse(errorCode, errorMessage) {
    return {
        statusCode: errorCode,
        body: JSON.stringify({
            error: errorMessage,
        }),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    };
}
