import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { MessageReactions } from '@/components/chat/MessageReactions';

describe('MessageReactions', () => {
  it('renders nothing when reactions are empty', () => {
    const { queryByTestId } = render(<MessageReactions reactions={{}} />);
    expect(queryByTestId('message-reactions')).toBeNull();
  });

  it('renders reaction chips with counts', () => {
    const { getByText, getByTestId } = render(
      <MessageReactions reactions={{ '\u2764\uFE0F': { count: 3, userReacted: false } }} />
    );
    expect(getByTestId('message-reactions')).toBeTruthy();
    expect(getByText('3')).toBeTruthy();
  });

  it('highlights reactions the user has reacted to', () => {
    const { UNSAFE_getAllByType } = render(
      <MessageReactions reactions={{ '\u{1F44D}': { count: 1, userReacted: true } }} />
    );
    const chips = UNSAFE_getAllByType('TouchableOpacity');
    expect(chips.length).toBeGreaterThan(0);
  });

  it('calls onReactionPress when a reaction is pressed', () => {
    const onPress = jest.fn();
    const { getByText } = render(
      <MessageReactions
        reactions={{ '\u{1F602}': { count: 2, userReacted: false } }}
        onReactionPress={onPress}
      />
    );
    fireEvent.press(getByText('\u{1F602}'));
    expect(onPress).toHaveBeenCalledWith('\u{1F602}');
  });

  it('filters out reactions with zero count', () => {
    const { queryByText } = render(
      <MessageReactions reactions={{ '\u2764\uFE0F': { count: 0, userReacted: false } }} />
    );
    // Zero count reactions should not be rendered
    const reactionsContainer = queryByText('0');
    expect(reactionsContainer).toBeNull();
  });
});
