import { Injectable } from '@angular/core';
import {Observable, Subject} from "rxjs";
import {CompatClient, IMessage, Stomp, StompHeaders} from "@stomp/stompjs";
import {HttpClient} from "@angular/common/http";
import SockJS from "sockjs-client/dist/sockjs";
import {Result} from "../../../model/result";
import {ChatGroup, Message} from "../../../model/chat";
import {AuthService} from "../auth/auth.service";
@Injectable({
  providedIn: 'root'
})
export class ChatService {
  baseUrl = "http://localhost:8090/chat/api"
  baseSocketEndpoint = "http://localhost:8090/chat-websocket/chat-websocket";
  isChatOpen: boolean = false;
  private chatVisibilitySubject = new Subject<boolean>();
  private stompClient!: CompatClient;
  private messageSubject = new Subject<any>();
  private pendingSubscriptions: string[] = []; // Lưu trữ các chatId cần subscribe
  private isConnected: boolean = false;


  constructor(private http: HttpClient, private authService: AuthService) {
    this.initializeWebSocketConnection();
  }

  toggleChatVisibility(): void {
    this.isChatOpen = !this.isChatOpen;
    this.chatVisibilitySubject.next(this.isChatOpen);
  }

  getChatVisibilityChanges(): Observable<boolean> {
    return this.chatVisibilitySubject.asObservable();
  }

  private async initializeWebSocketConnection() {
    const token = await this.authService.getToken();
    const socket = new SockJS(`${this.baseSocketEndpoint}?access_token=${token}`);
    this.stompClient = Stomp.over(socket);
    const headers = new StompHeaders();

    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    this.stompClient.configure({
      reconnectDelay: 5000,
      connectHeaders: headers,
      debug: msg => {
        console.log(msg)
      }
    })

    this.stompClient.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      this.isConnected = true;
      this.pendingSubscriptions.forEach(chatId => this.subscribeToChat(chatId));
      this.pendingSubscriptions = [];
    };

    this.stompClient.onStompError = (error) => {
      console.error('WebSocket error:', error);
    };
    this.stompClient.activate();
  }
  public subscribeToChat(chatId: string) {
    if (!chatId) {
      console.error('Attempted to subscribe with invalid chatId:', chatId);
      return;
    }

    console.log(`Attempting to subscribe to chat: ${chatId}, Connection status: ${this.isConnected}`);

    if (this.stompClient && this.isConnected) {
      console.log(`Subscribing to /topic/chat/${chatId}`);
      this.stompClient.subscribe(`/topic/chat/${chatId}`, (message: IMessage) => {
        console.log('Received message:', message.body);
        try {
          const parsedMessage = JSON.parse(message.body);
          console.log('Parsed message:', parsedMessage);
          this.messageSubject.next(parsedMessage);
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      });
    } else {
      console.log('WebSocket not connected, adding to pending subscriptions');
      if (!this.pendingSubscriptions.includes(chatId)) {
        this.pendingSubscriptions.push(chatId);
      }
    }
  }

  // Get messages as they arrive
  getMessages(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  // Send a message
  sendMessage(destination: string, message: Message): void {
    console.log(`Sending message to ${destination}:`, message);
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/send/' + destination,
        body: JSON.stringify(message)
      });
      console.log('Message sent');
    } else {
      console.error('WebSocket is not connected, message not sent');
    }
  }

  public disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  // Get chat list for a user
  getUserChats(): Observable<Result> {
    return this.http.get(`${this.baseUrl}/chat/userChatList`);
  }

  // Get messages for a chat with pagination
  getChatMessages(chatId: string, userId: string, lastMessageId: string | null): Observable<Result> {
    let url = `${this.baseUrl}/chat/recent-messages/${chatId}?userId=${userId}&limit=20`;
    if (lastMessageId) {
      url += `&lastMessageId=${lastMessageId}`;
    }
    return this.http.get(url);
  }

  unsubscribeFromChat(chatId: any){
    this.stompClient.unsubscribe(`/topic/chat/${chatId}`);
  }

  // Create new private chat
  addPrivateChat(userId: string, recipientId: string): Observable<Result> {
    return this.http.post(`${this.baseUrl}/chat/addPrivateChat`, {
      userId1: userId,
      userId2: recipientId
    });
  }

  // Create new group chat
  createGroupChat(chatGroup: ChatGroup): Observable<any> {
    return this.http.post(`${this.baseUrl}/chat/addGroupChat`, chatGroup);
  }

  getGroupChatDetails(chatId: string): Observable<Result> {
    return this.http.get(`${this.baseUrl}/chat/group/${chatId}`);
  }
}
