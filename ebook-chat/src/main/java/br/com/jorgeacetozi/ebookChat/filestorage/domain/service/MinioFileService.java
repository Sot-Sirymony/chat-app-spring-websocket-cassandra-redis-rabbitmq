package br.com.jorgeacetozi.ebookChat.filestorage.domain.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.configuration.MinioProperties;
import br.com.jorgeacetozi.ebookChat.filestorage.domain.model.FileMetadata;
import br.com.jorgeacetozi.ebookChat.filestorage.domain.repository.FileMetadataRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

/**
 * MinIO file storage (BR-3.3). T7.1.2: optional AES encryption at rest.
 */
@Service
public class MinioFileService {

	private final MinioClient minioClient;
	private final String bucket;
	private final FileMetadataRepository fileMetadataRepository;
	private final FileEncryptionService fileEncryptionService;

	@Autowired
	public MinioFileService(MinioProperties properties, FileMetadataRepository fileMetadataRepository,
			FileEncryptionService fileEncryptionService) throws Exception {
		this.fileMetadataRepository = fileMetadataRepository;
		this.fileEncryptionService = fileEncryptionService;
		this.bucket = properties.getBucket();
		this.minioClient = MinioClient.builder()
				.endpoint(properties.getEndpoint())
				.credentials(properties.getAccessKey(), properties.getSecretKey())
				.build();
		ensureBucket();
	}

	private void ensureBucket() {
		try {
			if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucket).build())) {
				minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucket).build());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to ensure MinIO bucket: " + bucket, e);
		}
	}

	/**
	 * Upload bytes to MinIO and save metadata. If encryption enabled, content is encrypted at rest (T7.1.2).
	 */
	public String upload(String uploader, String filename, String contentType, byte[] content) {
		String objectId = UUID.randomUUID().toString();
		long originalSize = content == null ? 0L : (long) content.length;
		byte[] toStore = content;
		if (fileEncryptionService.isEnabled() && content != null && content.length > 0) {
			try {
				toStore = fileEncryptionService.encrypt(content);
			} catch (Exception e) {
				throw new RuntimeException("Failed to encrypt file", e);
			}
		}
		try {
			minioClient.putObject(PutObjectArgs.builder()
					.bucket(bucket)
					.object(objectId)
					.stream(new ByteArrayInputStream(toStore), toStore.length, -1)
					.contentType(contentType != null ? contentType : "application/octet-stream")
					.build());
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload file to MinIO", e);
		}
		FileMetadata meta = new FileMetadata();
		meta.setId(objectId);
		meta.setUploader(uploader);
		meta.setFilename(filename);
		meta.setContentType(contentType);
		meta.setSizeBytes(originalSize);
		meta.setCreatedAt(new Date());
		fileMetadataRepository.save(meta);
		return objectId;
	}

	/**
	 * Get file content by id. If encryption enabled, content is decrypted (T7.1.2).
	 */
	public InputStream getContent(String objectId) throws Exception {
		InputStream raw = minioClient.getObject(GetObjectArgs.builder()
				.bucket(bucket)
				.object(objectId)
				.build());
		if (!fileEncryptionService.isEnabled()) {
			return raw;
		}
		byte[] encrypted = readAll(raw);
		byte[] decrypted = fileEncryptionService.decrypt(encrypted);
		return new ByteArrayInputStream(decrypted);
	}

	private static byte[] readAll(InputStream in) throws java.io.IOException {
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
		return out.toByteArray();
	}

	public FileMetadata getMetadata(String objectId) {
		return fileMetadataRepository.findOne(objectId);
	}

	/**
	 * Link this file to a file transfer request (for approval-gated download).
	 */
	public void linkToRequest(String objectId, String requestId) {
		FileMetadata meta = fileMetadataRepository.findOne(objectId);
		if (meta != null) {
			meta.setRequestId(requestId);
			fileMetadataRepository.save(meta);
		}
	}
}
