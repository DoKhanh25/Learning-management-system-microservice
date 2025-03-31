import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {forkJoin, Subscription} from "rxjs";
import {ChatService} from "../../services/chat/chat.service";
import {AuthService} from "../../services/auth/auth.service";
import {UserService} from "../../services/user-service/user.service";
import {User} from "../../../model/user";
import {ChatGroup} from "../../../model/chat";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy{
  @ViewChild('messageContainer') messageContainer!: ElementRef;

  currentUser: any = {};
  messages: any[] = [];
  newMessage: string = '';
  chats: any[] = [];
  selectedChat: any = null;
  loading: boolean = false;
  lastMessageId: string | null = null;
  privateChats: any[] = [];
  groupChats: any[] = [];
  isChatOpen: boolean = false;
  userChats: any[] = [];
  searchTerm: string = '';
  searchResults: any[] = [];
  showSearchResults: boolean = false;

  activeChatBubbles: any[] = [];
  maximizedChatIds: {[key: string]: boolean} = {};
  private subscribedChats: Set<string> = new Set();
  private subscriptions: Subscription[] = [];

  showCreateGroupDialog: boolean = false;
  newGroup: any = {
    name: '',
    members: []
  };
  availableUsers: any[] = [];


  constructor(private chatService: ChatService,
              private authService: AuthService,
              private userService: UserService) {}

  ngOnInit(): void {
    // Initialize user from auth service
    this.currentUser = {
      id: this.authService.getUserFromCookie()?.id,
      name: this.authService.getUserFromCookie()?.username }; // Replace with actual auth service

    this.loadChats();

    this.subscriptions.push(
      this.chatService.getChatVisibilityChanges().subscribe((isOpen) => {
        this.isChatOpen = isOpen;
      })
    );
    this.isChatOpen = this.chatService.isChatOpen;

    this.subscriptions.push(
      this.chatService.getMessages().subscribe((result) => {
        console.log("Received message in component:", result);
        const chatBubble = this.activeChatBubbles.find(bubble => bubble.chatId === result.chatId);
        if (chatBubble) {
          if (!chatBubble.messages) {
            chatBubble.messages = [];
          }
          if (!chatBubble.messages.some((msg: any) => msg.messageId === result.messageId)) {
            chatBubble.messages.push(result);
            this.scrollChatBubbleToBottom(result.chatId);
          }
        }

        if (this.selectedChat && this.selectedChat.chatId === result.chatId) {
          if (!this.messages.some(msg => msg.messageId === result.messageId)) {
            this.messages.push(result);
            this.scrollToBottom();
          }
        }
      })
    );
  }

  loadChats(): void {
    this.loading = true;
    this.chatService.getUserChats().subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.chats = result.data;
          let privateUserChats: any[] = []
          // Separate private and group chats
          this.privateChats = this.chats.filter(chat => chat.startsWith('one_'));
          this.groupChats = this.chats.filter(chat => chat.startsWith('group_'));

          // create userChats
          privateUserChats = this.privateChats.map((chat: string) => {
            return {
              chatId: chat,
              userId: chat
                .replace("one_", "")
                .replace(this.currentUser.id, "")
                .replace("_", "")
            };
          });

          this.userService.getAllUsers().subscribe((result) => {
            if(result.status === 1){
              let allUsers: User[] = result.data;
              this.userChats = allUsers.filter((user) => {
                return privateUserChats.some(chat => chat.userId === user.userId);
              }).map(user => {
                const chatInfo = privateUserChats.find(chat => chat.userId === user.userId);
                return {
                  ...user,
                  chatId: chatInfo?.chatId
                };
              });
            }
          }, (error) => {
            console.error('Failed to load user', error);
            this.loading = false;
          })

          if (this.groupChats.length > 0) {
            // You need to create a method in ChatService to get group details
            forkJoin(
              this.groupChats.map(chatId =>
                this.chatService.getGroupChatDetails(chatId)
              )
            ).subscribe(groupResults => {
              this.groupChats = groupResults
                .filter(group => group.status === 1)
                .map(group => group.data);
            });
          }


          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Failed to load chats', err);
        this.loading = false;
      }
    });
  }

  onBubbleHeaderClick(chatId: string, event: Event): void {
    // Check if the click was directly on the header (not on buttons)
    if ((event.target as HTMLElement).classList.contains('bubble-header') ||
      (event.target as HTMLElement).classList.contains('bubble-user-info') ||
      (event.target as HTMLElement).classList.contains('bubble-user-name')) {
      this.toggleChatBubble(chatId);
    }
  }

  selectChat(chat: any): void {
    this.selectedChat = chat;
    const chatId = chat.chatId || chat.id;
    console.log(this.activeChatBubbles)
    if (!this.subscribedChats.has(chatId)) {
      this.chatService.subscribeToChat(chatId);
      this.subscribedChats.add(chatId);
    }


    this.messages = [];
    this.lastMessageId = null;

    const existingBubbleIndex = this.activeChatBubbles.findIndex(bubble =>
      (bubble.chatId === chatId) || (bubble.id === chatId));

    if (existingBubbleIndex === -1) {
      // Add a new chat bubble with consistent structure
      const newChatBubble = {
        chatId: chatId,
        username: chat.username || chat.groupName,
        userId: chat.userId || this.getUserIdFromChatId(chatId),
        newMessage: '',
        messages: []
      };

      this.activeChatBubbles.push(newChatBubble);

      this.loadMessagesForChat(chatId);

    } else {

      const existingBubble = this.activeChatBubbles[existingBubbleIndex];
      if (existingBubble.messages && existingBubble.messages.length > 0) {
        this.messages = [...existingBubble.messages];
      } else {
        this.loadMessagesForChat(chatId);
      }
    }

    this.maximizedChatIds[chatId] = true;

  }

  private getUserIdFromChatId(chatId: string): string {
    if (chatId.startsWith('one_')) {
      return chatId
        .replace('one_', '')
        .replace(this.currentUser.id, '')
        .replace('_', '');
    }
    return '';
  }

  loadMessagesForChat(chatId: string): void {
    const chat = this.activeChatBubbles.find(bubble =>
      (bubble.chatId === chatId) || (bubble.id === chatId));

    if (!chat) return;

    this.loading = true;

    this.chatService.getChatMessages(chatId, this.currentUser.id, null).subscribe({
      next: (result) => {
        if (result.status === 1) {
          if (!chat.messages) {
            chat.messages = [];
          }

          chat.messages = result.data;

          if (this.selectedChat &&
            (this.selectedChat.chatId === chatId || this.selectedChat.id === chatId)) {
            this.messages = [...result.data];
          }

          this.loading = false;
          this.scrollChatBubbleToBottom(chatId);
          this.scrollToBottom();
        }
      },
      error: (err) => {
        console.error('Failed to load messages', err);
        this.loading = false;
      }
    });
  }

  getChatMessages(chatId: string): any[] {
    const chat = this.activeChatBubbles.find(bubble =>
      (bubble.chatId === chatId) || (bubble.id === chatId));

    return (chat && chat.messages) ? chat.messages : [];
  }

  loadMessages(): void {
    if (!this.selectedChat) return;

    this.loading = true;
    this.chatService.getChatMessages(this.selectedChat.chatId, this.currentUser.id, this.lastMessageId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          const newMessages = result.data;
          this.messages = [...newMessages, ...this.messages];

          if (newMessages.length > 0) {
            this.lastMessageId = newMessages[0].messageId;
          }
          console.log(this.messages);

          this.loading = false;
          this.scrollToBottom();
        }
      },
      error: (err) => {
        console.error('Failed to load messages', err);
        this.loading = false;
      }
    });
  }

  loadMoreMessages(): void {
    if (this.messages.length > 0) {
      this.lastMessageId = this.messages[0].messageId;
      this.loadMessages();
    }
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedChat) return;

    const messageData = {
      senderId: this.currentUser.id,
      content: this.newMessage.trim(),
    };

    this.chatService.sendMessage(this.selectedChat.chatId, messageData);
    this.newMessage = '';
  }

  sendMessageFromBubble(chat: any): void {
    if (!chat.newMessage || !chat.newMessage.trim()) return;

    const messageData = {
      senderId: this.currentUser.id,
      content: chat.newMessage.trim(),
    };

    this.chatService.sendMessage(chat.chatId, messageData);
    chat.newMessage = '';
  }

  isOwnMessage(message: any): boolean {
    return message.senderId === this.currentUser.id;
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop =
          this.messageContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }

  scrollChatBubbleToBottom(chatId: string): void {
    setTimeout(() => {
      const chatElement = document.querySelector(`.bubble-messages[data-chat-id="${chatId}"]`);
      if (chatElement) {
        chatElement.scrollTop = chatElement.scrollHeight;
      }
    }, 100);
  }

  toggleChatBubble(chatId: string): void {
    this.maximizedChatIds[chatId] = !this.maximizedChatIds[chatId];

    if (this.maximizedChatIds[chatId]) {
      this.scrollChatBubbleToBottom(chatId);
    }

  }

  minimizeChatBubble(chatId: string, event: Event): void {
    event.stopPropagation();
    event.preventDefault();

    this.maximizedChatIds[chatId] = false;
    setTimeout(() => {
    }, 0);

  }

  closeChatBubble(chatId: string, event: Event): void {
    event.stopPropagation();
    this.activeChatBubbles = this.activeChatBubbles.filter(bubble =>
      bubble.chatId !== chatId && bubble.id !== chatId);

    // Remove chat from maximized map
    delete this.maximizedChatIds[chatId];

    this.subscribedChats.delete(chatId);
    this.chatService.unsubscribeFromChat(chatId);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.chatService.disconnect();
  }

  searchUsers(): void {
    if (this.searchTerm.trim().length === 0) {
      this.searchResults = [];
      this.showSearchResults = false;
      return;
    }

    this.userService.searchUsers(this.searchTerm).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.searchResults = result.data;
          this.searchResults = this.searchResults.filter((element) =>
            element.userId != this.currentUser.id
          );
          this.showSearchResults = true;
        }
      },
      error: (err) => {
        console.error('Failed to search users', err);
      }
    });
  }

  startChatWithUser(user: any): void {
    // Check if a chat already exists
    const existingChat = this.privateChats.find(chat =>
      chat.includes(user.userId));

    if (existingChat) {
      this.selectChat(existingChat);
      this.showSearchResults = false;
      this.searchTerm = '';
      return;
    }

    // Create a new chat
    this.chatService.addPrivateChat(this.currentUser.id, user.userId).subscribe({
      next: (result) => {
        if (result.status === 1) {
          const newChat = result.data;
          this.privateChats.unshift(newChat);
          this.showSearchResults = false;
          this.searchTerm = '';
          this.loadChats();
        }
      },
      error: (err) => {
        console.error('Failed to create chat', err);
      }
    });
  }

  openCreateGroupDialog(): void {
    this.newGroup = {
      name: '',
      members: []
    };

    // Load all users to select from
    this.userService.getAllUsers().subscribe({
      next: (result) => {
        if (result.status === 1) {
          // Filter out current user from the list
          this.availableUsers = result.data.filter(
            (user: any) => user.userId !== this.currentUser.id
          );
          this.showCreateGroupDialog = true;
        }
      },
      error: (err) => {
        console.error('Failed to load users', err);
      }
    });
  }

  createGroupChat(): void {
    if (!this.newGroup.name || this.newGroup.members.length === 0) {
      return;
    }

    // Format group data for API
    const chatGroup: ChatGroup = {
      creatorId: this.currentUser.id,
      groupName: this.newGroup.name,
      memberIds: [
        this.currentUser.id,
        ...this.newGroup.members.map((user: any) => user.userId)
      ]
    };

    this.chatService.createGroupChat(chatGroup).subscribe({
      next: (result) => {
        if (result.status === 1) {
          this.showCreateGroupDialog = false;
          this.loadChats();
        }
      },
      error: (err) => {
        console.error('Failed to create group chat', err);
      }
    });
  }


}
