import * as AWS from "aws-sdk";

const dynamoDB = new AWS.DynamoDB.DocumentClient();
const tableName = process.env.CHAT_DATA_TABLE!;
const pageSize = 30;

export const handler = async (event: any) => {
  const username = event.pathParameters.username;
  const lastEvaluatedKey = event.queryStringParameters?.lastEvaluatedKey;

  let params: AWS.DynamoDB.DocumentClient.QueryInput = {
    TableName: tableName,
    IndexName: "GSI1",
    KeyConditionExpression: "GSI1PK = :gsi1pk",
    ExpressionAttributeValues: {
      ":gsi1pk": `USER#${username}`,
    },
    ScanIndexForward: false, // descending order by last message sent timestamp
    Limit: pageSize,
  };

  if (lastEvaluatedKey) {
    params.ExclusiveStartKey = JSON.parse(decodeURIComponent(lastEvaluatedKey));
  }

  try {
    const result = await dynamoDB.query(params).promise();
    const response = {
      statusCode: 200,
      body: JSON.stringify({
        chats: result.Items,
        lastEvaluatedKey: result.LastEvaluatedKey
          ? encodeURIComponent(JSON.stringify(result.LastEvaluatedKey))
          : null,
      }),
    };
    return response;
  } catch (error) {
    console.error("Error querying DynamoDB: ", error);
    return {
      statusCode: 500,
      body: JSON.stringify({ message: "Internal Server Error" }),
    };
  }
};
