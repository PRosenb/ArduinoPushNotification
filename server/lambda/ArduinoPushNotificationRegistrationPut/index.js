const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
    var installationId = event.pathParameters.installationId;
    if (installationId == null) {
        return getErrorResponse("No argument installationId.");
    }

    var deviceToken = null;
    const body = event.body;
    if (body != null) {
        deviceToken = JSON.parse(body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        return getErrorResponse("No argument deviceToken.");
    }

    const result = await queryByInstallationId(installationId, deviceToken);
    return {
        statusCode: 201,
        body: JSON.stringify(result),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    };
};

async function queryByInstallationId(installationId, deviceToken) {
    try {
        var params = {
            TableName: 'ArduinoPushNotification',
            Key: {
                'InstallationId': { S: installationId },
            },
        };
        const result = await ddb.getItem(params).promise();
        const item = result.Item;
        if (item != undefined && item != null) {
            var deviceTokenFromDb = item["DeviceToken"].S;
            console.log("deviceTokenFromDb: ", deviceTokenFromDb);
            var installationId = item["InstallationId"].S;
            console.log("installationId: ", installationId);
            return updateDb(installationId, deviceToken);
        }
        else {
            return getErrorResponse("Invalid installationId");
        }
    }
    catch (err) {
        console.log("DB read error", err);
        return getErrorResponse("DB read error");
    }
}

async function updateDb(installationId, deviceToken) {
    try {
        var writeParams = {
            TableName: 'ArduinoPushNotification',
            Item: {
                'InstallationId': { S: installationId },
                'DeviceToken': { S: deviceToken },
            }
        };
        const data = await ddb.putItem(writeParams).promise();
        var result = {
            installationId: installationId,
        };
        return result;
    }
    catch (err) {
        console.log("DB write error", err);
        return getErrorResponse("DB write error");
    }
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
