package br.com.jorgeacetozi.ebookChat.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Runs before Spring Security and dispatches /ws/** directly to the DispatcherServlet,
 * so SockJS handshake (e.g. GET /ws/info) is never blocked by the security chain.
 */
public class WebSocketBypassSecurityFilter extends OncePerRequestFilter {

	private final DispatcherServlet dispatcherServlet;

	public WebSocketBypassSecurityFilter(DispatcherServlet dispatcherServlet) {
		this.dispatcherServlet = dispatcherServlet;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();
		if (path != null && path.startsWith("/ws")) {
			dispatcherServlet.service(request, response);
			return;
		}
		filterChain.doFilter(request, response);
	}
}
