-- Message reactions table
CREATE TABLE IF NOT EXISTS message_reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    emoji TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(message_id, user_id, emoji)
);
CREATE INDEX IF NOT EXISTS idx_message_reactions_message_id ON message_reactions(message_id);
ALTER TABLE message_reactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view reactions from their chats" ON message_reactions FOR SELECT
    USING (EXISTS (SELECT 1 FROM messages m JOIN chats c ON c.id = m.chat_id WHERE m.id = message_reactions.message_id AND auth.uid() = ANY(c.participant_ids)));
CREATE POLICY "Users can add reactions" ON message_reactions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can remove own reactions" ON message_reactions FOR DELETE USING (auth.uid() = user_id);

-- Storage buckets
INSERT INTO storage.buckets (id, name, public) VALUES ('messages', 'messages', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO storage.buckets (id, name, public) VALUES ('avatars', 'avatars', true) ON CONFLICT (id) DO NOTHING;

CREATE POLICY "Authenticated users can upload message media" ON storage.objects FOR INSERT TO authenticated WITH CHECK (bucket_id = 'messages');
CREATE POLICY "Anyone can read message media" ON storage.objects FOR SELECT USING (bucket_id = 'messages');
CREATE POLICY "Authenticated users can upload avatars" ON storage.objects FOR INSERT TO authenticated WITH CHECK (bucket_id = 'avatars');
CREATE POLICY "Anyone can read avatars" ON storage.objects FOR SELECT USING (bucket_id = 'avatars');

-- Call signaling and log tables
CREATE TABLE IF NOT EXISTS call_signals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    call_id UUID NOT NULL,
    from_user_id UUID NOT NULL REFERENCES users(id),
    to_user_id UUID NOT NULL REFERENCES users(id),
    signal_type TEXT NOT NULL,
    data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_call_signals_call_id ON call_signals(call_id);

CREATE TABLE IF NOT EXISTS call_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_id UUID NOT NULL REFERENCES chats(id),
    caller_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    call_type TEXT NOT NULL CHECK (call_type IN ('voice', 'video')),
    status TEXT NOT NULL DEFAULT 'ringing' CHECK (status IN ('ringing', 'connected', 'ended', 'failed', 'missed')),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    duration INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_call_log_chat_id ON call_log(chat_id);
ALTER TABLE call_log ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view call log from their chats" ON call_log FOR SELECT
    USING (EXISTS (SELECT 1 FROM chats c WHERE c.id = call_log.chat_id AND auth.uid() = ANY(c.participant_ids)));
CREATE POLICY "Users can insert call log" ON call_log FOR INSERT WITH CHECK (auth.uid() = caller_id OR auth.uid() = receiver_id);
