import { Stack, StackProps, Duration } from "aws-cdk-lib";
import { Construct } from "constructs";
import * as apigatewayv2 from "aws-cdk-lib/aws-apigatewayv2";
import * as dynamodb from "aws-cdk-lib/aws-dynamodb";
import * as integrations from "aws-cdk-lib/aws-apigatewayv2-integrations";
import * as iam from "aws-cdk-lib/aws-iam";
import * as sqs from "aws-cdk-lib/aws-sqs";
import { WebSocketStage } from "aws-cdk-lib/aws-apigatewayv2";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";

export class WebSocketStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    const notificationsQueue = sqs.Queue.fromQueueArn(
      this,
      "Notifications Queue",
      "arn:aws:sqs:eu-central-1:841162677495:NotificationsQueue"
    );

    const connectionsTable = dynamodb.Table.fromTableName(
      this,
      "ConnectionsTable",
      "WebSocketConnections"
    );

    const chatDataTable = dynamodb.Table.fromTableName(
      this,
      "chatDataTable",
      "ChatData"
    );

    const notificationTokenTable = dynamodb.Table.fromTableName(
      this,
      "NotificationTokensTable",
      "NotificationTokens"
    );

    // Lambda for $connect
    const connectHandler = new NodejsFunction(this, "ConnectHandler", {
      entry: "src/handlers/websocket/connect.ts",
      handler: "handler",
      timeout: Duration.seconds(5),
      environment: {
        CONNECTIONS_TABLE: connectionsTable.tableName,
      },
    });

    // Lambda for $disconnect
    const disconnectHandler = new NodejsFunction(this, "DisconnectHandler", {
      entry: "src/handlers/websocket/disconnect.ts",
      handler: "handler",
      timeout: Duration.seconds(5),
      environment: {
        CONNECTIONS_TABLE: connectionsTable.tableName,
      },
    });

    const sendMessageHandler = new NodejsFunction(this, "SendMessageHandler", {
      entry: "src/handlers/websocket/sendMessage.ts",
      handler: "handler",
      timeout: Duration.seconds(10),
      environment: {
        CONNECTIONS_TABLE: connectionsTable.tableName,
        CHAT_DATA_TABLE: chatDataTable.tableName,
        NOTIFICATION_TOKENS_TABLE: notificationTokenTable.tableName,
        NOTIFICATIONS_QUEUE: notificationsQueue.queueUrl,
      },
    });

    connectionsTable.grantWriteData(connectHandler);

    connectionsTable.grantReadWriteData(disconnectHandler);
    disconnectHandler.addToRolePolicy(
      new iam.PolicyStatement({
        effect: iam.Effect.ALLOW,
        actions: ["dynamodb:Query"],
        resources: [`${connectionsTable.tableArn}/index/*`],
      })
    );

    connectionsTable.grantReadData(sendMessageHandler);
    chatDataTable.grantReadWriteData(sendMessageHandler);
    notificationTokenTable.grantReadData(sendMessageHandler);

    notificationsQueue.grantSendMessages(sendMessageHandler);

    // WebSocket API
    const webSocketApi = new apigatewayv2.WebSocketApi(
      this,
      "ChatWebSocketAPI",
      {
        connectRouteOptions: {
          integration: new integrations.WebSocketLambdaIntegration(
            "ConnectIntegration",
            connectHandler
          ),
        },
        disconnectRouteOptions: {
          integration: new integrations.WebSocketLambdaIntegration(
            "DisconnectIntegration",
            disconnectHandler
          ),
        },
      }
    );

    // Add route for sending messages
    webSocketApi.addRoute("sendMessage", {
      integration: new integrations.WebSocketLambdaIntegration(
        "SendMessageIntegration",
        sendMessageHandler
      ),
    });

    // Deploy stage
    new WebSocketStage(this, "WebSocketStage", {
      webSocketApi,
      stageName: "dev",
      autoDeploy: true,
    });

    const webSocketEndpoint = `${webSocketApi.apiId}.execute-api.${this.region}.amazonaws.com/dev`;
    // Permissions for managing connections
    webSocketApi.grantManageConnections(sendMessageHandler);

    sendMessageHandler.addEnvironment(
      "WEBSOCKET_API_ENDPOINT",
      webSocketEndpoint
    );
  }
}
