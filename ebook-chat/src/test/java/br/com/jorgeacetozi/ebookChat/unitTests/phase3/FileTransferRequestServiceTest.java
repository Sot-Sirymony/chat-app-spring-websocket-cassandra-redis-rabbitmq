package br.com.jorgeacetozi.ebookChat.unitTests.phase3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.service.ChatRoomService;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.model.FileTransferRequest;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.repository.FileTransferRequestRepository;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.service.FileTransferRequestService;
import br.com.jorgeacetozi.ebookChat.metrics.ChatMetricsService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Phase 3 (BR-3.1): Test cases for file approval workflow — T3.1.1–T3.1.6.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileTransferRequestServiceTest {

	@Mock
	private FileTransferRequestRepository repository;

	@Mock
	private ChatRoomService chatRoomService;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private AuditService auditService;

	@Mock
	private ChatMetricsService chatMetricsService;

	@InjectMocks
	private FileTransferRequestService fileTransferRequestService;

	@Test
	public void shouldCreatePendingRequestWithRequesterRecipientRoomAndFileRef() {
		when(repository.save(any(FileTransferRequest.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
		FileTransferRequest req = fileTransferRequestService.createPending("alice", "bob", "room-1", "Please approve", "file-id-123");
		assertNotNull(req);
		assertNotNull(req.getId());
		assertEquals("alice", req.getRequester());
		assertEquals("bob", req.getRecipient());
		assertEquals("room-1", req.getRoomId());
		assertEquals("file-id-123", req.getFileRef());
		assertEquals(FileTransferRequest.Status.PENDING.name(), req.getStatus());
		assertNotNull(req.getRequestedAt());
		verify(repository).save(req);
	}

	@Test
	public void shouldFindPendingRequests() {
		List<FileTransferRequest> pending = new ArrayList<>();
		when(repository.findByStatusOrderByRequestedAtDesc(FileTransferRequest.Status.PENDING.name())).thenReturn(pending);
		List<FileTransferRequest> result = fileTransferRequestService.findPending();
		assertEquals(pending, result);
		verify(repository).findByStatusOrderByRequestedAtDesc(FileTransferRequest.Status.PENDING.name());
	}

	@Test
	public void shouldApprovePendingRequestAndUpdateStatusAndAudit() {
		FileTransferRequest req = new FileTransferRequest();
		req.setId("req-1");
		req.setStatus(FileTransferRequest.Status.PENDING.name());
		req.setRequestedAt(new Date(System.currentTimeMillis() - 10000));
		when(repository.findOne("req-1")).thenReturn(req);
		when(repository.save(any(FileTransferRequest.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

		fileTransferRequestService.approve("req-1", "admin");

		ArgumentCaptor<FileTransferRequest> captor = ArgumentCaptor.forClass(FileTransferRequest.class);
		verify(repository).save(captor.capture());
		assertEquals(FileTransferRequest.Status.APPROVED.name(), captor.getValue().getStatus());
		assertEquals("admin", captor.getValue().getApprover());
		assertNotNull(captor.getValue().getDecidedAt());
		verify(auditService).logEvent(eq("admin"), eq("FILE_APPROVE"), eq("req-1"), eq("allow"), eq(null), any(String.class));
		verify(chatMetricsService).recordFileApproval(any(Long.class));
	}

	@Test
	public void shouldNotApproveWhenRequestNotFound() {
		when(repository.findOne("nonexistent")).thenReturn(null);
		fileTransferRequestService.approve("nonexistent", "admin");
		verify(repository).findOne("nonexistent");
		verify(auditService, org.mockito.Mockito.never()).logEvent(any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void shouldRejectPendingRequestAndAudit() {
		FileTransferRequest req = new FileTransferRequest();
		req.setId("req-2");
		req.setStatus(FileTransferRequest.Status.PENDING.name());
		req.setRequester("alice");
		when(repository.findOne("req-2")).thenReturn(req);
		when(repository.save(any(FileTransferRequest.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

		fileTransferRequestService.reject("req-2", "admin");

		ArgumentCaptor<FileTransferRequest> captor = ArgumentCaptor.forClass(FileTransferRequest.class);
		verify(repository).save(captor.capture());
		assertEquals(FileTransferRequest.Status.REJECTED.name(), captor.getValue().getStatus());
		verify(auditService).logEvent(eq("admin"), eq("FILE_REJECT"), eq("req-2"), eq("deny"), eq(null), any(String.class));
	}
}
