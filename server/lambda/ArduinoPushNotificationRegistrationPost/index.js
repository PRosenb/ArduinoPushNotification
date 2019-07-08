const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = (event, context, callback) => {
    const body = event.body;
    var deviceToken = null;
    if (body != null) {
        deviceToken = JSON.parse(event.body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        errorResponse("No argument deviceToken.", callback);
        return;
    }
    queryByDeviceToken(deviceToken, context, callback);
};

function queryByDeviceToken(deviceToken, context, callback) {
    var readParams = {
        TableName: 'ArduinoPushNotification',
        IndexName: 'DeviceToken-index',
        KeyConditionExpression: 'DeviceToken = :devToken',
        ExpressionAttributeValues: { ":devToken": { S: deviceToken } },
    };
    ddb.query(readParams, function(err, result) {
        if (err) {
            console.log("DB read error", err);
            errorResponse("DB read error", callback);
        }
        else {
            var item = null;
            if (result.Items.length > 0) {
                item = result.Items[0];
            }
            if (item != null) {
                var deviceTokenFromDb = item["DeviceToken"].S;
                console.log("deviceTokenFromDb: ", deviceTokenFromDb);
                var installationId = item["InstallationId"].S;
                console.log("installationId: ", installationId);
                updateDb(installationId, deviceToken, callback);
            }
            else {
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
            console.log("DB write error", err);
            errorResponse("DB write error", callback);
        }
        else {
            var result = {
                installationId: installationId,
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

function errorResponse(errorMessage, callback) {
    callback(null, {
        statusCode: 500,
        body: JSON.stringify({
            error: errorMessage,
        }),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    });
}
