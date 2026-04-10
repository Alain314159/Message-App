import React from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity } from 'react-native';
import { IconButton } from 'react-native-paper';
import { theme } from '@/config/theme';

interface VoiceRecorderProps {
  visible: boolean;
  isRecording: boolean;
  duration: number;
  onStart: () => void;
  onStop: () => void;
  onCancel: () => void;
}

function formatDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

export const VoiceRecorder: React.FC<VoiceRecorderProps> = ({
  visible,
  isRecording,
  duration,
  onStart,
  onStop,
  onCancel,
}) => (
  <Modal visible={visible} transparent animationType="fade">
    <View style={styles.overlay}>
      <View style={styles.container}>
        <Text style={styles.title}>Nota de voz</Text>
        <Text style={styles.duration}>{formatDuration(duration)}</Text>
        {isRecording && (
          <View style={styles.recordingIndicator}>
            <View style={styles.pulse} />
            <Text style={styles.recordingText}>Grabando...</Text>
          </View>
        )}
        <View style={styles.buttons}>
          <TouchableOpacity style={styles.cancelButton} onPress={onCancel}>
            <IconButton icon="delete" size={28} iconColor={theme.colors.error} />
            <Text style={styles.cancelText}>Cancelar</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.recordButton, isRecording && styles.stopButton]}
            onPress={isRecording ? onStop : onStart}
          >
            <IconButton icon={isRecording ? 'stop' : 'microphone'} size={32} iconColor="#fff" />
          </TouchableOpacity>
          <View style={{ width: 80 }} />
        </View>
      </View>
    </View>
  </Modal>
);

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.8)', justifyContent: 'center', alignItems: 'center' },
  container: { width: '80%', backgroundColor: theme.colors.surface, borderRadius: 24, padding: 24, alignItems: 'center' },
  title: { fontSize: 20, fontWeight: '700', color: theme.colors.textPrimary, marginBottom: 8 },
  duration: { fontSize: 36, fontWeight: '300', color: theme.colors.textPrimary, marginBottom: 16 },
  recordingIndicator: { flexDirection: 'row', alignItems: 'center', marginBottom: 24 },
  pulse: { width: 12, height: 12, borderRadius: 6, backgroundColor: theme.colors.error, marginRight: 8 },
  recordingText: { fontSize: 14, color: theme.colors.error, fontWeight: '600' },
  buttons: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-around', width: '100%' },
  cancelButton: { alignItems: 'center' },
  cancelText: { fontSize: 12, color: theme.colors.error },
  recordButton: { backgroundColor: theme.colors.primary, borderRadius: 32, width: 64, height: 64, justifyContent: 'center', alignItems: 'center' },
  stopButton: { backgroundColor: theme.colors.error },
});
