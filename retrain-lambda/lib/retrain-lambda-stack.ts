import * as lambda from "aws-cdk-lib/aws-lambda";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as s3n from "aws-cdk-lib/aws-s3-notifications";
import * as iam from "aws-cdk-lib/aws-iam";
import { Construct } from "constructs";
import * as cdk from "aws-cdk-lib";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { Duration } from "aws-cdk-lib";

import * as dotenv from "dotenv";
dotenv.config();

export class RetrainLambdaStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const bucketName = process.env.BUCKET_NAME!; // change this
    const flaskApiUrl = process.env.API_URL!; // change this

    const retrainLambda = new NodejsFunction(this, "RetrainTriggerLambda", {
      entry: "src/handlers/retrain_handler.ts",
      handler: "handler",
      runtime: lambda.Runtime.NODEJS_20_X,
      timeout: Duration.seconds(30),
      environment: {
        S3_BUCKET: bucketName,
        FLASK_API_URL: flaskApiUrl,
      },
    });

    // Permissions
    retrainLambda.addToRolePolicy(
      new iam.PolicyStatement({
        actions: ["s3:ListBucket"],
        resources: [`arn:aws:s3:::${bucketName}`],
      })
    );

    // Trigger Lambda on new image upload
    const bucket = s3.Bucket.fromBucketName(this, "SourceBucket", bucketName);

    bucket.addEventNotification(
      s3.EventType.OBJECT_CREATED,
      new s3n.LambdaDestination(retrainLambda)
    );
  }
}
