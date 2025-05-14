import { APIGatewayEvent, Context } from "aws-lambda";

import * as AWS from "aws-sdk";

var ddb = new AWS.DynamoDB.DocumentClient();
const CONNECTIONS_TABLE = process.env.CONNECTIONS_TABLE!;

export const handler = async (event: APIGatewayEvent, _context: Context) => {
  const connectionId = event.requestContext.connectionId;
  const username = event.queryStringParameters?.username;

  if (!username)
    return {
      statusCode: 400,
      body: "Missing username in query string",
    };

  console.log(`New connection: ${connectionId} from user: ${username}`);

  try {
    await ddb
      .put({
        TableName: CONNECTIONS_TABLE,
        Item: {
          username: username,
          connectionId: connectionId,
        },
      })
      .promise();

    return {
      statusCode: 200,
      body: "Connected",
    };
  } catch {
    return {
      statusCode: 500,
      body: "Failed to connect to websocket!",
    };
  }
};
