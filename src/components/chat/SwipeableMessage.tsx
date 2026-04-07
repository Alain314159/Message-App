import React, { useRef } from 'react';
import { View, StyleSheet } from 'react-native';
import { RectButton, Swipeable } from 'react-native-gesture-handler';
import { IconButton, Text } from 'react-native-paper';
import { theme } from '@/config/theme';

interface SwipeableMessageProps {
  isMyMessage: boolean;
  onReply?: () => void;
  onForward?: () => void;
  children: React.ReactNode;
}

export const SwipeableMessage: React.FC<SwipeableMessageProps> = ({
  isMyMessage,
  onReply,
  onForward,
  children,
}) => {
  const swipeableRef = useRef<Swipeable>(null);

  const renderRightActions = () => (
    <View style={styles.rightActions}>
      <RectButton
        style={[styles.actionButton, styles.replyButton]}
        onPress={() => {
          onReply?.();
          swipeableRef.current?.close();
        }}
      >
        <IconButton icon="reply" size={24} iconColor="#fff" />
        <Text style={styles.actionText}>Responder</Text>
      </RectButton>
      {!isMyMessage && (
        <RectButton
          style={[styles.actionButton, styles.forwardButton]}
          onPress={() => {
            onForward?.();
            swipeableRef.current?.close();
          }}
        >
          <IconButton icon="share" size={24} iconColor="#fff" />
          <Text style={styles.actionText}>Reenviar</Text>
        </RectButton>
      )}
    </View>
  );

  return (
    <Swipeable
      ref={swipeableRef}
      renderRightActions={renderRightActions}
      friction={2}
      overshootRight={false}
    >
      {children}
    </Swipeable>
  );
};

const styles = StyleSheet.create({
  rightActions: { flexDirection: 'row', height: '100%' },
  actionButton: { justifyContent: 'center', alignItems: 'center', width: 80 },
  replyButton: { backgroundColor: theme.colors.primary },
  forwardButton: { backgroundColor: '#2196F3' },
  actionText: { color: '#fff', fontSize: 12, fontWeight: '600' },
});
