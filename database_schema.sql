-- ============================================
-- ESQUEMA DE BASE DE DATOS PARA SUPABASE
-- ============================================
-- 
-- ✅ VERIFICADO: Este script usa tipos de dato correctos para Supabase 2024-2025
-- 
-- Instrucciones:
-- 1. Ve a Supabase Dashboard → SQL Editor
-- 2. Copia y pega este script completo
-- 3. Click en "Run"
-- 4. Verifica en Table Editor que las tablas se crearon correctamente
-- 
-- Documentación:
-- https://supabase.com/docs/guides/database/tables
-- https://supabase.com/docs/guides/auth/row-level-security
-- ============================================

-- ============================================
-- 1. TABLA DE USUARIOS
-- ============================================
-- Nota: id UUID REFERENCES auth.users(id) vincula con Supabase Auth
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL DEFAULT 'Usuario',
    email TEXT,
    phone TEXT,
    photo_url TEXT,
    bio TEXT DEFAULT '',
    is_online BOOLEAN DEFAULT FALSE,
    last_seen BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    onesignal_player_id TEXT, -- Para notificaciones push (OneSignal)
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())
);

-- Índice para búsquedas por email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Índice para estado online
CREATE INDEX IF NOT EXISTS idx_users_online ON users(is_online);

-- Índice para OneSignal Player ID
CREATE INDEX IF NOT EXISTS idx_users_onesignal ON users(onesignal_player_id) WHERE onesignal_player_id IS NOT NULL;

-- ============================================
-- 2. TABLA DE CHATS
-- ============================================
-- member_ids usa UUID[] (array de UUIDs) en lugar de TEXT[]
CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL DEFAULT 'direct', -- 'direct' o 'group'
    name TEXT, -- Solo para grupos
    photo_url TEXT,
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL, -- Solo para grupos
    member_ids UUID[] NOT NULL DEFAULT '{}', -- Array de UUIDs de miembros
    last_message_enc TEXT, -- Último mensaje cifrado
    last_message_at BIGINT,
    pinned_message_id UUID,
    pinned_snippet TEXT,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())
);

-- Índice para buscar chats por miembro (usa GIN para arrays)
CREATE INDEX IF NOT EXISTS idx_chats_members ON chats USING GIN(member_ids);

-- Índice para ordenar por último mensaje
CREATE INDEX IF NOT EXISTS idx_chats_updated ON chats(updated_at DESC);

-- Índice para tipo de chat
CREATE INDEX IF NOT EXISTS idx_chats_type ON chats(type);

-- ============================================
-- 3. TABLA DE MENSAJES
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type TEXT NOT NULL DEFAULT 'text', -- 'text', 'image', 'video', 'audio', 'deleted'
    text_enc TEXT, -- Texto cifrado (AES-256-GCM ciphertext)
    nonce TEXT, -- Nonce para AES-256-GCM (no usado con Android Keystore)
    auth_tag TEXT, -- Tag de autenticación (no usado con Android Keystore)
    media_url TEXT, -- URL para multimedia (si aplica)
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    delivered_at BIGINT, -- Cuando se entregó
    read_at BIGINT, -- Cuando se leyó
    deleted_for_all BOOLEAN DEFAULT FALSE,
    deleted_for UUID[] DEFAULT '{}' -- Array de UUIDs que borraron el mensaje
);

-- Índice para buscar mensajes por chat
CREATE INDEX IF NOT EXISTS idx_messages_chat ON messages(chat_id);

-- Índice para ordenar por fecha (DESC para mostrar más recientes primero)
CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at DESC);

-- Índice para buscar por remitente
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);

-- Índice para mensajes no leídos
CREATE INDEX IF NOT EXISTS idx_messages_unread ON messages(read_at) WHERE read_at IS NULL;

-- Índice para mensajes por chat y fecha (compuesto para mejor rendimiento)
CREATE INDEX IF NOT EXISTS idx_messages_chat_created ON messages(chat_id, created_at DESC);

-- ============================================
-- 4. TABLA DE CONTACTOS (OPCIONAL)
-- ============================================
CREATE TABLE IF NOT EXISTS contacts (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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
    USING (auth.uid() = id);

-- Los usuarios pueden actualizar su propio perfil
CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid() = id);

-- Los usuarios pueden insertar su propio perfil (al registrarse)
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- ============================================
-- POLÍTICAS PARA CHATS
-- ============================================

-- Los usuarios pueden ver chats donde son miembros
CREATE POLICY "Users can view member chats"
    ON chats FOR SELECT
    USING (auth.uid() = ANY(member_ids));

-- Los usuarios pueden crear chats
CREATE POLICY "Users can create chats"
    ON chats FOR INSERT
    WITH CHECK (auth.uid() = ANY(member_ids));

