-- V4: Change internal_classification_tags column from jsonb to text to store JSON strings without casting issues.
ALTER TABLE pokemon_local ALTER COLUMN internal_classification_tags TYPE TEXT;
