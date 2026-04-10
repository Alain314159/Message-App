export type CallType = 'voice' | 'video';
export type CallStatus = 'idle' | 'ringing' | 'connected' | 'ended' | 'failed';

export interface Call {
  id: string;
  chatId: string;
  callerId: string;
  receiverId: string;
  type: CallType;
  status: CallStatus;
  startedAt: Date | null;
  endedAt: Date | null;
  duration: number;
}

export interface WebRTCSignal {
  type: 'offer' | 'answer' | 'candidate';
  data: any;
  from: string;
  to: string;
  callId: string;
}
