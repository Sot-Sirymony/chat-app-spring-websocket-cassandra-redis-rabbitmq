package br.com.jorgeacetozi.ebookChat.filestorage.domain.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

/**
 * T7.1.2: AES-256-GCM encryption for file content at rest. Key from config/secret manager.
 */
@Service
public class FileEncryptionService {

	private static final String ALG = "AES/GCM/NoPadding";
	private static final int GCM_TAG_LEN = 128;
	private static final int GCM_IV_LEN = 12;

	private final byte[] keyBytes;
	private final boolean enabled;

	public FileEncryptionService(br.com.jorgeacetozi.ebookChat.configuration.FileEncryptionProperties properties) {
		String key = properties != null ? properties.getKeyBase64() : null;
		boolean hasKey = key != null && !key.trim().isEmpty();
		this.enabled = properties != null && properties.isEnabled() && hasKey;
		if (enabled) {
			this.keyBytes = Base64.getDecoder().decode(key.trim());
			if (keyBytes == null || keyBytes.length != 32) {
				throw new IllegalArgumentException("ebook.chat.file-encryption.key-base64 must be base64 of 32 bytes (AES-256) when enabled");
			}
		} else {
			this.keyBytes = null;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public byte[] encrypt(byte[] plain) throws Exception {
		if (!enabled || plain == null) return plain;
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		byte[] iv = new byte[GCM_IV_LEN];
		new SecureRandom().nextBytes(iv);
		Cipher c = Cipher.getInstance(ALG);
		c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
		byte[] cipher = c.doFinal(plain);
		byte[] out = new byte[iv.length + cipher.length];
		System.arraycopy(iv, 0, out, 0, iv.length);
		System.arraycopy(cipher, 0, out, iv.length, cipher.length);
		return out;
	}

	public byte[] decrypt(byte[] encrypted) throws Exception {
		if (!enabled || encrypted == null || encrypted.length <= GCM_IV_LEN) return encrypted;
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		byte[] iv = new byte[GCM_IV_LEN];
		System.arraycopy(encrypted, 0, iv, 0, iv.length);
		byte[] cipher = new byte[encrypted.length - GCM_IV_LEN];
		System.arraycopy(encrypted, GCM_IV_LEN, cipher, 0, cipher.length);
		Cipher c = Cipher.getInstance(ALG);
		c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LEN, iv));
		return c.doFinal(cipher);
	}
}
