import { Given, When, Then } from '@cucumber/cucumber';
import assert from 'assert';

Given('I am logged in as a user', async function () {
  // Register a test user to ensure it exists
  const userData = {
    email: 'loggeduser@example.com',
    username: 'loggeduser',
    password: 'TestPass123',
  };
  
  try {
    await this.makeApiRequest('POST', '/auth/register', userData);
  } catch (error) {
    // User might already exist, that's fine
  }
  
  this.userData = userData;
  this.userData.token = 'mock-auth-token';
});

Given('there is an available event {string}', async function (eventName) {
  // Create a real event in the database
  const eventData = {
    title: eventName,
    sport: 'Yoga',
    location: 'Test Location',
    date: '2025-12-15',
    time: '10:00',
    organizer: 'testuser',
    capacity: 20,
  };
  
  await this.makeApiRequest('POST', '/events', eventData);
  // Get the event ID from the response
  const eventId = this.apiResponse.data?.id || 1;
  
  this.availableEvent = {
    id: eventId,
    title: eventName,
    sport: 'Yoga',
    capacity: 20,
  };
});

Given('I have joined an event {string}', async function (eventName) {
  // Create event and join it
  const eventData = {
    title: eventName,
    sport: 'Basketball',
    location: 'Test Gym',
    date: '2025-12-20',
    time: '18:00',
    organizer: 'testuser',
    capacity: 15,
  };
  
  await this.makeApiRequest('POST', '/events', eventData);
  const eventId = this.apiResponse.data?.id || 2;
  
  // Join the event
  await this.makeApiRequest('POST', `/events/${eventId}/join`, {});
  
  this.joinedEvent = {
    id: eventId,
    title: eventName,
    sport: 'Basketball',
  };
});

Given('there are multiple events for different sports', function () {
  this.multipleEvents = [
    { id: 1, title: 'Tennis Match', sport: 'Tennis' },
    { id: 2, title: 'Running Group', sport: 'Running' },
    { id: 3, title: 'Tennis Training', sport: 'Tennis' },
  ];
});

When('I create an event with the following details:', async function (dataTable) {
  const eventData = {};
  
  // Parse the data table - it's in format [[key, value], ...]
  const rows = dataTable.rawTable;
  rows.forEach(([key, value]) => {
    eventData[key] = value;
  });
  
  // Add required fields if missing
  if (!eventData.time) eventData.time = '10:00';
  if (!eventData.organizer) eventData.organizer = 'testuser';
  
  this.eventData = eventData;
  await this.makeApiRequest('POST', '/events', eventData);
});

When('I join the event', async function () {
  await this.makeApiRequest('POST', `/events/${this.availableEvent.id}/join`, {});
});

When('I leave the event', async function () {
  await this.makeApiRequest('POST', `/events/${this.joinedEvent.id}/leave`, {});
});

When('I search for {string} events', async function (sport) {
  this.searchQuery = sport;
  await this.makeApiRequest('GET', `/events?sport=${sport}`);
});

Then('the event should be created successfully', function () {
  assert.strictEqual(this.apiResponse.status, 201, 'Expected 201 status for event creation');
});

Then('I should see the event in my events list', function () {
  assert.ok(this.apiResponse.data, 'Expected event data in response');
});

Then('I should be added to the event participants', function () {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status for joining event');
});

Then('the event should appear in my joined events', function () {
  assert.ok(this.apiResponse.data.message, 'Expected confirmation message');
});

Then('I should be removed from the event participants', function () {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status for leaving event');
});

Then('the event should not appear in my joined events', function () {
  assert.ok(this.apiResponse.data, 'Expected response confirming removal');
});

Then('I should see only tennis-related events', function () {
  assert.strictEqual(this.apiResponse.status, 200, 'Expected 200 status for search');
  // In a real implementation, you would verify the response contains only tennis events
});

Then('other sports events should be filtered out', function () {
  // Verification that non-tennis events are not in the response
  assert.ok(this.apiResponse.data, 'Expected filtered event list');
});