-- Los usuarios pueden actualizar chats donde son miembros
CREATE POLICY "Users can update member chats"
    ON chats FOR UPDATE
    USING (auth.uid() = ANY(member_ids));

-- Los usuarios pueden eliminar chats donde son miembros
CREATE POLICY "Users can delete member chats"
    ON chats FOR DELETE
    USING (auth.uid() = ANY(member_ids));

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
            AND auth.uid() = ANY(chats.member_ids)
        )
    );

-- Los usuarios pueden enviar mensajes a sus chats
CREATE POLICY "Users can send messages"
    ON messages FOR INSERT
    WITH CHECK (
        auth.uid() = sender_id AND
        EXISTS (
            SELECT 1 FROM chats 
            WHERE chats.id = messages.chat_id 
            AND auth.uid() = ANY(chats.member_ids)
        )
    );

-- Los usuarios pueden actualizar sus propios mensajes
CREATE POLICY "Users can update own messages"
    ON messages FOR UPDATE
    USING (auth.uid() = sender_id);

-- Los usuarios pueden eliminar sus propios mensajes
CREATE POLICY "Users can delete own messages"
    ON messages FOR DELETE
    USING (auth.uid() = sender_id);

-- ============================================
-- POLÍTICAS PARA CONTACTOS
-- ============================================

-- Los usuarios pueden ver sus propios contactos
CREATE POLICY "Users can view own contacts"
    ON contacts FOR SELECT
    USING (auth.uid() = user_id);

-- Los usuarios pueden agregar contactos
CREATE POLICY "Users can insert contacts"
    ON contacts FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Los usuarios pueden eliminar sus contactos
CREATE POLICY "Users can delete contacts"
    ON contacts FOR DELETE
    USING (auth.uid() = user_id);

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
-- 7. FUNCIÓN PARA CREAR CHAT DIRECTO
-- ============================================
-- Esta función crea o verifica un chat directo entre 2 usuarios

CREATE OR REPLACE FUNCTION get_or_create_direct_chat(user1_id UUID, user2_id UUID)
RETURNS UUID AS $$
DECLARE
    chat_id UUID;
    sorted_member_ids UUID[];
BEGIN
    -- Ordenar member_ids para evitar duplicados
    sorted_member_ids := ARRAY(SELECT unnest(ARRAY[user1_id, user2_id]) ORDER BY 1);
    
    -- Buscar chat existente
    SELECT id INTO chat_id
    FROM chats
    WHERE type = 'direct'
      AND member_ids = sorted_member_ids
    LIMIT 1;
    
    -- Si no existe, crear nuevo chat
    IF chat_id IS NULL THEN
        INSERT INTO chats (type, member_ids, created_at, updated_at)
        VALUES ('direct', sorted_member_ids, EXTRACT(EPOCH FROM NOW()), EXTRACT(EPOCH FROM NOW()))
        RETURNING id INTO chat_id;
    END IF;
    
    RETURN chat_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- 8. VISTAS ÚTILES (OPCIONAL)
-- ============================================

-- Vista para mostrar últimos mensajes por chat
CREATE OR REPLACE VIEW chat_last_messages AS
SELECT DISTINCT ON (chat_id)
    chat_id,
    id AS message_id,
    sender_id,
    type,
    text_enc,
    created_at
FROM messages
ORDER BY chat_id, created_at DESC;

-- ============================================
-- 9. COMENTARIOS EN LAS TABLAS (DOCUMENTACIÓN)
-- ============================================

COMMENT ON TABLE users IS 'Perfiles de usuario vinculados con Supabase Auth';
COMMENT ON TABLE chats IS 'Conversaciones entre usuarios (directas o grupos)';
COMMENT ON TABLE messages IS 'Mensajes cifrados dentro de chats';
COMMENT ON TABLE contacts IS 'Lista de contactos de cada usuario';

COMMENT ON COLUMN users.onesignal_player_id IS 'OneSignal Player ID para notificaciones push';
COMMENT ON COLUMN chats.member_ids IS 'Array de UUIDs de miembros del chat';
COMMENT ON COLUMN messages.text_enc IS 'Mensaje cifrado con AES-256-GCM (Android Keystore)';
COMMENT ON COLUMN messages.deleted_for IS 'Array de UUIDs de usuarios que eliminaron el mensaje';

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
-- 
-- Verificación:
-- 1. Ve a Table Editor en Supabase
-- 2. Deberías ver las tablas: users, chats, messages, contacts
-- 3. Verifica que las políticas de seguridad están activas
-- 4. Prueba la función: SELECT get_or_create_direct_chat('uuid-1', 'uuid-2');
-- 
-- Siguientes pasos:
-- 1. Configura SupabaseConfig.kt con tus credenciales
-- 2. Configura OneSignal
-- 3. Prueba la app en Android Studio
-- ============================================
