# Cucumber BDD Testing Guide

## Overview

This project now includes **Cucumber** for Behavior-Driven Development (BDD) testing in both frontend and backend. Cucumber allows you to write tests in natural language (Gherkin) that are understandable by both technical and non-technical stakeholders.

## 📁 Project Structure

### Frontend (JavaScript/Node.js)
```
frontend/
├── cucumber.js                    # Cucumber configuration
├── features/
│   ├── authentication.feature     # Authentication scenarios
│   ├── events.feature            # Event management scenarios
│   ├── profile.feature           # Profile configuration scenarios
│   ├── step_definitions/
│   │   ├── authentication.steps.js
│   │   ├── events.steps.js
│   │   └── profile.steps.js
│   └── support/
│       └── world.js              # Cookie jar for session management
└── package.json                  # Cucumber scripts + cookie deps
```

### Backend (Java/Spring Boot)
```
backend/
├── pom.xml                       # Added Cucumber dependencies
└── src/test/
    ├── java/com/example/backend/
    │   ├── CucumberIntegrationTest.java        # Test runner
    │   └── cucumber/
    │       ├── CucumberSpringConfiguration.java # Spring context
    │       └── steps/
    │           ├── AuthenticationSteps.java
    │           ├── EventsSteps.java
    │           └── ProfileSteps.java
    └── resources/features/
        ├── authentication.feature
        ├── events.feature
        └── profile.feature
```

---

## 🚀 Running Cucumber Tests

### Frontend

**Run all Cucumber tests:**
```bash
cd frontend
npm run cucumber
```

**Run tests with HTML report:**
```bash
npm run cucumber:html
```

**Run both unit tests and Cucumber tests:**
```bash
npm run test:all
```

**Run specific feature file:**
```bash
npm run cucumber features/authentication.feature
```

**Output:** Test results will be displayed in the console, and HTML reports will be generated in `cucumber-report.html`.

### Backend

**Run all Cucumber tests with Maven:**
```bash
cd backend
mvn test -Dtest=CucumberIntegrationTest
```

**Run all tests (including Cucumber):**
```bash
mvn test
```

**Run specific feature (using tags):**
```bash
mvn test -Dcucumber.filter.tags="@authentication"
```

**Output:** 
- Console output with test results
- HTML report: `target/cucumber-reports/cucumber.html`
- JSON report: `target/cucumber-reports/cucumber.json`

---

## 📝 Writing Cucumber Tests

### Gherkin Syntax

Cucumber tests are written in **Gherkin**, a human-readable language:

```gherkin
Feature: User Authentication
  As a user
  I want to log in
  So that I can access my account

  Scenario: Successful login
    Given I am on the login page
    When I enter valid credentials
    And I click the login button
    Then I should see my dashboard
```

### Key Gherkin Keywords

- **Feature:** High-level description of a software feature
- **Scenario:** Specific example of how the feature behaves
- **Given:** Initial context (preconditions)
- **When:** Actions/events
- **Then:** Expected outcomes
- **And/But:** Additional steps
- **Background:** Steps that run before each scenario
- **Scenario Outline:** Template for multiple scenarios with examples

---

## 🔍 Example Features

### Authentication Feature

**Frontend** (`features/authentication.feature`):
```gherkin
Scenario: Successful user registration
  Given I am a new user
  When I register with email "test@example.com" and password "SecurePass123"
  Then I should receive a successful registration response
  And my account should be created
```

**Backend** (`src/test/resources/features/authentication.feature`):
```gherkin
Scenario: Successful user registration
  Given the authentication API is available
  When I send a POST request to "/auth/register" with:
    | email    | newuser@example.com |
    | password | SecurePass123       |
    | username | newuser             |
  Then the response status code should be 201
  And the response should contain a success message
```

### Event Management Feature

```gherkin
Scenario: Create a new event
  Given I am logged in as a user
  When I create an event with the following details:
    | title    | Morning Run   |
    | sport    | Running       |
    | location | Central Park  |
    | capacity | 10            |
  Then the event should be created successfully
  And I should see the event in my events list
```

---

## 🛠️ Configuration

### Frontend Configuration (`cucumber.js`)

```javascript
export default {
  default: {
    require: ['features/step_definitions/**/*.js', 'features/support/**/*.js'],
    format: ['progress', '@cucumber/pretty-formatter'],
    publishQuiet: true,
    parallel: 1,
  },
  html: {
    format: ['html:cucumber-report.html'],
  },
};
```

