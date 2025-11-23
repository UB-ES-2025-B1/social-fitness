# QA Report – Backend (Sprint 2)

## 🔍 Objective
Validate the **correct behavior of the backend server configuration**, database connection, and REST endpoints. Ensure that all tests pass in both local and CI/CD (GitHub Actions) environments.

## 🧪 Tests Executed

### Integration Tests (White Box - 2 tests)
**BackendApplicationTests.java**
- ✅ `contextLoads()` - Verifies that the Spring Boot context loads correctly
- ✅ `databaseIsReachable()` - Checks database connection (H2 in tests, PostgreSQL in production)

### Controller Tests (Black Box - 12 tests)

**AuthControllerTest.java** (4 tests)
- ✅ `register_shouldReturn201_whenPayloadValid` - POST /auth/register with valid data
- ✅ `register_shouldReturn400_whenMissingFields` - Validation of required fields
- ✅ `login_shouldReturn200_whenValidCredentialsFormat` - POST /auth/login with correct format
- ✅ `login_shouldReturn400_whenMissingFields` - Validation of required fields

**ConfigurarPerfilControllerTest.java** (3 tests)
- ✅ `whenSportsEmpty_shouldReturn400_withError` - Validation: cannot save profile without sports
- ✅ `whenSportsPresent_andUserExists_shouldSaveAndReturn200` - Save profile correctly
- ✅ `whenUserNotFound_shouldReturn404` - Error when user doesn't exist

**EventsControllerTest.java** (4 tests)
- ✅ `join_returns200_withMessage` - POST /events/{id}/join
- ✅ `leave_returns200_whenOk` - POST /events/{id}/leave successfully
- ✅ `leave_returns400_whenIllegalState` - Error when event cannot be left
- ✅ `create_returns201_whenValid` - POST /events with valid data

**BackendSecuritySmokeTest.java** (1 test)
- ✅ `root_shouldReturn404_whenNoMappingExists` - Verifies that root route returns 404

## 🔧 Test Configuration

### Test Environment
- **Database**: H2 in-memory (configured with `@AutoConfigureTestDatabase`)
- **Security**: Disabled with `@AutoConfigureMockMvc(addFilters = false)` for controller tests
- **CSRF**: Included with `.with(csrf())` in POST requests for Spring Security compatibility
- **Mocking**: Services mocked with `@MockBean` to isolate controller layer

### Fixes Applied (Sprint 2)
1. ✅ Added H2 dependency for tests without requiring PostgreSQL
2. ✅ Configured tests with `@AutoConfigureTestDatabase` to use H2
3. ✅ Added CSRF support in tests with `.with(csrf())`
4. ✅ Disabled security filters with `@AutoConfigureMockMvc(addFilters = false)`

## 📊 Results

**Total: 14 tests - 14 ✅ (100% pass rate)**
- White Box: 2 tests
- Black Box: 12 tests

## ✅ Conclusion
All backend tests **pass correctly** in both local and CI/CD environments. The test environment is fully **operational** with coverage of the main endpoints for authentication, profile, and events.

The backend test environment is **operational**.
