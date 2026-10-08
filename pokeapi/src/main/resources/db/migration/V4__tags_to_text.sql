-- Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
-- This source code is licensed under the Restricted Use License found in the
-- LICENSE.md file in the root directory of this source tree.

-- V4: Change internal_classification_tags column from jsonb to text to store JSON strings without casting issues.
ALTER TABLE pokemon_local ALTER COLUMN internal_classification_tags TYPE TEXT;
