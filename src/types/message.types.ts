import type { Message, MessageType } from './index';

export interface MessageReaction {
  id: string;
  messageId: string;
  userId: string;
  emoji: string;
  createdAt: Date;
}

export interface ReactionCounts {
  [emoji: string]: { count: number; userReacted: boolean };
}

export interface ReplyContext {
  messageId: string;
  senderName: string;
  text: string;
  type: MessageType;
}

export interface MessageEdit {
  id: string;
  messageId: string;
  previousContent: string;
  editedAt: Date;
  editedBy: string;
}

export interface ForwardTarget {
  chatId: string;
  chatName: string;
  lastMessage: string | null;
}

export type MessageAction = 'reply' | 'react' | 'edit' | 'forward' | 'star' | 'delete' | 'copy' | 'select';

export interface MessageWithMeta extends Message {
  reactions?: ReactionCounts;
  replyContext?: ReplyContext | null;
  isStarred?: boolean;
  isSelected?: boolean;
  editHistory?: MessageEdit[];
}
