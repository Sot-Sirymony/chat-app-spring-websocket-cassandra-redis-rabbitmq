package br.com.jorgeacetozi.ebookChat.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class WebSocketBypassFilterConfig {

	@Bean
	public FilterRegistrationBean webSocketBypassFilter(WebSocketBypassSecurityFilter filter) {
		FilterRegistrationBean reg = new FilterRegistrationBean(filter);
		reg.addUrlPatterns("/ws", "/ws/*", "/ws/*/*", "/ws/*/*/*", "/ws/*/*/*/*");
		reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return reg;
	}

	@Bean
	public WebSocketBypassSecurityFilter webSocketBypassSecurityFilter(DispatcherServlet dispatcherServlet) {
		return new WebSocketBypassSecurityFilter(dispatcherServlet);
	}
}
