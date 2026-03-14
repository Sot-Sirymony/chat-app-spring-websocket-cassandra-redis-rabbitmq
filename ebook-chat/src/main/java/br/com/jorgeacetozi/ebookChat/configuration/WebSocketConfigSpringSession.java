package br.com.jorgeacetozi.ebookChat.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.ExpiringSession;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfigSpringSession extends AbstractSessionWebSocketMessageBrokerConfigurer<ExpiringSession> {

	@Value("${ebook.chat.relay.host}")
	private String relayHost;

	@Value("${ebook.chat.relay.port}")
	private Integer relayPort;

	@Autowired
	private JwtChannelInterceptor jwtChannelInterceptor;

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(jwtChannelInterceptor);
	}

	/** Builds allowed origins for localhost and 127.0.0.1 on ports 3000-3999 and common dev ports. */
	private static String[] frontendOriginsAllPorts() {
		List<String> origins = new ArrayList<>();
		for (int port = 3000; port <= 3999; port++) {
			origins.add("http://localhost:" + port);
			origins.add("http://127.0.0.1:" + port);
		}
		for (int port : new int[] { 4173, 5173, 8080, 8081, 8082 }) {
			origins.add("http://localhost:" + port);
			origins.add("http://127.0.0.1:" + port);
		}
		return origins.toArray(new String[0]);
	}

	protected void configureStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOrigins(frontendOriginsAllPorts())
			.withSockJS();
	}

	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableStompBrokerRelay("/queue/", "/topic/")
			.setUserDestinationBroadcast("/topic/unresolved.user.dest")
			.setUserRegistryBroadcast("/topic/registry.broadcast")
			.setRelayHost(relayHost)
			.setRelayPort(relayPort);

		registry.setApplicationDestinationPrefixes("/chatroom");
	}
}
