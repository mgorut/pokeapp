<!--
  Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
  
  This source code is licensed under the Restricted Use License found in the
  LICENSE.md file in the root directory of this source tree.
-->

The exercise requires you to document your use of GenAI tools. This file helps you structure that documentation.

---
Document the EXACT prompt you used to generate the API scaffold or full implementation.

**Template:**
```markdown
- Project overview and requirements
- Tech stack constraints
- Clean Architecture principles
- Specific user stories
[Insert your full prompt here - use the prompts from files 00-04]
1. **First attempt**: [What you asked]
2. **AI output**: [Summary of what was generated]
3. **Corrections needed**: [What was wrong/missing]
4. **Refined prompt**: [How you improved the prompt]
5. **Final output**: [What was generated after corrections]
```

---
Show a representative sample of the AI-generated code (not the entire codebase).

**Template:**
```markdown
\`\`\`java
@Service
public class PokemonSyncService {
}
\`\`\`
1. Missing null check on PokeAPI response
2. No transaction management
3. Did not use domain exceptions
1. Added null check and custom exception
2. Added @Transactional annotation
3. Refactored to throw PokemonNotFoundException
```

---
Describe HOW you validated the AI's suggestions.

**Template:**
```markdown
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Code compiles without errors
- [ ] No security vulnerabilities (SonarQube/Snyk)
- [ ] Follows project coding standards (Checkstyle/ESLint)
- [ ] Code review against Clean Architecture principles
- [ ] Domain layer has no framework dependencies
- [ ] Business logic is testable
- [ ] Error handling is comprehensive
- [ ] Edge cases are covered
- Wrote tests BEFORE accepting AI code (TDD approach)
- Used AI to generate test cases, then validated coverage
- Manually tested edge cases AI might have missed
```

---
Document specific corrections or improvements you made.

**Template:**
```markdown
**AI Output**: Did not implement caching for PokeAPI responses.
**Issue**: US01 requires caching as a nice-to-have.
**Improvement**: Added Spring Cache with Caffeine, configured TTL of 24 hours.
**Code Change**:
\`\`\`java
@Cacheable(value = "pokemonList", key = "#page + '-' + #size")
public Page<PokemonListItem> getPokemonList(int page, int size) {
}
\`\`\`
**AI Output**: Only fetched first evolution stage.
**Issue**: US02 requires full evolutionary lineage.
**Improvement**: Implemented recursive resolution of evolution chain.
**Code Change**: [Show the improved code]
**AI Output**: Basic @NotBlank validation.
**Issue**: Did not handle edge cases like empty tags list, oversized payloads.
**Improvement**: Added custom validators, size constraints, and defensive checks.
```

---
Document how you handled these critical areas.

**Template:**
```markdown
**Scenario**: External API returns 503 or times out.
**Solution**: 
- Implemented retry logic with exponential backoff
- Return 503 to client with user-friendly message
- Log error for monitoring
**Scenario**: Two users try to update the same Pokemon simultaneously.
**Solution**:
- Optimistic locking with @Version field
- Return 409 Conflict if version mismatch
- Frontend shows "Data was modified by another user" message
**Scenario**: Expired or malformed JWT token.
**Solution**:
- JWT filter validates token on every request
- Return 401 Unauthorized with clear message
- Frontend redirects to login page
- JWT stored in HttpOnly cookie (security best practice)
- Token expiration: 24 hours
- Refresh token mechanism (optional)
- Password hashing: BCrypt with strength 12
- Bean Validation (JSR-380) for request payloads
- Custom validators for complex business rules
- Global exception handler for consistent error responses
- Frontend validation with Zod (mirrors backend rules)
```

---
1. **Clean Architecture**: Show how layers are separated
2. **TDD Approach**: Show test-first workflow
3. **GenAI Workflow**: Demonstrate prompt → output → validation → correction cycle
4. **Error Handling**: Show global exception handler in action
5. **Testing**: Run tests live, show coverage report
- "I used GenAI to accelerate development, but validated every suggestion against Clean Architecture principles"
- "The domain layer has zero framework dependencies, ensuring testability and maintainability"
- "I wrote tests first, then used AI to generate implementation, then refined based on test failures"
- "Edge cases like concurrent updates and external API failures are handled defensively"

---
Use this template to create your GenAI documentation. Be specific, show code examples, and demonstrate critical thinking.
