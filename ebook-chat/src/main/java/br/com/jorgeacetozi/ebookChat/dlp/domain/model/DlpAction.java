package br.com.jorgeacetozi.ebookChat.dlp.domain.model;

/**
 * Action to take based on DLP risk (BR-4.1).
 */
public enum DlpAction {
	ALLOW,
	WARN,
	BLOCK,
	REQUIRE_APPROVAL
}
