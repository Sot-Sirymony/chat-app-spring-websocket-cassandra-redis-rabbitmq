package br.com.jorgeacetozi.ebookChat.dlp.presidio;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;

/**
 * TP.2: HTTP client for Presidio Analyzer POST /analyze.
 */
public class PresidioAnalyzerClient {

	private final PresidioProperties properties;
	private final RestTemplate restTemplate;

	public PresidioAnalyzerClient(PresidioProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Math.min(3000, properties.getTimeoutMs()));
		factory.setReadTimeout(properties.getTimeoutMs());
		this.restTemplate = new RestTemplate(factory);
	}

	/**
	 * Call Presidio /analyze. Returns empty list on timeout or error.
	 */
	public List<PresidioEntity> analyze(String text) {
		if (text == null || text.isEmpty()) return Collections.emptyList();
		String url = properties.getAnalyzerBaseUrl().replaceAll("/$", "") + "/analyze";
		AnalyzeRequest req = new AnalyzeRequest(text, properties.getLanguage());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<AnalyzeRequest> entity = new HttpEntity<>(req, headers);
		try {
			ResponseEntity<PresidioEntity[]> re = restTemplate.postForEntity(url, entity, PresidioEntity[].class);
			if (re.getBody() == null) return Collections.emptyList();
			return java.util.Arrays.asList(re.getBody());
		} catch (ResourceAccessException e) {
			throw new PresidioUnavailableException("Presidio analyzer unavailable: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new PresidioUnavailableException("Presidio analyzer error: " + e.getMessage(), e);
		}
	}

	public static class AnalyzeRequest {
		private String text;
		private String language;

		public AnalyzeRequest(String text, String language) {
			this.text = text;
			this.language = language;
		}
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
		public String getLanguage() { return language; }
		public void setLanguage(String language) { this.language = language; }
	}

	/** One detected PII entity from Presidio. */
	public static class PresidioEntity {
		private String entity_type;
		private double score;
		private int start;
		private int end;

		public String getEntity_type() { return entity_type; }
		public void setEntity_type(String entity_type) { this.entity_type = entity_type; }
		public double getScore() { return score; }
		public void setScore(double score) { this.score = score; }
		public int getStart() { return start; }
		public void setStart(int start) { this.start = start; }
		public int getEnd() { return end; }
		public void setEnd(int end) { this.end = end; }
	}
}
