const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
    var deviceToken = null;
    const body = event.body;
    if (body != null) {
        deviceToken = JSON.parse(body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        return getErrorResponse("No argument deviceToken.");
    }

    const result = await queryByDeviceToken(deviceToken, context);
    return {
        statusCode: 201,
        body: JSON.stringify(result),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    };
};

async function queryByDeviceToken(deviceToken, context) {
    try {
        var readParams = {
            TableName: 'ArduinoPushNotification',
            IndexName: 'DeviceToken-index',
            KeyConditionExpression: 'DeviceToken = :devToken',
            ExpressionAttributeValues: { ":devToken": { S: deviceToken } },
        };
        const result = await ddb.query(readParams).promise();
        var item = null;
        if (result.Items.length > 0) {
            item = result.Items[0];
        }
        if (item != null) {
            var deviceTokenFromDb = item["DeviceToken"].S;
            console.log("deviceTokenFromDb: ", deviceTokenFromDb);
            var installationId = item["InstallationId"].S;
            console.log("installationId: ", installationId);
            return await updateDb(installationId, deviceToken);
        }
        else {
            console.log("create new entry");
            return await updateDb(context.awsRequestId, deviceToken);
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
        await ddb.putItem(writeParams).promise();
        return {
            installationId: installationId,
        };
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
