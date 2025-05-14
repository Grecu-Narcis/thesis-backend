import * as AWS from "aws-sdk";

const dynamoDB = new AWS.DynamoDB.DocumentClient();
const tableName = process.env.CHAT_DATA_TABLE!;
const pageSize = 30;

export const handler = async (event: any) => {
  const user1 = event.pathParameters.user1;
  const user2 = event.pathParameters.user2;
  const lastEvaluatedKey = event.queryStringParameters?.lastEvaluatedKey;

  // Sort usernames to ensure consistent chatId format
  const [u1, u2] = [user1, user2].sort();
  const chatId = `${u1}#${u2}`;

  const params: AWS.DynamoDB.DocumentClient.QueryInput = {
    TableName: tableName,
    KeyConditionExpression: "PK = :pk AND begins_with(SK, :skPrefix)",
    ExpressionAttributeValues: {
      ":pk": `CHAT#${chatId}`,
      ":skPrefix": "MESSAGE#",
    },
    ScanIndexForward: false, // newest messages first
    Limit: pageSize,
  };

  if (lastEvaluatedKey) {
    params.ExclusiveStartKey = JSON.parse(decodeURIComponent(lastEvaluatedKey));
  }

  try {
    const result = await dynamoDB.query(params).promise();
    return {
      statusCode: 200,
      body: JSON.stringify({
        messages: result.Items,
        lastEvaluatedKey: result.LastEvaluatedKey
          ? encodeURIComponent(JSON.stringify(result.LastEvaluatedKey))
          : null,
      }),
    };
  } catch (error) {
    console.error("Error querying messages: ", error);
    return {
      statusCode: 500,
      body: JSON.stringify({ message: "Internal Server Error" }),
    };
  }
};
