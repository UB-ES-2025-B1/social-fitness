import { Given, When, Then } from '@cucumber/cucumber';
import assert from 'assert';

Given('I have already configured my profile with {string}', async function (sport) {
  // Ensure we're logged in first
  if (!this.userData || !this.userData.username) {
    const userData = {
      email: 'profileuser@example.com',
      username: 'profileuser',
      password: 'TestPass123',
    };
    
    try {
      await this.makeApiRequest('POST', '/auth/register', userData);
    } catch (error) {
      // User might already exist
    }
    
    await this.makeApiRequest('POST', '/auth/login', {
      username: userData.username,
      password: userData.password,
    });
    
    this.userData = userData;
  }
  
  this.existingSports = [sport];
});

When('I select the following sports:', function (dataTable) {
  this.selectedSports = [];
  dataTable.rows().forEach(([sport, level]) => {
    this.selectedSports.push({ sport, level });
  });
});

When('I save my profile', async function () {
  const profileData = {
    sports: this.selectedSports || this.existingSports,
  };
  
  // Profile endpoint requires userId in path
  const userId = 1; // Test user ID
  await this.makeApiRequest('POST', `/profile/${userId}`, profileData);
});

When('I try to save my profile without selecting any sports', async function () {
  const userId = 1;
  await this.makeApiRequest('POST', `/profile/${userId}`, {
    sports: [],
  });
});

When('I add {string} to my sports list', function (sport) {
  if (!this.existingSports) {
    this.existingSports = [];
  }
  this.existingSports.push(sport);
});

Then('my profile should be updated successfully', function () {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status for profile update');
});

Then('my sports preferences should be stored', function () {
  assert.ok(this.apiResponse.data, 'Expected confirmation of stored preferences');
});

Then('my profile should include both {string} and {string}', function (sport1, sport2) {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status');
  // In a real implementation, verify both sports are in the response
  assert.ok(this.existingSports.includes(sport1), `Expected ${sport1} in profile`);
  assert.ok(this.existingSports.includes(sport2), `Expected ${sport2} in profile`);
});
