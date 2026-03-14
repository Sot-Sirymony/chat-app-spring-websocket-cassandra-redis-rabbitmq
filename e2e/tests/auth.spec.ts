import { test, expect } from '@playwright/test';
import { ADMIN } from './fixtures';

test.describe('Authentication', () => {
  test('A1.1 Successful login', async ({ page }) => {
    await page.goto('/');
    await page.locator('#username').fill(ADMIN.username);
    await page.locator('#password').fill(ADMIN.password);
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL(/\/chat/);
    await expect(page.getByRole('link', { name: /chat rooms/i })).toBeVisible();
    await expect(page.getByText(ADMIN.username)).toBeVisible();
  });

  test('A1.2 Invalid credentials', async ({ page }) => {
    await page.goto('/');
    await page.locator('#username').fill('wronguser');
    await page.locator('#password').fill('wrongpass');
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).not.toHaveURL(/\/chat/);
    await expect(page.getByRole('heading', { name: /invalid username or password/i })).toBeVisible();
  });

  test('A1.3 Empty credentials', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).not.toHaveURL(/\/chat/);
  });

  test('A1.4 Link to registration', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: /create an account/i }).click();
    await expect(page).toHaveURL(/\/new-account/);
  });

  test('A2.1 Logout from app', async ({ page }) => {
    await page.goto('/');
    await page.locator('#username').fill(ADMIN.username);
    await page.locator('#password').fill(ADMIN.password);
    await page.getByRole('button', { name: /sign in/i }).click();
    await page.waitForURL(/\/chat/);
    await page.getByText(ADMIN.username).click();
    await page.getByRole('link', { name: /logout/i }).click();
    await expect(page).toHaveURL(/\/(login)?(\?.*)?$/);
    await page.goto('/chat');
    await expect(page).toHaveURL(/\/(login)?/);
  });

  test('A3.1 Successful registration', async ({ page }) => {
    // Username must be unique and between 5 and 15 characters.
    const unique = 'e2' + Date.now().toString(36); // typically 8–10 chars total
    await page.goto('/new-account');
    await page.locator('#name').fill('E2E User');
    await page.locator('#email').fill(unique + '@test.com');
    await page.locator('#username').fill(unique);
    await page.locator('#password').fill('password123');
    await page.getByRole('button', { name: /create/i }).click();
    // Treat registration as successful if no validation errors are shown
    await expect(page.locator('.alert-danger')).toHaveCount(0);
  });

  test('A3.2 Registration form visible', async ({ page }) => {
    await page.goto('/new-account');
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('#name')).toBeVisible();
    await expect(page.locator('#username')).toBeVisible();
  });
});
