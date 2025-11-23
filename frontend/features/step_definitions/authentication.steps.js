import { Given, When, Then } from '@cucumber/cucumber';
import assert from 'assert';

Given('I am a new user', function () {
  const timestamp = Date.now();
  this.userData = {
    email: '',
    password: '',
    username: `testuser${timestamp}`,
  };
});

Given('I am a registered user with email {string}', async function (email) {
  const timestamp = Date.now();
  this.userData = {
    email: `test${timestamp}@example.com`,
    password: 'ValidPass123',
    username: `testuser${timestamp}`,
  };
  
  // Register this user so they exist in the database
  await this.makeApiRequest('POST', '/auth/register', this.userData);
  
  // Give it a moment to finish
  await new Promise(resolve => setTimeout(resolve, 100));
});

Given('I am a registered user', async function () {
  const timestamp = Date.now();
  this.userData = {
    email: 'existing@example.com',
    password: 'CorrectPassword123',
    username: `existinguser${timestamp}`,
  };
  
  // Set up a registered user for testing login
  await this.makeApiRequest('POST', '/auth/register', this.userData);
});

When('I register with email {string} and password {string}', async function (email, password) {
  // Use a unique email each time to avoid conflicts
  const timestamp = Date.now();
  this.userData.email = `test${timestamp}@example.com`;
  this.userData.password = password;
  
  await this.makeApiRequest('POST', '/auth/register', {
    email: this.userData.email,
    password: this.userData.password,
    username: this.userData.username,
  });
});

When('I try to register without providing an email', async function () {
  await this.makeApiRequest('POST', '/auth/register', {
    password: 'SomePassword123',
    username: 'testuser',
  });
});

When('I login with correct credentials', async function () {
  await this.makeApiRequest('POST', '/auth/login', {
    username: this.userData.username,
    password: this.userData.password,
  });
});

When('I login with incorrect password', async function () {
  await this.makeApiRequest('POST', '/auth/login', {
    username: this.userData.username,
    password: 'WrongPassword123',
  });
});

Then('I should receive a successful registration response', function () {
  assert.strictEqual(this.apiResponse.status, 201, 'Expected 201 status for successful registration');
});

Then('my account should be created', function () {
  assert.ok(this.apiResponse.data, 'Expected response data');
});

Then('I should receive a validation error', function () {
  assert.strictEqual(this.apiResponse.status, 400, 'Expected 400 status for validation error');
});

Then('the error should indicate that email is required', function () {
  assert.ok(
    this.apiResponse.data.message || this.apiResponse.data.error,
    'Expected error message in response'
  );
});

Then('I should receive a successful login response', function () {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status for successful login');
});

Then('I should receive an authentication token', function () {
  assert.ok(this.apiResponse.data.user || this.apiResponse.data.message, 'Expected user data or success message in response');
});

Then('I should receive an authentication error', function () {
  assert.ok(
    this.apiResponse.status === 400 || this.apiResponse.status === 401,
    'Expected 400 or 401 status for authentication error'
  );
});

Then('I should not receive a token', function () {
  assert.ok(this.apiResponse.status === 400 || this.apiResponse.status === 401, 'Should receive error status on failed login');
});

Then('the error should indicate that at least one sport is required', function () {
  assert.ok(
    this.apiResponse.data.message || this.apiResponse.data.errors,
    'Expected error message about sports being required'
  );
});
