-- ================================================
-- Migration V025: Remove unused Portuguese functionalities tables
-- Description: Drops unused Portuguese tables (funcionalidades, perfil_funcionalidades)
-- ================================================

-- Drop junction table first (has foreign key)
DROP TABLE IF EXISTS perfil_funcionalidades CASCADE;

-- Drop main table
DROP TABLE IF EXISTS funcionalidades CASCADE;
