-- ============================================
-- ESQUEMA DE BASE DE DATOS - MESSAGE APP ROMÁNTICA
-- ============================================
-- Especificación completa con:
-- - Sistema de emparejamiento (código 6 dígitos + email)
-- - Typing indicators
-- - Estados de mensaje (Sent/Delivered/Read)
-- - Presencia completa (online/last seen/typing)
-- ============================================

-- ============================================
-- EXTENSIONES NECESARIAS
-- ============================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. TABLA USUARIOS (Extendida)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL DEFAULT 'Usuario',
    photo_url TEXT,              -- URL de Supabase Storage
    bio TEXT DEFAULT '',
    
    -- Sistema de emparejamiento
    pairing_code VARCHAR(6) UNIQUE,  -- Código de 6 dígitos para invitar
    partner_id UUID REFERENCES users(id), -- ID de la pareja vinculada
    is_paired BOOLEAN DEFAULT FALSE,
    
    -- Presencia
    is_online BOOLEAN DEFAULT FALSE,
    last_seen BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    is_typing BOOLEAN DEFAULT FALSE,
    typing_in_chat UUID,         -- En qué chat está escribiendo
    
    -- Notificaciones
    onesignal_player_id TEXT,       -- Legacy (usar para JPush también)
    jpush_registration_id TEXT,     -- JPush Registration ID (nuevo)

    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())
);

-- Índices optimizados
CREATE INDEX idx_users_pairing_code ON users(pairing_code) WHERE pairing_code IS NOT NULL;
CREATE INDEX idx_users_partner ON users(partner_id) WHERE partner_id IS NOT NULL;
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_online ON users(is_online);

-- ============================================
-- 2. TABLA CHATS (Solo 1 chat por pareja)
-- ============================================
CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL DEFAULT 'couple', -- 'couple' siempre para esta app
    member_ids UUID[] NOT NULL DEFAULT '{}',

    -- Estados de typing (duplicado para rapidez)
    user1_typing BOOLEAN DEFAULT FALSE,
    user2_typing BOOLEAN DEFAULT FALSE,

    -- Mensaje fijado
    pinned_message_id UUID,
    pinned_snippet TEXT,

    -- Metadatos
    last_message_enc TEXT,
    last_message_at BIGINT,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),

    -- Constraint: Solo 2 miembros permitidos
    CONSTRAINT chk_two_members CHECK (array_length(member_ids, 1) = 2)
);

CREATE INDEX idx_chats_members ON chats USING GIN(member_ids);
CREATE INDEX idx_chats_updated ON chats(updated_at DESC);
CREATE INDEX idx_chats_typing ON chats(user1_typing, user2_typing);

-- ============================================
-- 3. TABLA MENSAJES (Con estados completos)
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type TEXT NOT NULL DEFAULT 'text', -- 'text', 'image', 'video', 'audio'
    
    -- Contenido cifrado
    text_enc TEXT,               -- Texto cifrado con Android Keystore
    nonce TEXT,                  -- IV para AES-256-GCM
    media_url TEXT,              -- URL de Supabase Storage
    
    -- Estados de mensaje (WhatsApp style)
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
    delivered_at BIGINT,         -- Cuando llegó al dispositivo
    read_at BIGINT,              -- Cuando abrió el chat
    
    -- Soft delete
    deleted_for_all BOOLEAN DEFAULT FALSE,
    deleted_for UUID[] DEFAULT '{}',
    
    -- Constraint: Verificar tipo de mensaje
    CONSTRAINT chk_message_type CHECK (type IN ('text', 'image', 'video', 'audio', 'deleted'))
);

-- Índices críticos para performance
CREATE INDEX idx_messages_chat_created ON messages(chat_id, created_at DESC);
CREATE INDEX idx_messages_unread ON messages(read_at) WHERE read_at IS NULL;
CREATE INDEX idx_messages_delivered ON messages(delivered_at) WHERE delivered_at IS NULL;
CREATE INDEX idx_messages_sender ON messages(sender_id);

-- ============================================
-- 4. FUNCIONES AUXILIARES
-- ============================================

-- Generar código de emparejamiento único de 6 dígitos
CREATE OR REPLACE FUNCTION generate_pairing_code()
RETURNS VARCHAR(6) AS $$
DECLARE
    code VARCHAR(6);
    exists_check BOOLEAN;
BEGIN
    LOOP
        -- Generar número aleatorio 6 dígitos (100000-999999)
        code := LPAD(FLOOR(RANDOM() * 900000 + 100000)::TEXT, 6, '0');
        
        -- Verificar si existe
        SELECT EXISTS(SELECT 1 FROM users WHERE pairing_code = code) INTO exists_check;
        
        EXIT WHEN NOT exists_check;
    END LOOP;
    
    RETURN code;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Crear chat entre pareja automáticamente
