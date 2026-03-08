package br.com.jorgeacetozi.ebookChat.dlp.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;
import br.com.jorgeacetozi.ebookChat.dlp.presidio.PresidioUnavailableException;

/**
 * TP.5: Facade that runs rule-based DLP and optionally Presidio; merges results and falls back on Presidio failure.
 */
@Service
public class DlpEngine {

	private static final Logger log = LoggerFactory.getLogger(DlpEngine.class);

	private final RuleBasedDlpProvider ruleBasedDlpProvider;
	private final PresidioDlpProvider presidioDlpProvider;
	private final PresidioProperties presidioProperties;

	@Autowired
	public DlpEngine(RuleBasedDlpProvider ruleBasedDlpProvider,
			PresidioProperties presidioProperties,
			Optional<PresidioDlpProvider> presidioDlpProvider) {
		this.ruleBasedDlpProvider = ruleBasedDlpProvider;
		this.presidioProperties = presidioProperties;
		this.presidioDlpProvider = presidioDlpProvider != null ? presidioDlpProvider.orElse(null) : null;
	}

	/**
	 * Scan text: rule-based always; if Presidio enabled, call Presidio and merge (max risk, combined rule IDs). On Presidio timeout/unavailable, use rule-based only.
	 */
	public DlpScanResult scan(String text) {
		if (text == null || text.isEmpty()) {
			return DlpScanResult.allow();
		}
		DlpScanResult ruleResult = ruleBasedDlpProvider.scan(text);
		if (!presidioProperties.isEnabled() || presidioDlpProvider == null) {
			return ruleResult;
		}
		try {
			DlpScanResult presidioResult = presidioDlpProvider.scan(text);
			return merge(ruleResult, presidioResult);
		} catch (PresidioUnavailableException e) {
			log.warn("Presidio unavailable, using rule-based result only: {}", e.getMessage());
			return ruleResult;
		}
	}

	private DlpScanResult merge(DlpScanResult a, DlpScanResult b) {
		if (a.getMatchedRuleIds().isEmpty() && b.getMatchedRuleIds().isEmpty()) {
			return DlpScanResult.allow();
		}
		DlpRiskLevel maxLevel = a.getRiskLevel().ordinal() >= b.getRiskLevel().ordinal() ? a.getRiskLevel() : b.getRiskLevel();
		List<String> combined = new ArrayList<>(a.getMatchedRuleIds());
		for (String id : b.getMatchedRuleIds()) {
			if (!combined.contains(id)) combined.add(id);
		}
		DlpAction action = actionFor(maxLevel);
		String msg = (a.getMessage() != null ? a.getMessage() : "") + (b.getMessage() != null ? " " + b.getMessage() : "").trim();
		return new DlpScanResult(maxLevel, combined, action, msg);
	}

	private DlpAction actionFor(DlpRiskLevel level) {
		switch (level) {
			case CRITICAL: return DlpAction.BLOCK;
			case HIGH:    return DlpAction.REQUIRE_APPROVAL;
			case MEDIUM:  return DlpAction.WARN;
			case LOW:
			default:      return DlpAction.ALLOW;
		}
	}

	/**
	 * Scan file content; supports text/plain. Extracted text is run through scan() (TP.7: Presidio included when enabled).
	 */
	public DlpScanResult scanFile(String contentType, byte[] content) {
		if (content == null || content.length == 0) {
			return DlpScanResult.allow();
		}
		if (contentType != null && contentType.toLowerCase().contains("text/plain")) {
			try {
				return scan(new String(content, java.nio.charset.StandardCharsets.UTF_8));
			} catch (Exception e) {
				return DlpScanResult.allow();
			}
		}
		return DlpScanResult.allow();
	}
}
