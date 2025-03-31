import {Lesson} from "./course";

export interface AIChat{
  id?: number,
  userMessage?: string,
  geminiResponse?: string,
  messageOrder?: number,
  createdTime?: any,
  sessionId?: number
  session?: AIChatSession
}

export interface AIChatSession{
  id?: number,
  userId?: string,
  messages?: AIChat[],
  contextUsed?: string,
  sessionName?: string,
  createdTime?: any,
  updatedTime?: any,
  lesson?: Lesson,
  lessonId?: number
}
