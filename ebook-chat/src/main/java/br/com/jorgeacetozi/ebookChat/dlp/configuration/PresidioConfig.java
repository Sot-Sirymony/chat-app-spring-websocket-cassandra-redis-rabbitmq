package br.com.jorgeacetozi.ebookChat.dlp.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.jorgeacetozi.ebookChat.dlp.presidio.PresidioAnalyzerClient;

/**
 * TP.2: Exposes PresidioAnalyzerClient when Presidio is enabled.
 */
@Configuration
public class PresidioConfig {

	@Bean
	@ConditionalOnProperty(name = "ebook.chat.presidio.enabled", havingValue = "true")
	public PresidioAnalyzerClient presidioAnalyzerClient(PresidioProperties properties) {
		return new PresidioAnalyzerClient(properties);
	}
}
