import { test, expect } from './fixtures';

test.describe('Layout and navigation', () => {
  test('N1.1 Chat Rooms link', async ({ adminPage: page }) => {
    await page.getByRole('link', { name: /chat rooms/i }).click();
    await expect(page).toHaveURL(/\/chat/);
  });

  test('N1.2 User dropdown visible when authenticated', async ({ adminPage: page }) => {
    await page.getByText('admin').click();
    await expect(page.getByRole('link', { name: /logout/i })).toBeVisible();
  });

  test('N1.3 Admin sees New Chat Room and analytics links', async ({ adminPage: page }) => {
    await page.getByText('admin').click();
    await expect(page.getByRole('link', { name: /new chat room/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /pending approvals/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /security analytics/i })).toBeVisible();
  });

  test('N1.4 Non-admin does not see New Chat Room or Security analytics', async ({ userPage: page }) => {
    await page.getByRole('button', { name: 'user' }).click();
    await expect(page.getByRole('link', { name: /new chat room/i })).not.toBeVisible();
    await expect(page.getByRole('link', { name: /security analytics/i })).not.toBeVisible();
    await expect(page.getByRole('link', { name: /my approval requests/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /logout/i })).toBeVisible();
  });

  test('N2.1 Unauthenticated access to /chat redirects to login', async ({ page }) => {
    await page.goto('/chat');
    await expect(page).toHaveURL(/\/(login)?/);
  });

  test('N2.2 Unauthenticated access to /chatroom redirects to login', async ({ page }) => {
    await page.goto('/chatroom/some-id');
    await expect(page).toHaveURL(/\/(login)?/);
  });

  test('N2.3 Non-admin cannot access analytics', async ({ userPage: page }) => {
    await page.goto('/analytics');
    // Non-admin should not see the analytics heading
    await expect(page.getByRole('heading', { name: /security analytics/i })).not.toBeVisible();
  });

  test('N2.4 Admin can open analytics', async ({ adminPage: page }) => {
    await page.getByText('admin').click();
    await page.getByRole('link', { name: /security analytics/i }).click();
    await expect(page).toHaveURL(/\/analytics/);
    await expect(page.getByRole('heading', { name: /security analytics/i })).toBeVisible();
  });
});
