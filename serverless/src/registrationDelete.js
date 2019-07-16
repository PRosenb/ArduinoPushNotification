const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
    // return {
    //     statusCode: 200,
    //     body: JSON.stringify({
    //         input: event,
    //     }),
    // };
    try {
        var installationId = null;
        const pathParameters = event.pathParameters;
        if (pathParameters != null) {
            installationId = pathParameters.installationId;
        }
        if (installationId == null) {
            return getErrorResponse("No argument installationId.");
        }

        var params = {
            TableName: 'ArduinoPushNotification',
            Key: {
                'InstallationId': { S: installationId },
            },
        };
        const data = await ddb.deleteItem(params).promise();
        console.log("Delete success:", JSON.stringify(data));
        return {
            statusCode: 201,
            body: JSON.stringify({
                installationId: installationId
            }),
            headers: {
                'Access-Control-Allow-Origin': '*',
            },
        };
    }
    catch (err) {
        console.error("Delete failed:", JSON.stringify(err));
        return getErrorResponse("Delete failed: ", JSON.stringify(err));
    }
};

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
