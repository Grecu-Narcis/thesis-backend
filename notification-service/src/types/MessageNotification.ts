import { NotificationPayload } from "./NotificationPayload";
import { Notification } from "./Notification";

export class MessageNotification extends Notification {
  build(): NotificationPayload {
    return {
      to: this.record.destinationToken,
      title: this.record.username,
      body: this.record.messageBody,
      data: { otherUser: this.record.username, chatId: this.record.chatId },
    };
  }
}
