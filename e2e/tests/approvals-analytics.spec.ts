import { test, expect } from './fixtures';

test.describe('Approvals page', () => {
  test('P1.1 Admin sees Pending approval requests section', async ({ adminPage: page }) => {
    await page.goto('/approvals');
    await expect(page.getByText(/pending approval requests/i)).toBeVisible();
    await expect(page.locator('#pendingBody')).toBeVisible();
  });

  test('P2.1 My approval requests section visible', async ({ adminPage: page }) => {
    await page.goto('/approvals');
    await expect(page.getByRole('heading', { name: /my approval requests/i })).toBeVisible();
    await expect(page.locator('#myRequestsBody')).toBeVisible();
  });

  test('P2.1 User sees only My approval requests', async ({ userPage: page }) => {
    await page.goto('/approvals');
    await expect(page.getByRole('heading', { name: /my approval requests/i })).toBeVisible();
    await expect(page.locator('#myRequestsBody')).toBeVisible();
  });
});

test.describe('Analytics page', () => {
  test('X1.1 Admin sees risky users and risky rooms tables', async ({ adminPage: page }) => {
    await page.goto('/analytics');
    await expect(page.getByRole('heading', { name: /security analytics/i })).toBeVisible();
    await expect(page.locator('#riskyUsersBody')).toBeVisible();
    await expect(page.locator('#riskyRoomsBody')).toBeVisible();
  });
});
