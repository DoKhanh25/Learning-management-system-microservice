type SetIterator<T> = Iterator<T>;
declare module 'sockjs-client/dist/sockjs' {
  export default class SockJS {
    constructor(url: string, _reserved?: any, options?: any);
    onopen: () => void;
    onmessage: (e: MessageEvent) => void;
    onclose: () => void;
    close: () => void;
    send: (data: string) => void;
  }
}
