import { NotificationPayload } from "./NotificationPayload";
import { Notification } from "./Notification";

export class NewSpotNotification extends Notification {
  build(): NotificationPayload {
    return {
      to: this.record.destinationToken,
      title: "New spot around!",
      body: `${this.record.createdBy} spotted a new car around you!`,
      data: { postId: this.record.postId, type: "NEW_POST" },
    };
  }
}
