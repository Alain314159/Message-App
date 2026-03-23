-- ============================================
-- ESQUEMA DE BASE DE DATOS PARA SUPABASE
-- ============================================
-- 
-- Este script crea todas las tablas necesarias
-- para la app de mensajería romántica
--
-- Instrucciones:
-- 1. Ve a Supabase Dashboard → SQL Editor
-- 2. Copia y pega este script completo
-- 3. Click en "Run"
-- ============================================

-- ============================================
-- 1. TABLA DE USUARIOS
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY, -- UUID de Supabase Auth
    display_name TEXT NOT NULL DEFAULT 'Usuario',
    email TEXT,
    phone TEXT,
    photo_url TEXT,
    bio TEXT DEFAULT '',
    is_online BOOLEAN DEFAULT FALSE,
    last_seen BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    onesignal_player_id TEXT, -- Para notificaciones push
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())
);

-- Índice para búsquedas por email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Índice para estado online
CREATE INDEX IF NOT EXISTS idx_users_online ON users(is_online);

-- ============================================
-- 2. TABLA DE CHATS
-- ============================================
CREATE TABLE IF NOT EXISTS chats (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL DEFAULT 'direct', -- 'direct' o 'group'
    name TEXT, -- Solo para grupos
    photo_url TEXT,
    owner_id TEXT REFERENCES users(id), -- Solo para grupos
    member_ids TEXT[] NOT NULL DEFAULT '{}', -- Array de UUIDs de miembros
    last_message_enc TEXT, -- Último mensaje cifrado
    last_message_at BIGINT,
    pinned_message_id TEXT,
    pinned_snippet TEXT,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())
);

-- Índice para buscar chats por miembro
CREATE INDEX IF NOT EXISTS idx_chats_members ON chats USING GIN(member_ids);

-- Índice para ordenar por último mensaje
CREATE INDEX IF NOT EXISTS idx_chats_updated ON chats(updated_at DESC);

-- Índice para tipo de chat
CREATE INDEX IF NOT EXISTS idx_chats_type ON chats(type);

-- ============================================
-- 3. TABLA DE MENSAJES
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    chat_id TEXT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id TEXT NOT NULL REFERENCES users(id),
    type TEXT NOT NULL DEFAULT 'text', -- 'text', 'image', 'video', 'audio', 'deleted'
    text_enc TEXT, -- Texto cifrado (AES-256-GCM ciphertext)
    nonce TEXT, -- Nonce para AES-256-GCM
    auth_tag TEXT, -- Tag de autenticación
    media_url TEXT, -- URL para multimedia (si aplica)
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    delivered_at BIGINT, -- Cuando se entregó
    read_at BIGINT, -- Cuando se leyó
    deleted_for_all BOOLEAN DEFAULT FALSE,
    deleted_for TEXT[] DEFAULT '{}' -- Array de UUIDs que borraron el mensaje
);

-- Índice para buscar mensajes por chat
CREATE INDEX IF NOT EXISTS idx_messages_chat ON messages(chat_id);

-- Índice para ordenar por fecha
CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at ASC);

-- Índice para buscar por remitente
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);

-- Índice para mensajes no leídos
CREATE INDEX IF NOT EXISTS idx_messages_unread ON messages(read_at) WHERE read_at IS NULL;

-- ============================================
-- 4. TABLA DE CONTACTOS (OPCIONAL)
-- ============================================
CREATE TABLE IF NOT EXISTS contacts (
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    alias TEXT DEFAULT '',
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    PRIMARY KEY (user_id, contact_user_id)
);

-- Índice para buscar contactos por usuario
CREATE INDEX IF NOT EXISTS idx_contacts_user ON contacts(user_id);

-- ============================================
-- 5. REGLAS DE SEGURIDAD (RLS)
-- ============================================
-- Habilitar Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;

-- ============================================
-- POLÍTICAS PARA USERS
-- ============================================
-- Los usuarios pueden ver su propio perfil
CREATE POLICY "Users can view own profile"
    ON users FOR SELECT
    USING (auth.uid()::text = id);

-- Los usuarios pueden actualizar su propio perfil
CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid()::text = id);

-- Los usuarios pueden insertar su propio perfil (al registrarse)
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid()::text = id);

-- ============================================
-- POLÍTICAS PARA CHATS
-- ============================================
-- Los usuarios pueden ver chats donde son miembros
CREATE POLICY "Users can view member chats"
    ON chats FOR SELECT
    USING (auth.uid()::text = ANY(member_ids));

-- Los usuarios pueden crear chats
CREATE POLICY "Users can create chats"
    ON chats FOR INSERT
    WITH CHECK (auth.uid()::text = ANY(member_ids));

-- Los usuarios pueden actualizar chats donde son miembros
CREATE POLICY "Users can update member chats"
    ON chats FOR UPDATE
    USING (auth.uid()::text = ANY(member_ids));

-- ============================================
-- POLÍTICAS PARA MENSAJES
-- ============================================
-- Los usuarios pueden ver mensajes de sus chats
CREATE POLICY "Users can view chat messages"
    ON messages FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM chats
            WHERE chats.id = messages.chat_id
            AND auth.uid()::text = ANY(chats.member_ids)
        )
    );

-- Los usuarios pueden enviar mensajes a sus chats
CREATE POLICY "Users can send messages"
    ON messages FOR INSERT
    WITH CHECK (
        auth.uid()::text = sender_id AND
        EXISTS (
            SELECT 1 FROM chats
            WHERE chats.id = messages.chat_id
            AND auth.uid()::text = ANY(chats.member_ids)
        )
    );

-- Los usuarios pueden actualizar sus propios mensajes
CREATE POLICY "Users can update own messages"
    ON messages FOR UPDATE
    USING (auth.uid()::text = sender_id);

-- ============================================
-- POLÍTICAS PARA CONTACTOS
-- ============================================
-- Los usuarios pueden ver sus propios contactos
CREATE POLICY "Users can view own contacts"
    ON contacts FOR SELECT
    USING (auth.uid()::text = user_id);

-- Los usuarios pueden agregar contactos
CREATE POLICY "Users can insert contacts"
    ON contacts FOR INSERT
    WITH CHECK (auth.uid()::text = user_id);

-- Los usuarios pueden eliminar sus contactos
CREATE POLICY "Users can delete contacts"
    ON contacts FOR DELETE
    USING (auth.uid()::text = user_id);

-- ============================================
-- 6. FUNCIONES Y TRIGGERS
-- ============================================

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = EXTRACT(EPOCH FROM NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para users
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para chats
CREATE TRIGGER update_chats_updated_at
    BEFORE UPDATE ON chats
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 7. DATOS DE EJEMPLO (OPCIONAL)
-- ============================================
-- Descomenta esto si quieres datos de prueba

/*
-- Insertar usuario de prueba (necesitas auth.uid() válido)
-- Esto es solo para debugging local
INSERT INTO users (id, display_name, email, is_online)
VALUES ('test-user-1', 'Usuario Test', 'test@example.com', TRUE);
*/

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
-- 
-- Verificación:
-- 1. Ve a Table Editor en Supabase
-- 2. Deberías ver las tablas: users, chats, messages, contacts
-- 3. Las políticas de seguridad están activas
-- 
-- Siguiente paso:
-- - Configura SupabaseConfig.kt con tus credenciales
-- - Configura OneSignal
-- ============================================
