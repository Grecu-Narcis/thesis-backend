import { S3 } from "aws-sdk";
import axios from "axios";

const s3 = new S3();

const BUCKET = process.env.S3_BUCKET!;
const FLASK_API_URL = process.env.FLASK_API_URL!;

export const handler = async () => {
  let count = 0;
  let continuationToken: string | undefined;

  do {
    const result = await s3
      .listObjectsV2({
        Bucket: BUCKET,
        ContinuationToken: continuationToken,
      })
      .promise();

    count += result.Contents?.length || 0;
    continuationToken = result.IsTruncated
      ? result.NextContinuationToken
      : undefined;
  } while (continuationToken);

  console.log(`Found ${count} images`);

  if (count % 10000) {
    return {
      statusCode: 200,
      body: "Not enough images. Waiting...",
    };
  }

  try {
    const res = await axios.post(FLASK_API_URL);

    return {
      statusCode: 200,
      body: `Triggered retrain: ${res.statusText}`,
    };
  } catch (err) {
    console.error("Failed to POST to Flask API", err);
    return {
      statusCode: 500,
      body: "Failed to trigger retrain",
    };
  }
};
