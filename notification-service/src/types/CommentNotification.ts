import { NotificationPayload } from "./NotificationPayload";
import { Notification } from "./Notification";

export class CommentNotification extends Notification {
  build(): NotificationPayload {
    return {
      to: this.record.destinationToken,
      title: "New comment!",
      body: `${this.record.createdBy} commented on your spot.`,
      data: { postId: this.record.postId, type: "COMMENT" },
    };
  }
}
