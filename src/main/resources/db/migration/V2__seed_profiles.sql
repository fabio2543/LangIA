-- ================================================
-- Migration V2: Seed Profiles
-- Description: Inserts initial profile data
-- ================================================

INSERT INTO profiles (code, name, description, hierarchy_level, active) VALUES
('STUDENT', 'Student', 'Student profile with basic learning permissions', 1, TRUE),
('TEACHER', 'Teacher', 'Teacher profile with course management and student monitoring permissions', 2, TRUE),
('ADMIN', 'Administrator', 'Administrator profile with full system access', 3, TRUE)
ON CONFLICT (code) DO NOTHING;
