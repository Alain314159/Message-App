import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useCall } from '@/hooks/useCall';
import { theme } from '@/config/theme';
import { IconButton } from 'react-native-paper';

function formatCallDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

export const CallScreen: React.FC = () => {
  const { currentCall, isMuted, isVideoOff, duration, endCall, toggleMute, toggleVideo } = useCall();
  if (!currentCall) return null;

  return (
    <View style={styles.container}>
      <View style={styles.info}>
        <Text style={styles.name}>
          {currentCall.callerId === 'me' ? 'Tu' : 'Contacto'}
        </Text>
        <Text style={styles.status}>
          {currentCall.status === 'connected' ? formatCallDuration(duration) : currentCall.status}
        </Text>
      </View>
      <View style={styles.controls}>
        <TouchableOpacity style={styles.controlButton} onPress={toggleMute}>
          <IconButton icon={isMuted ? 'microphone-off' : 'microphone'} size={28} iconColor="#fff" />
          <Text style={styles.controlLabel}>{isMuted ? 'Activar' : 'Silenciar'}</Text>
        </TouchableOpacity>
        {currentCall.type === 'video' && (
          <TouchableOpacity style={styles.controlButton} onPress={toggleVideo}>
            <IconButton icon={isVideoOff ? 'video-off' : 'video'} size={28} iconColor="#fff" />
            <Text style={styles.controlLabel}>{isVideoOff ? 'Activar' : 'Camara'}</Text>
          </TouchableOpacity>
        )}
        <TouchableOpacity style={[styles.controlButton, styles.endCallButton]} onPress={endCall}>
          <IconButton icon="phone-hangup" size={28} iconColor="#fff" />
          <Text style={styles.controlLabel}>Colgar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: theme.colors.background, justifyContent: 'space-between', paddingVertical: 80 },
  info: { alignItems: 'center' },
  name: { fontSize: 24, fontWeight: '700', color: theme.colors.textPrimary, marginBottom: 8 },
  status: { fontSize: 16, color: theme.colors.textSecondary },
  controls: { flexDirection: 'row', justifyContent: 'center', gap: 32, paddingBottom: 40 },
  controlButton: { alignItems: 'center' },
  controlLabel: { fontSize: 12, color: theme.colors.textSecondary, marginTop: 4 },
  endCallButton: { backgroundColor: theme.colors.error, borderRadius: 28 },
});