### Backend Configuration

**Maven Dependencies** (in `pom.xml`):
- `cucumber-java` (7.20.1)
- `cucumber-spring` (7.20.1)
- `cucumber-junit-platform-engine` (7.20.1)
- `junit-platform-suite`

**Test Runner** (`CucumberIntegrationTest.java`):
Uses JUnit Platform Suite with Cucumber engine to discover and run feature files.

---

## 🔐 Session Management

### Frontend Session Handling

The frontend tests use **tough-cookie** and **fetch-cookie** to maintain authentication sessions across requests. This is essential for testing authenticated endpoints.

**How it works:**
1. Each test scenario gets its own cookie jar (via `world.js`)
2. When a user logs in, the session cookie is automatically stored
3. All subsequent requests include the session cookie
4. Tests can interact with protected endpoints as an authenticated user

**Example:**
```javascript
// Login establishes a session
Given('I am logged in as a user', async function () {
  await this.makeApiRequest('POST', '/auth/register', userData);
  await this.makeApiRequest('POST', '/auth/login', credentials);
  // Session cookie is now stored in this.cookieJar
});

// Later requests automatically include the cookie
When('I join the event', async function () {
  await this.makeApiRequest('POST', `/events/${eventId}/join`, {});
  // This works because we're still authenticated
});
```

### Backend Session Handling

The backend tests use **Apache HttpClient5** with a `BasicCookieStore` (configured in `TestRestTemplateConfig.java`) to maintain sessions across Cucumber steps.

---

## 📊 Reports

### Frontend Reports

After running `npm run cucumber:html`, open:
```
frontend/cucumber-report.html
```

### Backend Reports

After running Maven tests, check:
```
backend/target/cucumber-reports/cucumber.html
```

---

## 🎯 Best Practices

### 1. Write Clear Scenarios
- Use business language, not technical jargon
- One scenario should test one behavior
- Keep scenarios independent

### 2. Reusable Step Definitions
- Make steps generic enough to reuse
- Use parameters to make steps flexible
- Avoid duplicating logic

### 3. Use Background for Common Setup
```gherkin
Background:
  Given I am logged in as a user
  And the API is available
```

### 4. Data Tables for Multiple Inputs
```gherkin
When I create an event with:
  | title    | Morning Run  |
  | sport    | Running      |
  | capacity | 10           |
```

### 5. Scenario Outlines for Test Data
```gherkin
Scenario Outline: Login with different credentials
  When I login with "<email>" and "<password>"
  Then I should see "<result>"
  
  Examples:
    | email           | password    | result  |
    | valid@test.com  | Pass123     | success |
    | invalid@test.com| Wrong       | error   |
```

---

## 🔗 Integration with Existing Tests

Cucumber tests **complement** existing unit tests:

- **Unit Tests (Vitest/JUnit):** Test individual components/functions
- **Cucumber BDD Tests:** Test complete user workflows and API interactions
- Both can run together: `npm run test:all` (frontend) or `mvn test` (backend)

---

## 🐛 Troubleshooting

### Frontend Issues

**Problem:** `Cannot find module` errors
```bash
npm install
```

**Problem:** Tests can't connect to backend
- Ensure backend is running on `http://localhost:8080`
- Or set `API_BASE_URL` environment variable

### Backend Issues

**Problem:** Cucumber dependencies not found
```bash
mvn clean install
```

**Problem:** Spring context fails to load
- Check that `@SpringBootTest` is properly configured
- Verify H2 database is available for tests

---

## 📚 Additional Resources

- [Cucumber.js Documentation](https://cucumber.io/docs/cucumber/)
- [Cucumber-JVM Documentation](https://cucumber.io/docs/cucumber/api/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [BDD Best Practices](https://cucumber.io/docs/bdd/)

---

## 🤝 Contributing

When adding new features:
1. Write the feature file in Gherkin
2. Implement step definitions
3. Run tests to ensure they pass
4. Update this documentation if needed

---

## 📝 Summary

You now have a complete BDD testing setup with Cucumber! This allows you to:

✅ Write tests in natural language (Gherkin)  
✅ Test both frontend and backend with the same approach  
✅ Generate human-readable reports  
✅ Improve collaboration between developers and stakeholders  
✅ Document system behavior through executable specifications  

**Happy Testing! 🎉**
