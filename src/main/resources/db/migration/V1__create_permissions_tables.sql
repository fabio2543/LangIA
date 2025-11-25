-- ================================================
-- Migration V1: Create Permissions Structure
-- Description: Creates tables for functionalities, profiles and their associations
-- ================================================

-- Table: functionalities
-- Stores all available functionalities/permissions in the system
CREATE TABLE IF NOT EXISTS functionalities (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL,
    module VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups by code
CREATE INDEX IF NOT EXISTS idx_functionalities_code ON functionalities(code);

-- Index for filtering by module
CREATE INDEX IF NOT EXISTS idx_functionalities_module ON functionalities(module);

-- Index for active functionalities
CREATE INDEX IF NOT EXISTS idx_functionalities_active ON functionalities(active);

-- ================================================

-- Table: profiles
-- Stores user profiles with hierarchy levels
CREATE TABLE IF NOT EXISTS profiles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    hierarchy_level INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups by code
CREATE INDEX IF NOT EXISTS idx_profiles_code ON profiles(code);

-- Index for hierarchy level queries
CREATE INDEX IF NOT EXISTS idx_profiles_hierarchy ON profiles(hierarchy_level);

-- Index for active profiles
CREATE INDEX IF NOT EXISTS idx_profiles_active ON profiles(active);

-- ================================================

-- Table: profile_functionalities
-- Junction table between profiles and functionalities
CREATE TABLE IF NOT EXISTS profile_functionalities (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    functionality_id BIGINT NOT NULL,
    granted_by_inheritance BOOLEAN NOT NULL DEFAULT FALSE,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_profile_functionalities_profile
        FOREIGN KEY (profile_id)
        REFERENCES profiles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_profile_functionalities_functionality
        FOREIGN KEY (functionality_id)
        REFERENCES functionalities(id)
        ON DELETE CASCADE,

    -- Unique constraint: each profile can have each functionality only once
    CONSTRAINT uk_profile_functionality
        UNIQUE (profile_id, functionality_id)
);

-- Index for queries by profile
CREATE INDEX IF NOT EXISTS idx_profile_functionalities_profile ON profile_functionalities(profile_id);

-- Index for queries by functionality
CREATE INDEX IF NOT EXISTS idx_profile_functionalities_functionality ON profile_functionalities(functionality_id);

-- Index for inheritance queries
CREATE INDEX IF NOT EXISTS idx_profile_functionalities_inheritance ON profile_functionalities(granted_by_inheritance);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_profile_functionalities_profile_granted
    ON profile_functionalities(profile_id, granted_by_inheritance);
