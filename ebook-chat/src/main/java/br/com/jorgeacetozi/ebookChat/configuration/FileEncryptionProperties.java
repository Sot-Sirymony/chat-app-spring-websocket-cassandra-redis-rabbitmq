package br.com.jorgeacetozi.ebookChat.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * T7.1.2: File encryption at rest. Key from secret manager / env in production.
 */
@ConfigurationProperties(prefix = "ebook.chat.file-encryption")
public class FileEncryptionProperties {

	private boolean enabled = false;
	private String keyBase64 = "";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getKeyBase64() {
		return keyBase64;
	}

	public void setKeyBase64(String keyBase64) {
		this.keyBase64 = keyBase64;
	}
}
