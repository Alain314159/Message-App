import { useState, useCallback, useEffect } from 'react';
import { webrtcService } from '@/services/calls/webrtc.service';
import type { Call, CallType, WebRTCSignal } from '@/types/call.types';

export function useCall() {
  const [currentCall, setCurrentCall] = useState<Call | null>(null);
  const [incomingCall, setIncomingCall] = useState<Call | null>(null);
  const [remoteStream, setRemoteStream] = useState<MediaStream | null>(null);
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoOff, setIsVideoOff] = useState(false);
  const [duration, setDuration] = useState(0);

  useEffect(() => {
    webrtcService.onRemoteStream(setRemoteStream);
    webrtcService.onCallEnd(() => {
      setCurrentCall(null);
      setRemoteStream(null);
      setDuration(0);
    });
    if (currentCall?.status === 'connected') {
      const interval = setInterval(() => setDuration((p) => p + 1), 1000);
      return () => clearInterval(interval);
    }
  }, [currentCall?.status]);

  const startCall = useCallback(async (chatId: string, callerId: string, receiverId: string, type: CallType) => {
    const call: Call = {
      id: crypto.randomUUID(),
      chatId,
      callerId,
      receiverId,
      type,
      status: 'ringing',
      startedAt: null,
      endedAt: null,
      duration: 0,
    };
    setCurrentCall(call);
    await webrtcService.initiateCall(call.id, callerId, receiverId, type);
    setCurrentCall((prev) => prev ? { ...prev, status: 'connected', startedAt: new Date() } : null);
  }, []);

  const answerCall = useCallback(async (call: Call, userId: string, signal: WebRTCSignal) => {
    setIncomingCall(null);
    setCurrentCall(call);
    await webrtcService.answerCall(call.id, userId, signal, call.type);
    setCurrentCall((prev) => prev ? { ...prev, status: 'connected', startedAt: new Date() } : null);
  }, []);

  const endCall = useCallback(async () => {
    await webrtcService.endCall();
    setCurrentCall(null);
    setDuration(0);
  }, []);

  const toggleMute = useCallback(() => {
    setIsMuted((p) => {
      webrtcService.muteAudio(!p);
      return !p;
    });
  }, []);

  const toggleVideo = useCallback(() => {
    setIsVideoOff((p) => {
      webrtcService.muteVideo(!p);
      return !p;
    });
  }, []);

  const declineIncoming = useCallback(() => setIncomingCall(null), []);

  return {
    currentCall,
    incomingCall,
    setIncomingCall,
    remoteStream,
    isMuted,
    isVideoOff,
    duration,
    startCall,
    answerCall,
    endCall,
    toggleMute,
    toggleVideo,
    declineIncoming,
  };
}
