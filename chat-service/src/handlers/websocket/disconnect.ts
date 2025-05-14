import { APIGatewayEvent } from "aws-lambda";
import * as AWS from "aws-sdk";

const ddb = new AWS.DynamoDB.DocumentClient();
const CONNECTIONS_TABLE = process.env.CONNECTIONS_TABLE!;

export const handler = async (event: APIGatewayEvent) => {
  const connectionId = event.requestContext.connectionId!;
  console.log(`Disconnecting connectionId: ${connectionId}`);

  try {
    // find username
    const queryResult = await ddb
      .query({
        TableName: CONNECTIONS_TABLE,
        IndexName: "ConnectionIdIndex",
        KeyConditionExpression: "connectionId = :connectionId",
        ExpressionAttributeValues: {
          ":connectionId": connectionId,
        },
      })
      .promise();

    if (!queryResult.Items || queryResult.Items.length === 0) {
      console.warn("Connection not found.");
      return { statusCode: 200, body: "No connection found." };
    }

    const { username } = queryResult.Items[0];

    await ddb
      .delete({
        TableName: CONNECTIONS_TABLE,
        Key: {
          username,
          connectionId,
        },
      })
      .promise();

    console.log(`Removed connection for user: ${username}`);

    return { statusCode: 200, body: "Disconnected." };
  } catch (err) {
    console.error("Failed to disconnect:", err);
    return { statusCode: 500, body: "Internal server error" };
  }
};
