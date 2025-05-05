import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import * as sqs from "aws-cdk-lib/aws-sqs";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { Runtime } from "aws-cdk-lib/aws-lambda";
import { SqsEventSource } from "aws-cdk-lib/aws-lambda-event-sources";

export class NotificationServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const notificationsQueue = sqs.Queue.fromQueueArn(
      this,
      "Notifications Queue",
      "arn:aws:sqs:eu-central-1:841162677495:NotificationsQueue"
    );

    const notificationLambda = new NodejsFunction(this, "notificationHandler", {
      runtime: Runtime.NODEJS_20_X,
      entry: "src/handler/notificationHandler.ts",
      handler: "handler",
    });

    notificationsQueue.grantConsumeMessages(notificationLambda);

    notificationLambda.addEventSource(
      new SqsEventSource(notificationsQueue, {
        batchSize: 10,
      })
    );
  }
}
