declare module "sockjs-client" {
  interface SockJS extends WebSocket {}
  const SockJS: {
    new (url: string): SockJS;
  };
  export = SockJS;
}
