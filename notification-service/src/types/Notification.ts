import { NotificationPayload } from "./NotificationPayload";

export abstract class Notification {
  constructor(protected record: any) {}

  abstract build(): NotificationPayload;
}
