-- Migration: Update phone column to support international format (+55XXYXXXXXXXX)
-- Created: 2024
-- Description: Changes phone column to store international format (15 characters) for WhatsApp integration

DO $$
BEGIN
    -- Check if users table exists
    IF EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'users'
    ) THEN
        -- Check if phone column exists
        IF EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_schema = 'public'
            AND table_name = 'users'
            AND column_name = 'phone'
        ) THEN
            -- Update existing phone numbers to international format if they're not already
            -- This converts local format (10 or 11 digits) to international format (+55...)
            UPDATE users
            SET phone = '+55' || phone
            WHERE phone IS NOT NULL
            AND phone NOT LIKE '+55%'
            AND LENGTH(phone) BETWEEN 10 AND 11;

            -- Alter column to support 15 characters (international format: +55XXYXXXXXXXX)
            ALTER TABLE users
            ALTER COLUMN phone TYPE VARCHAR(15);
        END IF;
    END IF;
END $$;

