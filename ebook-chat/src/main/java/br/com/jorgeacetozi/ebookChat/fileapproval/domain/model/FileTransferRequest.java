package br.com.jorgeacetozi.ebookChat.fileapproval.domain.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * BR-3.1: Workflow-based file/message approval request.
 */
@Entity
@Table(name = "file_transfer_request")
public class FileTransferRequest {

	public enum Status { PENDING, APPROVED, REJECTED }

	@Id
	private String id;
	private String requester;
	private String recipient;
	private String roomId;
	private String fileRef;
	@Lob
	@Column(columnDefinition = "TEXT")
	private String contentText;
	private String status;
	private Date requestedAt;
	private Date decidedAt;
	private String approver;

	public FileTransferRequest() {}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getRequester() { return requester; }
	public void setRequester(String requester) { this.requester = requester; }
	public String getRecipient() { return recipient; }
	public void setRecipient(String recipient) { this.recipient = recipient; }
	public String getRoomId() { return roomId; }
	public void setRoomId(String roomId) { this.roomId = roomId; }
	public String getFileRef() { return fileRef; }
	public void setFileRef(String fileRef) { this.fileRef = fileRef; }
	public String getContentText() { return contentText; }
	public void setContentText(String contentText) { this.contentText = contentText; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Date getRequestedAt() { return requestedAt; }
	public void setRequestedAt(Date requestedAt) { this.requestedAt = requestedAt; }
	public Date getDecidedAt() { return decidedAt; }
	public void setDecidedAt(Date decidedAt) { this.decidedAt = decidedAt; }
	public String getApprover() { return approver; }
	public void setApprover(String approver) { this.approver = approver; }
}
