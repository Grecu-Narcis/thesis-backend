import { SQSEvent } from "aws-lambda";

exports.handler = async (event: SQSEvent) => {
  const notificationsToSend = [];
  for (const record of event.Records) {
    const body = JSON.parse(record.body);

    notificationsToSend.push(convertToExpoNotification(body));
  }

  console.log(notificationsToSend);
  await sendNotifications(notificationsToSend);

  return {};
};

function convertToExpoNotification(record: any) {
  const token = record.destinationToken;
  const postId = record.postId;
  const createdBy = record.createdBy;

  return {
    to: token,
    title: "New spot around!",
    body: `${createdBy} spotted a new car around you!`,
    data: {
      postId: postId,
    },
  };
}

async function sendNotifications(notificationToSend: any[]) {
  try {
    await fetch("https://exp.host/--/api/v2/push/send", {
      method: "POST",
      body: JSON.stringify(notificationToSend),
    });
  } catch (error) {
    console.error(error);
  }
}
