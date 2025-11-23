import { setWorldConstructor, World } from '@cucumber/cucumber';
import { CookieJar } from 'tough-cookie';
import fetchCookie from 'fetch-cookie';

class CustomWorld extends World {
  constructor(options) {
    super(options);
    this.apiResponse = null;
    this.apiError = null;
    this.userData = {};
    
    // Set up cookie jar so we can stay logged in between requests
    this.cookieJar = new CookieJar();
    this.fetch = fetchCookie(fetch, this.cookieJar);
  }

  async makeApiRequest(method, endpoint, data = null) {
    const baseUrl = process.env.API_BASE_URL || 'http://localhost:8080';
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    };

    if (data) {
      options.body = JSON.stringify(data);
    }

    try {
      const response = await this.fetch(`${baseUrl}${endpoint}`, options);
      const responseData = await response.json().catch(() => ({}));
      
      // Show errors in the console to help with debugging
      if (response.status >= 400 && responseData.message) {
        console.log(`API Error (${response.status}): ${responseData.message}`, responseData.errors || '');
      }
      
      this.apiResponse = {
        status: response.status,
        data: responseData,
      };
    } catch (error) {
      this.apiError = error;
      console.log('Request failed:', error.message);
    }
  }
}

setWorldConstructor(CustomWorld);
