import {Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import { MessageService } from 'primeng/api';
import {LessonPages} from "../../../model/course";
import {AiChatService} from "../../services/ai-chat/ai-chat.service";
import {AIChat, AIChatSession} from "../../../model/aichat";
import {BoldMarkdownPipe} from "../../pipe/boldMarkdown/bold-markdown.pipe";

interface ChatMessage {
  content: string;
  sender: 'user' | 'ai';
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat-assistant',
  templateUrl: './ai-chat-assistant.component.html',
  styleUrls: ['./ai-chat-assistant.component.css']
})
export class AiChatAssistantComponent implements OnInit, OnChanges {
  @Input() lessonData: any;
  @Input() currentPage!: LessonPages;
  @Input() lessonId!: number;
  @ViewChild('chatContainer') chatContainer!: ElementRef;


  messages: ChatMessage[] = [];
  newMessage: string = '';
  isTyping: boolean = false;
  hasLoadedPreviousSession: boolean = false;

  sessionId: number | null = null;

  constructor(
    private messageService: MessageService,
    private aiChatService: AiChatService
  ) { }


  ngOnInit(): void {
    // Add welcome message
    this.messages = [{
      content: 'Xin chào! Tôi là trợ lý AI. Hãy hỏi tôi bất kì thông tin gì về bài học!',
      sender: 'ai',
      timestamp: new Date()
    }];

    if (this.lessonId) {
      this.loadExistingSessionOrCreateNew();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lessonId'] && !changes['lessonId'].firstChange && this.lessonId) {
      this.loadExistingSessionOrCreateNew();
    }
  }

  loadExistingSessionOrCreateNew(): void {
    this.aiChatService.getAiChatSessionByLessonId(this.lessonId).subscribe({
      next: (response) => {
        if (response.status === 1 && response.data) {
          // Use the most recent session
          let aiChatSession: AIChatSession = response.data;
          console.log(aiChatSession)

          this.sessionId = aiChatSession.id!;
          this.hasLoadedPreviousSession = true;

          // Load previous messages if available
          if (aiChatSession.messages && aiChatSession.messages.length > 0) {
            // Clear welcome message
            this.messages = [];

            // Add previous messages to chat
            aiChatSession.messages.forEach((msg: any) => {
              if (msg.userMessage) {
                this.messages.push({
                  content: msg.userMessage,
                  sender: 'user',
                  timestamp: new Date(msg.createdTime || Date.now())
                });
              }
              if (msg.geminiResponse) {
                this.messages.push({
                  content: msg.geminiResponse,
                  sender: 'ai',
                  timestamp: new Date(msg.createdTime || Date.now())
                });
              }
            });
          }
        } else {
          // No existing sessions, create new one
          this.startNewSession();
        }
      },
      error: (err) => {
        console.error('Failed to fetch chat sessions', err);
        this.startNewSession();
      }
    });
  }

  startNewSession(): void {
    const sessionName = `Lesson ${this.lessonId} - ${this.lessonData?.name || 'Chat'}`;
    let aiChatSession: AIChatSession = {
      sessionName: sessionName,
      lessonId: this.lessonId
    }
    this.aiChatService.startAiSession(aiChatSession).subscribe({
      next: (response) => {
        if (response.status === 1) {
          this.sessionId = response.data.id;
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to start AI chat session'
          });
        }
      },
      error: (err: any) => {
        console.error('Failed to start AI chat session', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to connect to AI service'
        });
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim()) return;

    // Add user message
    this.messages.push({
      content: this.newMessage,
      sender: 'user',
      timestamp: new Date()
    });

    // Store message and clear input
    const userQuestion = this.newMessage;
    this.newMessage = '';

    // Show typing indicator
    this.isTyping = true;

    // Check if we have a session
    if (!this.sessionId) {
      this.startNewSession();
      setTimeout(() => this.processUserMessage(userQuestion), 1000);
      return;
    }

    this.processUserMessage(userQuestion);
    setTimeout(() => this.scrollToBottom(), 100);

  }

  private processUserMessage(userQuestion: string): void {
    // Call the AI service with the actual question
    let aiChat: AIChat = {
      userMessage: userQuestion,
      sessionId: this.sessionId!,
    }
    this.aiChatService.sendMessageInSession(aiChat, this.lessonId).subscribe({
      next: (response) => {
        this.isTyping = false;
        if (response.status === 1) {
          // Add AI response to chat
          this.messages.push({
            content: response.data,
            sender: 'ai',
            timestamp: new Date()
          });
          setTimeout(() => this.scrollToBottom(), 100);
        } else {
          this.handleAiError('Failed to get AI response');
        }
      },
      error: (err) => {
        console.error('AI response error', err);
        this.isTyping = false;
        this.handleAiError('AI service unavailable');
      }
    });
  }

  private handleAiError(message: string): void {
    this.messages.push({
      content: `Sorry, ${message}. Please try again later.`,
      sender: 'ai',
      timestamp: new Date()
    });
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: message
    });
  }
  scrollToBottom(): void {
    try {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    } catch(err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  clearChat(): void {
    this.messages = [{
      content: 'Xin chào! Tôi là trợ lý AI. Hãy hỏi tôi bất kì thông tin gì về bài học!',
      sender: 'ai',
      timestamp: new Date()
    }];
    if(this.messages.length == 1){
      return
    };
    console.log(this.messages.length)
    this.aiChatService.deleteSession(this.sessionId!).subscribe((response) => {
      if(response.status === 1){
        this.sessionId = null;
        this.loadExistingSessionOrCreateNew();
      }
    }, (error) => {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: error
      });
    })

  }
}
