package br.com.jorgeacetozi.ebookChat.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO configuration (BR-3.3). Bound from application.yml under ebook.chat.minio.
 */
@ConfigurationProperties(prefix = "ebook.chat.minio")
public class MinioProperties {

	private String endpoint = "http://localhost:9000";
	private String bucket = "ebook-chat-files";
	private String accessKey = "minioadmin";
	private String secretKey = "minioadmin";

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
