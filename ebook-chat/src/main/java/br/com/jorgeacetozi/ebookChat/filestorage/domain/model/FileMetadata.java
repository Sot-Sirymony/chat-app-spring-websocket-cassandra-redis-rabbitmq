package br.com.jorgeacetozi.ebookChat.filestorage.domain.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Metadata for files stored in MinIO (BR-3.3). id is the MinIO object id.
 */
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

	@Id
	private String id;
	private String uploader;
	private String filename;
	private String contentType;
	private Long sizeBytes;
	private String requestId;
	private Date createdAt;

	public FileMetadata() {}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getUploader() { return uploader; }
	public void setUploader(String uploader) { this.uploader = uploader; }
	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }
	public String getContentType() { return contentType; }
	public void setContentType(String contentType) { this.contentType = contentType; }
	public Long getSizeBytes() { return sizeBytes; }
	public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
	public String getRequestId() { return requestId; }
	public void setRequestId(String requestId) { this.requestId = requestId; }
	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
