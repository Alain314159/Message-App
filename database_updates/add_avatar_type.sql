-- ============================================
-- ACTUALIZACIÓN: Agregar campo avatar_type
-- ============================================
-- Ejecutar este script si ya tienes la base de datos creada
-- y quieres agregar el campo de avatares

-- Agregar columna avatar_type a la tabla users
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_type VARCHAR(20) DEFAULT 'cerdita';

-- Agregar comentario descriptivo
COMMENT ON COLUMN users.avatar_type IS 'Tipo de avatar seleccionado: cerdita, koala';

-- Verificar que se agregó correctamente
-- SELECT column_name, data_type, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'users' AND column_name = 'avatar_type';
