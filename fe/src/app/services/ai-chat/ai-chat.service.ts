import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Result} from "../../../model/result";
import {AIChat, AIChatSession} from "../../../model/aichat";

@Injectable({
  providedIn: 'root'
})
export class AiChatService {
  baseUrl = "http://localhost:8090/course/api"

  constructor(private http: HttpClient) { }

  getAiChatSessionByLessonId(lessonId: number): Observable<Result> {
    return this.http.get(`${this.baseUrl}/getAiChatSessionByLessonId?lessonId=${lessonId}`);
  }

  getAllAiChatBySessionId(sessionId: number): Observable<Result>{
    return this.http.get(`${this.baseUrl}/getAllAiChatBySessionId?sessionId=${sessionId}`)
  }

  startAiSession(aiChatSession: AIChatSession): Observable<Result> {
    return this.http.post(`${this.baseUrl}/startAiSession`, aiChatSession);
  }

  sendMessageInSession(aiChat: AIChat, lessonId: number): Observable<Result> {
    return this.http.post(`${this.baseUrl}/sendMessageInSession?lessonId=${lessonId}`, aiChat);
  }

  deleteSession(sessionId: number): Observable<Result>{
    return this.http.delete(`${this.baseUrl}/deleteSession?sessionId=${sessionId}`)
  }
}
