package br.com.jorgeacetozi.ebookChat.dlp.presidio;

/**
 * Thrown when Presidio Analyzer is unreachable or returns an error (TP.2 fallback).
 */
public class PresidioUnavailableException extends RuntimeException {

	public PresidioUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
