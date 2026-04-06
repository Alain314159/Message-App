-- ============================================
-- CERLITA CHAT - DATABASE SCHEMA ACTUALIZADO
-- ============================================
-- Ejecutar este SQL en Supabase SQL Editor
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- TABLA: USERS
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL DEFAULT 'Usuario',
    photo_url TEXT,

    -- Presencia
    is_online BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMPTZ,
    is_typing BOOLEAN DEFAULT FALSE,

    -- Notificaciones
    push_token TEXT,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_online ON users(is_online);

-- ============================================
-- TABLA: CHATS
-- ============================================
CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT,
    is_group BOOLEAN NOT NULL DEFAULT FALSE,
    participant_ids UUID[] NOT NULL,

    -- Último mensaje
    last_message_id UUID REFERENCES messages(id),

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_chats_participants ON chats USING GIN(participant_ids);
CREATE INDEX IF NOT EXISTS idx_chats_updated_at ON chats(updated_at DESC);

-- ============================================
-- TABLA: CHAT_PARTICIPANTS
-- ============================================
CREATE TABLE IF NOT EXISTS chat_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Evitar duplicados
    UNIQUE(chat_id, user_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_chat_participants_chat_id ON chat_participants(chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_participants_user_id ON chat_participants(user_id);

-- ============================================
-- TABLA: MESSAGES
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Contenido
    message_type TEXT NOT NULL DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'video', 'audio', 'file')),
    content TEXT, -- Texto cifrado (E2E)
    media_url TEXT,
    thumbnail_url TEXT,
    reply_to_id UUID REFERENCES messages(id),

    -- Estado del mensaje
    status TEXT NOT NULL DEFAULT 'sent' CHECK (status IN ('sending', 'sent', 'delivered', 'read', 'failed')),
    is_edited BOOLEAN DEFAULT FALSE,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at DESC);

-- ============================================
-- TABLA: NOTIFICATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    data JSONB,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'sent', 'delivered', 'read')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a todas las tablas
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chats_updated_at BEFORE UPDATE ON chats
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_messages_updated_at BEFORE UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger para actualizar chat.updated_at cuando se inserta mensaje
CREATE OR REPLACE FUNCTION update_chat_on_new_message()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE chats
    SET
        last_message_id = NEW.id,
        updated_at = NOW()
    WHERE id = NEW.chat_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_chat_on_new_message_trigger
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_chat_on_new_message();

-- ============================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================

-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- ============================================
-- RLS POLICIES: USERS
-- ============================================

-- Users can view all users (para búsqueda)
CREATE POLICY "Users can view all users"
    ON users FOR SELECT
    USING (true);

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid() = id);

-- Users can insert their own profile (during signup)
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- ============================================
-- RLS POLICIES: CHATS
-- ============================================

-- Users can view chats where they are participants
CREATE POLICY "Users can view their chats"
    ON chats FOR SELECT
    USING (auth.uid() = ANY(participant_ids));

-- Users can create chats
CREATE POLICY "Users can create chats"
    ON chats FOR INSERT
    WITH CHECK (auth.uid() = ANY(participant_ids));

-- Users can update their chats
CREATE POLICY "Users can update their chats"
    ON chats FOR UPDATE
    USING (auth.uid() = ANY(participant_ids));

-- ============================================
-- RLS POLICIES: CHAT_PARTICIPANTS
-- ============================================

-- Users can view participants of their chats
CREATE POLICY "Users can view participants of their chats"
    ON chat_participants FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM chats
            WHERE chats.id = chat_participants.chat_id
            AND auth.uid() = ANY(chats.participant_ids)
        )
    );

-- Users can add themselves to chats
CREATE POLICY "Users can join chats"
    ON chat_participants FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- ============================================
-- RLS POLICIES: MESSAGES
-- ============================================

-- Users can view messages from their chats
CREATE POLICY "Users can view messages from their chats"
    ON messages FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM chats
            WHERE chats.id = messages.chat_id
            AND auth.uid() = ANY(chats.participant_ids)
        )
    );

-- Users can send messages to their chats
CREATE POLICY "Users can send messages"
    ON messages FOR INSERT
    WITH CHECK (
        auth.uid() = sender_id
        AND EXISTS (
            SELECT 1 FROM chats
            WHERE chats.id = messages.chat_id
            AND auth.uid() = ANY(chats.participant_ids)
        )
    );

-- Users can update their own messages
CREATE POLICY "Users can update own messages"
    ON messages FOR UPDATE
    USING (auth.uid() = sender_id);

-- Users can delete their own messages
CREATE POLICY "Users can delete own messages"
    ON messages FOR DELETE
    USING (auth.uid() = sender_id);

-- ============================================
-- RLS POLICIES: NOTIFICATIONS
-- ============================================

-- Users can view their own notifications
CREATE POLICY "Users can view own notifications"
    ON notifications FOR SELECT
    USING (auth.uid() = user_id);

-- Users can update their own notifications
CREATE POLICY "Users can update own notifications"
    ON notifications FOR UPDATE
    USING (auth.uid() = user_id);

-- System can insert notifications for users
CREATE POLICY "System can insert notifications"
    ON notifications FOR INSERT
    WITH CHECK (true);

-- ============================================
-- FUNCIONES AUXILIARES
-- ============================================

-- Función para obtener o crear chat directo
CREATE OR REPLACE FUNCTION get_or_create_direct_chat(user1_id UUID, user2_id UUID)
RETURNS UUID AS $$
DECLARE
    chat_id UUID;
BEGIN
    -- Try to find existing chat
    SELECT id INTO chat_id
    FROM chats
    WHERE is_group = FALSE
    AND participant_ids @> ARRAY[user1_id, user2_id]
    AND participant_ids <@ ARRAY[user1_id, user2_id]
    LIMIT 1;

    -- If not found, create new chat
    IF chat_id IS NULL THEN
        INSERT INTO chats (is_group, participant_ids)
        VALUES (FALSE, ARRAY[user1_id, user2_id])
        RETURNING id INTO chat_id;
    END IF;

    RETURN chat_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- STORAGE BUCKETS (opcional)
-- ============================================
-- Para crear buckets de almacenamiento:
-- 1. Ve a Supabase Dashboard > Storage
-- 2. Crea buckets: "images", "videos", "files"
-- 3. Hazlos públicos o privados según necesidad

-- ============================================
-- VERIFICACIÓN
-- ============================================
-- Después de ejecutar, verifica que existen:
-- ✅ Tabla: users
-- ✅ Tabla: chats
-- ✅ Tabla: chat_participants
-- ✅ Tabla: messages
-- ✅ Tabla: notifications
-- ✅ RLS activado en todas las tablas
-- ✅ Función: get_or_create_direct_chat
-- ✅ Triggers para actualizar timestamps
-- ============================================
