const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = (event, context, callback) => {
    var installationId = event.pathParameters.installationId;
    const body = event.body;
    var deviceToken = null;
    if (body != null) {
        deviceToken = JSON.parse(body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        errorResponse("No argument registrationToken.", callback);
        return;
    }

    if (installationId == null) {
         errorResponse("No argument installationId.", callback);
         return;
    }
    queryByInstallationId(installationId, deviceToken, context, callback);
};

function queryByInstallationId(installationId, deviceToken, context, callback) {
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
            const item = result.Item;
            if (item != undefined && item != null) {
                var deviceTokenFromDb = item["DeviceToken"].S;
                console.log("deviceTokenFromDb: ", deviceTokenFromDb);
                var installationId = item["InstallationId"].S;
                console.log("installationId: ", installationId);
                updateDb(installationId, deviceToken, callback);
            }
            else {
                errorResponse("Invalid installationId", callback);
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
