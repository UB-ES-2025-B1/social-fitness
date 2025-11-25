# Cucumber BDD Testing - Quick Start

This project uses **Cucumber** for Behavior-Driven Development (BDD) testing.

## 🚀 Quick Start

### Frontend Tests
```bash
cd frontend
npm run cucumber          # Run all BDD tests
npm run cucumber:html     # Generate HTML report
```

### Backend Tests
```bash
cd backend
mvn test -Dtest=CucumberIntegrationTest
```

## 📁 Test Locations

- **Frontend Features:** `frontend/features/*.feature`
- **Backend Features:** `backend/src/test/resources/features/*.feature`

## 📖 Full Documentation

See [CUCUMBER_GUIDE.md](./CUCUMBER_GUIDE.md) for complete setup, examples, and best practices.

## ✨ Features Tested

- ✅ User Authentication (register, login)
- ✅ Event Management (create, join, leave, search)
- ✅ Profile Configuration (sports preferences, skill levels)

## 🔗 Resources

- [Cucumber Documentation](https://cucumber.io/docs/)
- [Gherkin Syntax](https://cucumber.io/docs/gherkin/reference/)
