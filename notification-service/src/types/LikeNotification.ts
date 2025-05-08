import { NotificationPayload } from "./NotificationPayload";
import { Notification } from "./Notification";

export class LikeNotification extends Notification {
  build(): NotificationPayload {
    return {
      to: this.record.destinationToken,
      title: "Your post got a like!",
      body: `${this.record.likedBy} liked your spot.`,
      data: { postId: this.record.postId, type: "LIKE" },
    };
  }
}
