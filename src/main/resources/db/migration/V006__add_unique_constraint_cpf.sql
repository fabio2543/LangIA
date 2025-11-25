-- Migration: Add unique constraint to CPF column in users table
-- Created: 2024
-- Description: Ensures CPF uniqueness in the database

-- Check if the users table exists and add unique constraint to cpf_string column
-- This migration is idempotent - it will only add the constraint if it doesn't exist

DO $$
BEGIN
    -- Check if users table exists
    IF EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'users'
    ) THEN
        -- Check if unique constraint already exists
        IF NOT EXISTS (
            SELECT FROM pg_constraint
            WHERE conname = 'users_cpf_string_key'
        ) THEN
            -- Add unique constraint to cpf_string column
            ALTER TABLE users ADD CONSTRAINT users_cpf_string_key UNIQUE (cpf_string);

            -- Create index for better query performance (if it doesn't exist)
            IF NOT EXISTS (
                SELECT FROM pg_indexes
                WHERE tablename = 'users'
                AND indexname = 'idx_users_cpf_string'
            ) THEN
                CREATE INDEX idx_users_cpf_string ON users(cpf_string);
            END IF;
        END IF;
    END IF;
END $$;

