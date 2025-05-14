import { DynamoDB } from "aws-sdk";

const dynamoDb = new DynamoDB.DocumentClient();
const CHAT_DATA_TABLE = process.env.CHAT_DATA_TABLE!;

export const handler = async (event: any) => {
  let body;

  try {
    body = event.body ? JSON.parse(event.body) : {};
  } catch (error) {
    return {
      statusCode: 400,
      body: JSON.stringify({ message: "Invalid JSON in request body" }),
    };
  }

  const { username, chatId, timestamp } = body;

  if (!username || !chatId || !timestamp) {
    return {
      statusCode: 400,
      body: JSON.stringify({
        message: "Missing username, chatId, or timestamp",
      }),
    };
  }

  try {
    await dynamoDb
      .update({
        TableName: CHAT_DATA_TABLE,
        Key: {
          PK: `USER#${username}`,
          SK: `CHAT#${chatId}`,
        },
        UpdateExpression: "set seenTimestamp = :seenTimestamp",
        ExpressionAttributeValues: {
          ":seenTimestamp": timestamp,
        },
      })
      .promise();

    return {
      statusCode: 200,
      body: JSON.stringify({ message: "Chat marked as seen" }),
    };
  } catch (error) {
    console.error("Error updating chat:", error);
    return {
      statusCode: 500,
      body: JSON.stringify({ message: "Failed to mark chat as seen" }),
    };
  }
};
