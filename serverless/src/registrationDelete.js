const AWS = require('aws-sdk');
const ddb = new AWS.DynamoDB();

exports.handler = async(event, context) => {
//     return {
//         statusCode: 200,
//         body: JSON.stringify({
//             input: event,
//         }),
//     };
    try {
        var installationId = null;
        const pathParameters = event.pathParameters;
        if (pathParameters != null) {
            installationId = pathParameters.installationId;
        }
        if (installationId == null) {
            return getErrorResponse(400, "Missing installationId");
        }

        var params = {
            TableName: process.env.DYNAMODB_TABLE,
            Key: {
                'InstallationId': { S: installationId },
            },
        };
        const data = await ddb.deleteItem(params).promise();
        console.log("Delete success:", JSON.stringify(data));
        return {
            statusCode: 200,
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
        return getErrorResponse(500, "Delete failed: ", JSON.stringify(err));
    }
};

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
