package br.com.jorgeacetozi.ebookChat.fileapproval.domain.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.InstantMessage;
import br.com.jorgeacetozi.ebookChat.metrics.ChatMetricsService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.service.ChatRoomService;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.model.FileTransferRequest;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.repository.FileTransferRequestRepository;

@Service
public class FileTransferRequestService {

	private final FileTransferRequestRepository repository;
	private final ChatRoomService chatRoomService;
	private final SimpMessagingTemplate messagingTemplate;
	private final AuditService auditService;
	private final ChatMetricsService chatMetricsService;

	@Autowired
	public FileTransferRequestService(FileTransferRequestRepository repository,
			ChatRoomService chatRoomService, SimpMessagingTemplate messagingTemplate,
			AuditService auditService, ChatMetricsService chatMetricsService) {
		this.repository = repository;
		this.chatRoomService = chatRoomService;
		this.messagingTemplate = messagingTemplate;
		this.auditService = auditService;
		this.chatMetricsService = chatMetricsService;
	}

	@Transactional
	public FileTransferRequest createPending(String requester, String recipient, String roomId, String contentText, String fileRef) {
		FileTransferRequest req = new FileTransferRequest();
		req.setId(UUID.randomUUID().toString());
		req.setRequester(requester);
		req.setRecipient(recipient);
		req.setRoomId(roomId);
		req.setContentText(contentText);
		req.setFileRef(fileRef);
		req.setStatus(FileTransferRequest.Status.PENDING.name());
		req.setRequestedAt(new Date());
		return repository.save(req);
	}

	public List<FileTransferRequest> findPending() {
		return repository.findByStatusOrderByRequestedAtDesc(FileTransferRequest.Status.PENDING.name());
	}

	public List<FileTransferRequest> findByRequester(String requester) {
		return repository.findByRequesterOrderByRequestedAtDesc(requester);
	}

	public FileTransferRequest findById(String id) {
		return repository.findOne(id);
	}

	@Transactional
	public void approve(String requestId, String approverUsername) {
		FileTransferRequest req = repository.findOne(requestId);
		if (req == null || !FileTransferRequest.Status.PENDING.name().equals(req.getStatus())) {
			return;
		}
		req.setStatus(FileTransferRequest.Status.APPROVED.name());
		req.setDecidedAt(new Date());
		req.setApprover(approverUsername);
		repository.save(req);
		if (req.getRequestedAt() != null && req.getDecidedAt() != null) {
			long latencyMs = req.getDecidedAt().getTime() - req.getRequestedAt().getTime();
			chatMetricsService.recordFileApproval(latencyMs);
		}
		auditService.logEvent(approverUsername, "FILE_APPROVE", requestId, "allow", null, "Approved request " + requestId);
		deliverContent(req);
		notifyRequester(req.getRequester(), "approved", req);
	}

	@Transactional
	public void reject(String requestId, String approverUsername) {
		FileTransferRequest req = repository.findOne(requestId);
		if (req == null || !FileTransferRequest.Status.PENDING.name().equals(req.getStatus())) {
			return;
		}
		req.setStatus(FileTransferRequest.Status.REJECTED.name());
		req.setDecidedAt(new Date());
		req.setApprover(approverUsername);
		repository.save(req);
		auditService.logEvent(approverUsername, "FILE_REJECT", requestId, "deny", null, "Rejected request " + requestId);
		notifyRequester(req.getRequester(), "rejected", req);
	}

	private void deliverContent(FileTransferRequest req) {
		if (req.getRoomId() == null) return;
		// Room may no longer exist (e.g. Redis cleared); skip delivery to avoid NPE but approval still succeeds
		if (chatRoomService.findById(req.getRoomId()) == null) return;
		String text = req.getContentText() != null ? req.getContentText() : "";
		if (req.getFileRef() != null && !req.getFileRef().isEmpty()) {
			text = text + " [Download attachment](/api/files/" + req.getFileRef() + "/download)";
		}
		if (text.isEmpty()) return;
		InstantMessage msg = new InstantMessage();
		msg.setFromUser(req.getRequester());
		msg.setToUser(req.getRecipient());
		msg.setText(text);
		msg.setChatRoomId(req.getRoomId());
		if (req.getRecipient() == null || req.getRecipient().isEmpty()) {
			chatRoomService.sendPublicMessage(msg);
		} else {
			chatRoomService.sendPrivateMessage(msg);
		}
	}

	private void notifyRequester(String username, String outcome, FileTransferRequest req) {
		messagingTemplate.convertAndSendToUser(username, "/queue/approval-result",
				java.util.Collections.singletonMap("requestId", req.getId()));
	}
}
