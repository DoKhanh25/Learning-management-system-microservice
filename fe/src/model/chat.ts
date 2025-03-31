export interface ChatGroup{
  chatId?: string,
  groupName?: string,
  creatorId?: string,
  memberIds?: string[],
  createdAt?: any
}

export interface Message{
  senderId?: string,
  content?: string
}
