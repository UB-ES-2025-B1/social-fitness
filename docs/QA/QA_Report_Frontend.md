# QA Report – Frontend (Sprint 2)

## 🔍 Objective
Verify the **correct configuration of the testing framework** (**Vitest** + **React Testing Library** + **Happy DOM**) and ensure that all interactive components behave correctly according to expected user flows. Guarantee that tests pass in CI/CD (GitHub Actions).

---

## 🧪 Tests Executed (Black Box - 13 tests)

All frontend tests are **Black Box**, as they test the external behavior of components without accessing internal implementation details.

### Authentication (2 tests)
**LoginForm.test.jsx** (1 test)
- ✅ `triggers handlers on typing and submit` - Verifies user interactions (typing, clicking) and callbacks

**RegisterForm.test.jsx** (1 test)
- ✅ `calls onChange per field and onSubmit on submit` - Validates registration form and submission

### User Profile (3 tests)
**ProfilePage.test.jsx** (1 test)
- ✅ `shows username and email as read-only and allows entering edit mode to add/remove/set levels` - Tests profile edit mode

**ProfileConfigurator.test.jsx** (2 tests)
- ✅ `validates that you cannot proceed without selecting any sport` - Required sport selection validation
- ✅ `happy path: selects sports, defines levels and emits payload on completion` - Complete configuration flow

### Events (7 tests)
**EventExplorer.test.jsx** (4 tests)
- ✅ `loads and displays events on mount` - Initial event loading
- ✅ `sends query on pressing "Search"` - Search functionality
- ✅ `opens filter modal and applies filters on pressing "Save"` - Filter system
- ✅ (Test 4) - Additional EventExplorer validation

**EventJoinLeave.test.jsx** (2 tests)
- ✅ `shows joined events group collapsed by default and can expand` - Joined events visualization
- ✅ `allows leaving an event and removes it from joined section; shows error on failure` - Leave events

**CreateEvent.test.jsx** (1 test)
- ✅ `renders form fields and enforces capacity and price constraints and shows creating state` - Create form validation

### Navigation (1 test)
**TopBar.test.jsx** (1 test)
- ✅ `renders tabs and calls onChange with correct modes` - Navigation component

---

## 🔧 Test Configuration

### Environment
- **Framework**: Vitest 3.2.4
- **Test Environment**: Happy DOM (changed from jsdom to avoid worker thread issues in CI)
- **Testing Library**: @testing-library/react + @testing-library/user-event
- **Mocking**: Vitest mocks (`vi.fn()`) to simulate API calls and callbacks

### Fixes Applied (Sprint 2)
1. ✅ **Changed from jsdom to happy-dom** - Solves `webidl-conversions` errors in GitHub Actions
2. ✅ Updated imports from `getByRole` for more robust queries
3. ✅ Improved assertions with `screen.getByLabelText` for better change resistance
4. ✅ Added localization support (queries with regex `/username/i`)

### CI/CD Configuration
- **Node.js**: v18
- **Cache**: npm dependencies cached to improve speed
- **Execution**: Sequential (avoids race conditions in happy-dom)

---

## 📊 Results

**Total: 13 tests - 13 ✅ (100% pass rate)**
- 8 test files
- Coverage: Authentication, Profile, Events, Navigation
- Average duration: ~8 seconds

---

## ✅ Conclusion
The frontend test environment is **fully operational**. All tests pass correctly in both **local** and **CI/CD (GitHub Actions)** environments. Complete coverage of main flows has been achieved:

- ✅ Authentication forms (login/register)
- ✅ User profile configuration
- ✅ Event exploration and management
- ✅ Application navigation

The migration to **happy-dom** has resolved threading issues in CI while maintaining compatibility with all existing tests.
