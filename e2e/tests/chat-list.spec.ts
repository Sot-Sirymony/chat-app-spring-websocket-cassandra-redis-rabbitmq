import { test, expect } from './fixtures';

test.describe('Chat list', () => {
  test('C1.1 List rooms - page structure', async ({ adminPage: page }) => {
    await page.goto('/chat');
    await expect(page.getByRole('heading', { name: /available chat rooms/i })).toBeVisible();
    await expect(page.locator('table thead')).toContainText(/name|description|connected/i);
  });

  test('C1.3 Room row has Join link when rooms exist', async ({ adminPage: page }) => {
    await page.goto('/chat');
    const joinLinks = page.getByRole('link', { name: /join/i });
    const count = await joinLinks.count();
    if (count > 0) {
      await expect(joinLinks.first()).toBeVisible();
    }
  });

  test('C2.1 Open New Chat Room modal (admin)', async ({ adminPage: page }) => {
    await page.goto('/chat');
    await page.getByText('admin').click();
    await page.getByRole('link', { name: /new chat room/i }).click();
    await expect(page.locator('#newChatRoomModal')).toBeVisible();
    await expect(page.locator('#newChatroomName')).toBeVisible();
    await expect(page.locator('#newChatRoomDescription')).toBeVisible();
    await expect(page.locator('#newChatRoomClassification')).toBeVisible();
  });

  test('C2.2 Create room success', async ({ adminPage: page }) => {
    await page.goto('/chat');
    await page.getByText('admin').click();
    await page.getByRole('link', { name: /new chat room/i }).click();
    await page.locator('#newChatRoomModal').waitFor({ state: 'visible' });
    const name = `E2E Room ${Date.now()}`;
    await page.locator('#newChatroomName').fill(name);
    await page.locator('#newChatRoomDescription').fill('E2E test room');
    await page.locator('#btnCreateNewChatRoom').click();
    await expect(page.locator('#newChatRoomModal')).not.toBeVisible();
    await expect(page.getByText(name)).toBeVisible();
    const row = page.getByRole('row', { name: new RegExp(name) });
    await expect(row.getByRole('link', { name: /join/i })).toBeVisible();
  });

  test('C2.4 Create room - close without saving', async ({ adminPage: page }) => {
    await page.goto('/chat');
    const rowsBefore = await page.locator('table tbody tr').count();
    await page.getByText('admin').click();
    await page.getByRole('link', { name: /new chat room/i }).click();
    await page.locator('#newChatroomName').fill('Will not save');
    await page
      .locator('#newChatRoomModal')
      .getByRole('button', { name: 'Close' })
      .last()
      .click();
    await expect(page.locator('#newChatRoomModal')).not.toBeVisible();
    const rowsAfter = await page.locator('table tbody tr').count();
    expect(rowsAfter).toBe(rowsBefore);
  });

  test('C3.1 Join via link', async ({ adminPage: page }) => {
    await page.goto('/chat');
    const joinLink = page.getByRole('link', { name: /join/i }).first();
    const count = await joinLink.count();
    if (count === 0) {
      test.skip();
      return;
    }
    await joinLink.click();
    await expect(page).toHaveURL(/\/chatroom\/.+/);
  });
});
