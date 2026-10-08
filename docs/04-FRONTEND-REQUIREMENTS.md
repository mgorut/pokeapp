# 🎨 Frontend Requirements - React + TypeScript

## Context
Build a modern, responsive frontend that consumes the PokéManager API. Primary evaluation criteria: responsiveness, user-centric design, clean architecture, efficient state management.

---

## 🛠️ Tech Stack (MANDATORY)
- **Framework**: React 18+ with Vite
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS
- **State Management**: React Query (TanStack Query) for server state
- **Routing**: React Router v6
- **HTTP Client**: Axios with interceptors for JWT
- **Forms**: React Hook Form + Zod validation
- **Testing**: Jest + React Testing Library

---

## 📱 Required Views

### 1. Authentication Views
- **Login Page**: Email + password form, link to register
- **Register Page**: Username + email + password + confirm password
- **Protected Route Wrapper**: Redirect to login if no JWT

### 2. Public Pokemon Views
- **Pokemon List (Paginated)**:
  - Grid layout with Pokemon cards
  - Each card shows: sprite, name, category, mass, skills (tags)
  - Pagination controls (prev/next/page numbers)
  - Loading skeletons during fetch
  - Error state with retry button
- **Pokemon Detail**:
  - Hero section with large sprite
  - Stats visualization (bar chart or progress bars)
  - Narrative description (flavor text)
  - Evolutionary lineage (horizontal timeline)
  - "Sync to Local" button (if authenticated and not synced)

### 3. Protected Admin Views
- **My Synced Pokemon**: List of locally synced Pokemon
- **Edit Pokemon Form**:
  - Fields: `localizedName`, `geographicMetadata`, `internalClassificationTags` (multi-select/chips)
  - Real-time validation with Zod
  - Success/error toasts
  - Optimistic UI updates

---

## 🏗️ Architectural Requirements

### Component Organization
```text
src/
├── components/           # Reusable UI components
│   ├── ui/              # Button, Input, Card, Modal, Toast
│   └── layout/          # Header, Footer, Sidebar
├── features/            # Feature-based modules
│   ├── auth/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── types/
│   ├── pokemon-list/
│   ├── pokemon-detail/
│   └── pokemon-edit/
├── services/            # API client configuration
├── hooks/               # Shared custom hooks
├── types/               # Global TypeScript types
├── utils/               # Helper functions
└── App.tsx
```

### State Management Strategy
- **Server State**: React Query (caching, refetching, mutations)
- **Client State**: React Context (auth, theme)
- **Form State**: React Hook Form
- **URL State**: React Router params/searchParams

### Performance Requirements
- [ ] Lazy load routes with `React.lazy()`
- [ ] Memoize expensive computations with `useMemo`/`useCallback`
- [ ] Virtualize long lists (if >100 items)
- [ ] Image lazy loading
- [ ] No console warnings in production build

---

## 🎨 Design Requirements

### Responsiveness
- [ ] Mobile-first approach
- [ ] Breakpoints: sm (640px), md (768px), lg (1024px), xl (1280px)
- [ ] Touch-friendly buttons (min 44x44px)
- [ ] Readable typography (min 14px body text)

### User-Centric Design
- [ ] Clear visual hierarchy
- [ ] Consistent color palette (Pokemon-themed: red, white, blue accents)
- [ ] Loading states for all async operations
- [ ] Error boundaries with user-friendly messages
- [ ] Empty states with helpful CTAs
- [ ] Accessible (WCAG 2.1 AA): proper ARIA labels, keyboard navigation, contrast ratios

---

## 🧪 Testing Requirements

### Component Tests
- [ ] Render tests (does it show?)
- [ ] Interaction tests (click, type, submit)
- [ ] Async tests (loading, error, success states)
- [ ] Accessibility tests (jest-axe)

### Example Test
```typescript
describe('PokemonCard', () => {
  it('renders sprite, name, and skills', () => {
    render(<PokemonCard pokemon={mockPokemon} />);
    expect(screen.getByAltText('bulbasaur')).toBeInTheDocument();
    expect(screen.getByText('Grass')).toBeInTheDocument();
  });

  it('shows loading skeleton when isLoading', () => {
    render(<PokemonCard isLoading />);
    expect(screen.getByTestId('skeleton')).toBeInTheDocument();
  });
});
```

---

## 📦 Delivery Requirements
- [ ] `README.md` with:
  - Setup instructions (`npm install`, `npm run dev`)
  - Environment variables (`.env.example`)
  - Build for production (`npm run build`)
  - Run tests (`npm test`)
- [ ] Pre-populate with mock credentials for demo:
  - Username: `demo@bla.com`
  - Password: `Demo123!`
- [ ] No TypeScript errors (`npm run type-check` passes)
- [ ] No ESLint errors (`npm run lint` passes)

---

## Output Instructions
Generate the implementation in this order:
1. Project setup (Vite + TypeScript + Tailwind config)
2. Shared components (UI library)
3. Auth feature (login, register, protected routes)
4. Pokemon list feature (pagination, cards)
5. Pokemon detail feature (stats, evolution)
6. Pokemon edit feature (forms, validation)
7. API service layer (Axios + React Query hooks)
8. Tests for each feature

Include TypeScript interfaces for all API responses.
