// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

import { describe, expect, it } from 'vitest';
import { editSchema } from './editSchema';

/** Pure schema tests – mirror the backend US04 validation rules. */
describe('editSchema (US04 validation)', () => {
  it('accepts a valid payload', () => {
    const result = editSchema.safeParse({
      localizedName: 'Mysterious Bulba',
      geographicMetadata: 'Viridian Forest, Kanto',
      internalClassificationTags: ['grass', 'starter'],
    });
    expect(result.success).toBe(true);
  });

  it('rejects names longer than 60 characters', () => {
    const result = editSchema.safeParse({ localizedName: 'x'.repeat(61), internalClassificationTags: [] });
    expect(result.success).toBe(false);
  });

  it('rejects more than 10 tags', () => {
    const result = editSchema.safeParse({ internalClassificationTags: Array.from({ length: 11 }, (_, i) => `t${i}`) });
    expect(result.success).toBe(false);
  });

  it('rejects blank or over-long individual tags', () => {
    expect(editSchema.safeParse({ internalClassificationTags: [''] }).success).toBe(false);
    expect(editSchema.safeParse({ internalClassificationTags: ['y'.repeat(31)] }).success).toBe(false);
  });

  it('rejects geographic metadata longer than 255 characters', () => {
    const result = editSchema.safeParse({ geographicMetadata: 'z'.repeat(256), internalClassificationTags: [] });
    expect(result.success).toBe(false);
  });
});
