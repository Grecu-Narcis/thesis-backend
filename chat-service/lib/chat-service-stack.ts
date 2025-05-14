import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import * as dynamodb from "aws-cdk-lib/aws-dynamodb";
import * as apigateway from "aws-cdk-lib/aws-apigateway";
import * as iam from "aws-cdk-lib/aws-iam";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { Runtime } from "aws-cdk-lib/aws-lambda";

export class ChatServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const chatDataTable = dynamodb.Table.fromTableName(
      this,
      "chatDataTable",
      "ChatData"
    );

    const connectionsTable = dynamodb.Table.fromTableName(
      this,
      "ConnectionsTable",
      "WebSocketConnections"
    );

    // Lambda for retrieving chats
    const getChatsForUser = new NodejsFunction(this, "GetChatsForUserHandler", {
      runtime: Runtime.NODEJS_20_X,
      entry: "src/handlers/getChatsForUser.ts",
      handler: "handler",
      environment: {
        CHAT_DATA_TABLE: chatDataTable.tableName,
      },
    });

    const getMessagesInChat = new NodejsFunction(
      this,
      "GetMessagesInChatHandler",
      {
        runtime: Runtime.NODEJS_20_X,
        entry: "src/handlers/getMessagesInChat.ts",
        handler: "handler",
        environment: {
          CHAT_DATA_TABLE: chatDataTable.tableName,
        },
      }
    );

    const markAsSeen = new NodejsFunction(this, "MarkAsSeenHandler", {
      runtime: Runtime.NODEJS_20_X,
      entry: "src/handlers/markAsSeen.ts",
      handler: "handler",
      environment: {
        CHAT_DATA_TABLE: chatDataTable.tableName,
      },
    });

    chatDataTable.grantWriteData(markAsSeen);

    chatDataTable.grantReadData(getChatsForUser);

    getChatsForUser.addToRolePolicy(
      new iam.PolicyStatement({
        effect: iam.Effect.ALLOW,
        actions: ["dynamodb:Query"],
        resources: [`${chatDataTable.tableArn}/index/*`],
      })
    );

    chatDataTable.grantReadData(getMessagesInChat);

    // api gateway for chats and messages

    const api = new apigateway.RestApi(this, "ChatServiceApi", {
      restApiName: "Chat Service",
    });

    // GET /chats/{username}
    const chats = api.root.addResource("chats");
    const userChats = chats.addResource("{username}");
    userChats.addMethod(
      "GET",
      new apigateway.LambdaIntegration(getChatsForUser)
    );

    // GET /chats/{chatId}/messages
    const messages = api.root
      .addResource("messages")
      .addResource("{user1}")
      .addResource("{user2}");

    // POST /chats/markAsSeen
    const markAsSeenResource = api.root.addResource("markAsSeen");
    markAsSeenResource.addMethod(
      "POST",
      new apigateway.LambdaIntegration(markAsSeen)
    );

    messages.addMethod(
      "GET",
      new apigateway.LambdaIntegration(getMessagesInChat)
    );
  }
}