CREATE OR REPLACE FUNCTION create_couple_chat()
RETURNS TRIGGER AS $$
BEGIN
    -- Si se actualizó partner_id y ahora tiene pareja
    IF NEW.partner_id IS NOT NULL AND OLD.partner_id IS NULL THEN
        -- Crear chat con ambos miembros
        INSERT INTO chats (member_ids, created_at, updated_at)
        VALUES (ARRAY[NEW.id, NEW.partner_id], 
                EXTRACT(EPOCH FROM NOW()), 
                EXTRACT(EPOCH FROM NOW()));
        
        -- Actualizar la otra persona también
        UPDATE users SET 
            partner_id = NEW.id,
            is_paired = TRUE,
            updated_at = EXTRACT(EPOCH FROM NOW())
        WHERE id = NEW.partner_id AND partner_id IS NULL;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trigger_create_chat_on_pair
    AFTER UPDATE OF partner_id ON users
    FOR EACH ROW
    EXECUTE FUNCTION create_couple_chat();

-- Actualizar último mensaje del chat automáticamente
CREATE OR REPLACE FUNCTION update_chat_last_message()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.deleted_for_all = FALSE THEN
        UPDATE chats SET
            last_message_enc = NEW.text_enc,
            last_message_at = NEW.created_at,
            updated_at = EXTRACT(EPOCH FROM NOW())
        WHERE id = NEW.chat_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_chat_on_message
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_chat_last_message();

-- ============================================
-- 5. POLÍTICAS DE SEGURIDAD (RLS)
-- ============================================

-- Users: Solo ver/editar propio perfil y datos de pareja
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own and partner data"
    ON users FOR SELECT
    USING (auth.uid() = id OR auth.uid() = partner_id);

CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Chats: Solo miembros pueden ver
ALTER TABLE chats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Couple members can view chat"
    ON chats FOR SELECT
    USING (auth.uid() = ANY(member_ids));

CREATE POLICY "Couple members can create chat"
    ON chats FOR INSERT
    WITH CHECK (auth.uid() = ANY(member_ids));

CREATE POLICY "Couple members can update typing"
    ON chats FOR UPDATE
    USING (auth.uid() = ANY(member_ids));

-- Messages: Solo miembros del chat
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Members can view messages"
    ON messages FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM chats 
            WHERE chats.id = messages.chat_id 
            AND auth.uid() = ANY(chats.member_ids)
        )
    );

CREATE POLICY "Members can insert messages"
    ON messages FOR INSERT
    WITH CHECK (
        auth.uid() = sender_id AND
        EXISTS (
            SELECT 1 FROM chats 
            WHERE chats.id = messages.chat_id 
            AND auth.uid() = ANY(chats.member_ids)
        )
    );

CREATE POLICY "Sender can update own messages"
    ON messages FOR UPDATE
    USING (auth.uid() = sender_id);

CREATE POLICY "Receiver can update delivery status"
    ON messages FOR UPDATE
    USING (
        auth.uid() != sender_id AND
        EXISTS (
            SELECT 1 FROM chats 
            WHERE chats.id = messages.chat_id 
            AND auth.uid() = ANY(chats.member_ids)
        )
    );

-- ============================================
-- 6. REALTIME CONFIG (Para typing indicators y mensajes)
-- ============================================

-- Habilitar realtime en las tablas necesarias
DO $$ BEGIN
    CREATE PUBLICATION supabase_realtime;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Añadir tablas a realtime
ALTER PUBLICATION supabase_realtime ADD TABLE messages;
ALTER PUBLICATION supabase_realtime ADD TABLE chats;
ALTER PUBLICATION supabase_realtime ADD TABLE users;

-- ============================================
-- 7. DATOS DE EJEMPLO (OPCIONAL - SOLO TESTING)
-- ============================================
-- Descomentar solo para testing local

/*
-- Crear dos usuarios de prueba
INSERT INTO users (id, email, display_name, is_paired)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'user1@test.com', 'Usuario 1', FALSE),
    ('00000000-0000-0000-0000-000000000002', 'user2@test.com', 'Usuario 2', FALSE);

-- Vincular usuarios
UPDATE users SET partner_id = '00000000-0000-0000-0000-000000000002' WHERE id = '00000000-0000-0000-0000-000000000001';
UPDATE users SET partner_id = '00000000-0000-0000-0000-000000000001' WHERE id = '00000000-0000-0000-0000-000000000002';
UPDATE users SET is_paired = TRUE WHERE id IN ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002');

-- Crear chat de prueba
INSERT INTO chats (member_ids, created_at, updated_at)
VALUES (ARRAY['00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002']);

-- Crear mensaje de prueba
INSERT INTO messages (chat_id, sender_id, type, text_enc, created_at)
VALUES (
    (SELECT id FROM chats LIMIT 1),
    '00000000-0000-0000-0000-000000000001',
    'text',
    'SGVsbG8gV29ybGQh', -- Base64 de "Hello World!"
    EXTRACT(EPOCH FROM NOW())
);
*/

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
-- 
-- Instrucciones:
-- 1. Ve a Supabase Dashboard → SQL Editor
-- 2. Copia y pega este script completo
-- 3. Click en "Run"
-- 4. Verifica en Table Editor que las tablas se crearon correctamente
-- 
-- Tablas esperadas:
-- - users (con pairing_code, partner_id, typing)
-- - chats (con user1_typing, user2_typing)
-- - messages (con delivered_at, read_at)
-- 
-- Funciones esperadas:
-- - generate_pairing_code()
-- - create_couple_chat()
-- - update_chat_last_message()
-- 
-- Triggers esperados:
-- - trigger_create_chat_on_pair
-- - trigger_update_chat_on_message
-- ============================================
