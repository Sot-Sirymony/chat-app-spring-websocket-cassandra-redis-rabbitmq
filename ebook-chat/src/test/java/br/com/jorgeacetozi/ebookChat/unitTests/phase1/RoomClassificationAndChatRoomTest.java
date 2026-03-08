package br.com.jorgeacetozi.ebookChat.unitTests.phase1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.ChatRoom;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.RoomClassification;

/**
 * Phase 1 (BR-2.2) T2.2.1–T2.2.2: Room classification and allowed roles/departments.
 */
public class RoomClassificationAndChatRoomTest {

	@Test
	public void shouldHaveAllClassificationLevels() {
		assertEquals(4, RoomClassification.values().length);
		assertEquals(RoomClassification.PUBLIC, RoomClassification.valueOf("PUBLIC"));
		assertEquals(RoomClassification.RESTRICTED, RoomClassification.valueOf("RESTRICTED"));
	}

	@Test
	public void shouldAllowSettingClassificationOnChatRoom() {
		ChatRoom room = new ChatRoom("1", "Test", "Desc");
		room.setClassification(RoomClassification.CONFIDENTIAL);
		assertNotNull(room.getClassification());
		assertEquals(RoomClassification.CONFIDENTIAL, room.getClassification());
	}

	@Test
	public void shouldAllowSettingAllowedDepartments() {
		ChatRoom room = new ChatRoom("1", "Restricted Room", "Desc");
		room.setClassification(RoomClassification.RESTRICTED);
		room.setAllowedDepartments("Engineering,Legal");
		assertNotNull(room.getAllowedDepartments());
		assertEquals("Engineering,Legal", room.getAllowedDepartments());
	}
}
