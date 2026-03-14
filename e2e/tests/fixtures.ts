import { test as base } from '@playwright/test';

export const ADMIN = { username: 'admin', password: 'admin' };
export const USER = { username: 'user', password: 'password' };

async function login(page: import('@playwright/test').Page, credentials: { username: string; password: string }) {
  await page.goto('/');
  await page.locator('#username').fill(credentials.username);
  await page.locator('#password').fill(credentials.password);
  await page.getByRole('button', { name: /sign in/i }).click();
  await page.waitForURL(/\/chat/, { timeout: 15000 });
}

export const test = base.extend<{ adminPage: import('@playwright/test').Page; userPage: import('@playwright/test').Page }>({
  adminPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await login(page, ADMIN);
    await use(page);
    await context.close();
  },
  userPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await login(page, USER);
    await use(page);
    await context.close();
  },
});

export { expect } from '@playwright/test';
