export interface NotificationPayload {
  to: string;
  title: string;
  body: string;
  data?: Record<string, any>;
}
