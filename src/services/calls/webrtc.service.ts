import { supabase } from '@/services/supabase/config';
import type { RealtimeChannel } from '@supabase/supabase-js';
import type { WebRTCSignal } from '@/types/call.types';

const RTC_CONFIG: RTCConfiguration = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
  ],
};

export class WebRTCService {
  private peerConnection: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private channel: RealtimeChannel | null = null;
  private onSignalCallback?: (signal: WebRTCSignal) => void;
  private onStreamCallback?: (stream: MediaStream) => void;
  private onCallEndedCallback?: () => void;

  async initiateCall(
    callId: string,
    callerId: string,
    receiverId: string,
    type: 'voice' | 'video'
  ) {
    this.peerConnection = new RTCPeerConnection(RTC_CONFIG);
    await this.getUserMedia(type);
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) =>
        this.peerConnection!.addTrack(track, this.localStream!)
      );
    }
    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate)
        this.sendSignal({ type: 'candidate', data: event.candidate, from: callerId, to: receiverId, callId });
    };
    this.peerConnection.ontrack = (event) => this.onStreamCallback?.(event.streams[0]);
    const offer = await this.peerConnection.createOffer();
    await this.peerConnection.setLocalDescription(offer);
    await this.sendSignal({ type: 'offer', data: offer, from: callerId, to: receiverId, callId });
    this.listenForSignals(callId);
  }

  async answerCall(
    callId: string,
    userId: string,
    signal: WebRTCSignal,
    type: 'voice' | 'video'
  ) {
    this.peerConnection = new RTCPeerConnection(RTC_CONFIG);
    await this.getUserMedia(type);
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) =>
        this.peerConnection!.addTrack(track, this.localStream!)
      );
    }
    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate)
        this.sendSignal({ type: 'candidate', data: event.candidate, from: userId, to: signal.from, callId });
    };
    this.peerConnection.ontrack = (event) => this.onStreamCallback?.(event.streams[0]);
    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(signal.data));
    const answer = await this.peerConnection.createAnswer();
    await this.peerConnection.setLocalDescription(answer);
    await this.sendSignal({ type: 'answer', data: answer, from: userId, to: signal.from, callId });
    this.listenForSignals(callId);
  }

  async handleSignal(signal: WebRTCSignal) {
    if (!this.peerConnection) return;
    if (signal.type === 'answer')
      await this.peerConnection.setRemoteDescription(new RTCSessionDescription(signal.data));
    else if (signal.type === 'candidate')
      await this.peerConnection.addIceCandidate(new RTCIceCandidate(signal.data));
  }

  async endCall() {
    if (this.localStream) {
      this.localStream.getTracks().forEach((t) => t.stop());
      this.localStream = null;
    }
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }
    if (this.channel) {
      this.channel.unsubscribe();
      this.channel = null;
    }
    this.onCallEndedCallback?.();
  }

  muteAudio(muted: boolean) {
    if (this.localStream) {
      const t = this.localStream.getAudioTracks()[0];
      if (t) t.enabled = !muted;
    }
  }

  muteVideo(muted: boolean) {
    if (this.localStream) {
      const t = this.localStream.getVideoTracks()[0];
      if (t) t.enabled = !muted;
    }
  }

  onSignal(cb: (signal: WebRTCSignal) => void) {
    this.onSignalCallback = cb;
  }

  onRemoteStream(cb: (stream: MediaStream) => void) {
    this.onStreamCallback = cb;
  }

  onCallEnd(cb: () => void) {
    this.onCallEndedCallback = cb;
  }

  private async getUserMedia(type: 'voice' | 'video') {
    this.localStream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: type === 'video',
    });
  }

  private async sendSignal(signal: WebRTCSignal) {
    const { error } = await (supabase.from('call_signals') as any).insert({
      call_id: signal.callId,
      from_user_id: signal.from,
      to_user_id: signal.to,
      signal_type: signal.type,
      data: signal.data,
    });
    if (error) console.error('Failed to send signal:', error);
  }

  private listenForSignals(callId: string) {
    this.channel = supabase.channel(`call:${callId}`);
    this.channel.on(
      'postgres_changes',
      {
        event: 'INSERT',
        schema: 'public',
        table: 'call_signals',
        filter: `call_id=eq.${callId}`,
      },
      (payload) => {
        const s = payload.new as any;
        this.handleSignal({
          type: s.signal_type,
          data: s.data,
          from: s.from_user_id,
          to: s.to_user_id,
          callId: s.call_id,
        });
      }
    ).subscribe();
  }
}

export const webrtcService = new WebRTCService();
