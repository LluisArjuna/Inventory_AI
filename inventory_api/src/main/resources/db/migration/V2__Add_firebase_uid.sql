-- V2__Add_firebase_uid.sql
-- Add firebase_uid column to users table
ALTER TABLE users ADD COLUMN firebase_uid VARCHAR(255) UNIQUE;
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
