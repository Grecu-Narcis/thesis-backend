import { CommentNotification } from "../types/CommentNotification";
import { FollowNotification } from "../types/FollowNotification";
import { LikeNotification } from "../types/LikeNotification";
import { MessageNotification } from "../types/MessageNotification";
import { NewSpotNotification } from "../types/NewPostNotification";
import { Notification } from "../types/Notification";

export class NotificationFactory {
  static create(record: any): Notification {
    switch (record.notificationType) {
      case "POST_CREATED":
        return new NewSpotNotification(record);
      case "COMMENT":
        return new CommentNotification(record);
      case "LIKE":
        return new LikeNotification(record);
      case "NEW_FOLLOW":
        return new FollowNotification(record);
      case "NEW_MESSAGE":
        return new MessageNotification(record);
      default:
        throw new Error(
          `Unsupported notification type: ${record.notificationType}`
        );
    }
  }
}
