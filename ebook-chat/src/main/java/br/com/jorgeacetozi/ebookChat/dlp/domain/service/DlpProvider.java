package br.com.jorgeacetozi.ebookChat.dlp.domain.service;

import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;

/**
 * TP.4: Abstraction for a DLP scan provider (rule-based or Presidio).
 */
public interface DlpProvider {

	/**
	 * Scan text and return risk result. Must not return null.
	 */
	DlpScanResult scan(String text);
}
