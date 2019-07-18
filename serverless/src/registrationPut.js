const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
    var installationId = event.pathParameters.installationId;
    if (installationId == null) {
        return getErrorResponse(400, "Missing installationId");
    }

    var deviceToken = null;
    const body = event.body;
    if (body != null) {
        deviceToken = JSON.parse(body).deviceToken;
    }
    if (deviceToken == undefined || deviceToken == null) {
        return getErrorResponse(400, "Missing deviceToken");
    }

    const result = await queryByInstallationId(installationId, deviceToken);
    return {
        statusCode: 200,
        body: JSON.stringify(result),
        headers: {
            'Access-Control-Allow-Origin': '*',
        },
    };
};

async function queryByInstallationId(installationId, deviceToken) {
    try {
        var params = {
            TableName: process.env.DYNAMODB_TABLE,
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
            return getErrorResponse(404, "installationId not found");
        }
    }
    catch (err) {
        console.log("DB read error", err);
        return getErrorResponse(500, "DB read error");
    }
}

async function updateDb(installationId, deviceToken) {
    try {
        var writeParams = {
            TableName: process.env.DYNAMODB_TABLE,
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
