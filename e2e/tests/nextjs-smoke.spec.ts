import { test, expect } from '@playwright/test';

const ADMIN = { username: 'admin', password: 'admin' };

test.describe('Next.js app smoke', () => {
  test('login → chat list → join room → send message', async ({ page }) => {
    await page.goto('/');
    await page.getByLabel(/username/i).fill(ADMIN.username);
    await page.getByLabel(/password/i).fill(ADMIN.password);
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL(/\/chat/, { timeout: 15000 });

    const joinLink = page.getByRole('link', { name: /join/i }).first();
    const joinCount = await joinLink.count();
    if (joinCount === 0) {
      test.skip(true, 'No room to join');
      return;
    }
    await joinLink.click();
    await expect(page).toHaveURL(/\/chatroom\/.+/);

    const messageInput = page.getByPlaceholder(/type your message/i);
    await expect(messageInput).toBeVisible({ timeout: 15000 });
    await messageInput.fill('E2E smoke test message');
    await page.getByRole('button', { name: /send/i }).click();

    await expect(page.getByText('E2E smoke test message')).toBeVisible({ timeout: 5000 });
  });
});
