import { SQSEvent } from "aws-lambda";
import { NotificationFactory } from "../factory/NotificationFactory";

exports.handler = async (event: SQSEvent) => {
  const notificationsToSend = [];
  for (const record of event.Records) {
    const body = JSON.parse(record.body);
    console.log("------------------------");
    console.log("parsing: " + JSON.stringify(body));

    const notification = NotificationFactory.create(body).build();

    notificationsToSend.push(notification);
  }

  console.log(notificationsToSend);
  await sendNotifications(notificationsToSend);

  return {};
};

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
