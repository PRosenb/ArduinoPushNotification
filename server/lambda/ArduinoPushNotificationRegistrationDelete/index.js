const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = (event, context, callback) => {
    var installationId = event.pathParameters.installationId;
    if (installationId == null) {
        errorResponse("No argument installationId.", callback);
        return;
    }

    var params = {
        TableName: 'ArduinoPushNotification',
        Key: {
            'InstallationId': { S: installationId },
        },
    };

    ddb.deleteItem(params, function(err, data) {
        if (err) {
            console.error("Delete failed:", JSON.stringify(err, null, 2));
            errorResponse("Delete failed", callback)
        }
        else {
            console.log("Delete success:", JSON.stringify(data));
            callback(null, {
                statusCode: 201,
                body: JSON.stringify({
                    installationId: installationId
                }),
                headers: {
                    'Access-Control-Allow-Origin': '*',
                },
            });
        }
    });
};

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
