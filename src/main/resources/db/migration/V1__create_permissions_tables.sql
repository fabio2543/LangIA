-- Migration: Create permissions tables (functionalities, profiles, profile_functionalities)
-- Created: 2024

-- Table: functionalities
-- Stores system functionalities/features that can be granted to profiles
CREATE TABLE functionalities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    module VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: profiles
-- Stores user profiles with hierarchical levels
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    hierarchy_level INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_hierarchy_level CHECK (hierarchy_level IN (1, 2, 3))
);

-- Table: profile_functionalities
-- Junction table: Associates profiles with functionalities
CREATE TABLE profile_functionalities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    functionality_id UUID NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by_inheritance BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_profile_functionalities_profile
        FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_functionalities_functionality
        FOREIGN KEY (functionality_id) REFERENCES functionalities(id) ON DELETE CASCADE,
    CONSTRAINT uk_profile_functionality UNIQUE (profile_id, functionality_id)
);

-- Indexes for performance
CREATE INDEX idx_profile_functionalities_profile_id ON profile_functionalities(profile_id);
CREATE INDEX idx_profile_functionalities_functionality_id ON profile_functionalities(functionality_id);
CREATE INDEX idx_functionalities_code ON functionalities(code);
CREATE INDEX idx_functionalities_module ON functionalities(module);
CREATE INDEX idx_functionalities_active ON functionalities(active);
CREATE INDEX idx_profiles_code ON profiles(code);
CREATE INDEX idx_profiles_hierarchy_level ON profiles(hierarchy_level);
CREATE INDEX idx_profiles_active ON profiles(active);

-- Insert default profiles
INSERT INTO profiles (code, name, description, hierarchy_level, active) VALUES
    ('STUDENT', 'Student', 'Student profile with basic system access', 1, true),
    ('TEACHER', 'Teacher', 'Teacher profile with access to create and manage lessons', 2, true),
    ('ADMIN', 'Administrator', 'Administrator profile with full system access', 3, true);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to automatically update updated_at
CREATE TRIGGER update_functionalities_updated_at
    BEFORE UPDATE ON functionalities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
