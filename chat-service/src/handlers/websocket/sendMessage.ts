import * as AWS from "aws-sdk";

const ddb = new AWS.DynamoDB.DocumentClient();
const apiGateway = new AWS.ApiGatewayManagementApi({
  endpoint: process.env.WEBSOCKET_API_ENDPOINT, // e.g., "abc123.execute-api.region.amazonaws.com/dev"
});

const chatDataTable = process.env.CHAT_DATA_TABLE!;
const connectionsTable = process.env.CONNECTIONS_TABLE!;

export const handler = async (event: any) => {
  const { from, to, message } = JSON.parse(event.body ?? "{}");

  const [user1, user2] = [from, to].sort();
  const chatId = `${user1}#${user2}`;
  const timestamp = Date.now();

  // Save the message to DynamoDB
  try {
    await saveMessageToDynamoDB(from, to, chatId, message, timestamp);
  } catch (error) {
    console.error("Failed to store message:", error);
    return {
      statusCode: 500,
      body: JSON.stringify({ error: "Failed to store message." }),
    };
  }

  // Send the message via WebSocket
  try {
    await sendMessageToReceiver(to, {
      from,
      message,
      timestamp,
      chatId,
      action: "newMessage",
    });
  } catch (error) {
    console.error("Failed to send WebSocket message:", error);
    // maybe store in a retry queue failed messages
  }

  return {
    statusCode: 200,
    body: JSON.stringify({ message: "Message sent and stored." }),
  };
};

// Save chat message and metadata to DynamoDB
async function saveMessageToDynamoDB(
  from: string,
  to: string,
  chatId: string,
  message: string,
  timestamp: number
) {
  const messageItem = {
    PK: `CHAT#${chatId}`,
    SK: `MESSAGE#${timestamp}`,
    senderId: from,
    receiverId: to,
    messageBody: message,
    timestamp,
  };

  const chatForUser1 = {
    PK: `USER#${from}`,
    SK: `CHAT#${chatId}`,
    chatId,
    otherUser: to,
    lastMessageSentTimestamp: timestamp,
    seenTimestamp: timestamp,
    messageBody: message,
    lastSender: from,
    GSI1PK: `USER#${from}`,
    GSI1SK: timestamp,
  };

  const chatForUser2 = {
    PK: `USER#${to}`,
    SK: `CHAT#${chatId}`,
    chatId,
    otherUser: from,
    lastMessageSentTimestamp: timestamp,
    seenTimestamp: 0,
    messageBody: message,
    lastSender: from,
    GSI1PK: `USER#${to}`,
    GSI1SK: timestamp,
  };

  await Promise.all([
    ddb.put({ TableName: chatDataTable, Item: messageItem }).promise(),
    ddb.put({ TableName: chatDataTable, Item: chatForUser1 }).promise(),
    ddb.put({ TableName: chatDataTable, Item: chatForUser2 }).promise(),
  ]);
}

// Send message to the receiver’s WebSocket connection
async function sendMessageToReceiver(username: string, message: any) {
  // Get connectionIds for the receiver
  const result = await ddb
    .query({
      TableName: connectionsTable,
      KeyConditionExpression: "username = :u",
      ExpressionAttributeValues: {
        ":u": username,
      },
    })
    .promise();

  const connections = result.Items ?? [];

  // if no connection send push notification -> later, need to check how to retrieve the token

  await Promise.all(
    connections.map((conn) =>
      apiGateway
        .postToConnection({
          ConnectionId: conn.connectionId,
          Data: JSON.stringify(message),
        })
        .promise()
        .catch(async (err) => {
          console.warn(`Failed to send to ${conn.connectionId}:`, err);

          if (err.statusCode === 410)
            await ddb
              .delete({
                TableName: connectionsTable,
                Key: {
                  PK: `USER#${username}`,
                  SK: `CONNECTION#${conn.connectionId}`,
                },
              })
              .promise();
        })
    )
  );
}
