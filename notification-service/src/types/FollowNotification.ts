import { Notification } from "./Notification";
import { NotificationPayload } from "./NotificationPayload";

export class FollowNotification extends Notification {
  build(): NotificationPayload {
    return {
      to: this.record.destinationToken,
      title: "New follower!",
      body: `${this.record.followingUser} started to follow you.`,
      data: { username: this.record.followingUser, type: "NEW_FOLLOW" },
    };
  }
}
