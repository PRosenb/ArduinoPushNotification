const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = (event, context, callback) => {
    const body = event.body;
    if (body != null) {
        var deviceToken = JSON.parse(event.body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        var result = {
            error: "No argument registrationToken."
        }
        return {
            statusCode: 500,
            body: JSON.stringify(result),
        };
    }

    queryByIdDeviceToken(deviceToken, context, callback);
};

function queryByIdDeviceToken(deviceToken, context, callback) {
    var readParams = {
        TableName: 'ArduinoPushNotification',
        IndexName: 'DeviceToken-index',
        KeyConditionExpression: 'DeviceToken = :devToken',
        ExpressionAttributeValues: { ":devToken": { S: deviceToken } },
    };
    ddb.query(readParams, function(err, result) {
        if (err) {
            console.log("Error", err);
            // errorResponse("DB read error", context.awsRequestId, callback);
        }
        else {
            console.log("Success", result);
            const item = result.Items[0];
            if (item !== undefined && item !== null) {
                var deviceTokenFromDb = item["DeviceToken"].S;
                console.log("deviceTokenFromDb: ", deviceTokenFromDb);
                var installationId = item["InstallationId"].S;
                console.log("installationId: ", installationId);
                updateDb(installationId, deviceToken, callback);
            }
            else {
                // no entry yet, use unique awsRequestId
                console.log("create new entry");
                updateDb(context.awsRequestId, deviceToken, callback);
            }
        }
    });
}

function updateDb(installationId, deviceToken, callback) {
    var writeParams = {
        TableName: 'ArduinoPushNotification',
        Item: {
            'InstallationId': { S: installationId },
            'DeviceToken': { S: deviceToken },
        }
    };
    ddb.putItem(writeParams, function(err, data) {
        if (err) {
            console.log("Create Error", err);
        }
        else {
            console.log("Create Success", data);
            var result = {
                installationId: installationId,
                deviceToken: deviceToken,
            };

            callback(null, {
                statusCode: 201,
                body: JSON.stringify(result),
                headers: {
                    'Access-Control-Allow-Origin': '*',
                },
            });
        }
    });
}
