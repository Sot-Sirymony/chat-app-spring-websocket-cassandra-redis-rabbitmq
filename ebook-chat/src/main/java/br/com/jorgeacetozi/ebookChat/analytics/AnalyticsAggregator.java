package br.com.jorgeacetozi.ebookChat.analytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * In-memory aggregation of deny counts per user and per resource (room) for T8.1.2.
 * Updated when audit events with result=deny are logged.
 */
@Component
public class AnalyticsAggregator {

	private final Map<String, Long> denyCountByUser = new ConcurrentHashMap<>();
	private final Map<String, Long> denyCountByResource = new ConcurrentHashMap<>();

	public void recordDeny(String username, String resource) {
		if (username != null && !username.isEmpty()) {
			denyCountByUser.merge(username, 1L, Long::sum);
		}
		if (resource != null && !resource.isEmpty()) {
			denyCountByResource.merge(resource, 1L, Long::sum);
		}
	}

	public List<RiskyUserEntry> getRiskyUsers(int limit) {
		return denyCountByUser.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.limit(limit > 0 ? limit : 50)
				.map(e -> new RiskyUserEntry(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	public List<RiskyRoomEntry> getRiskyRooms(int limit) {
		return denyCountByResource.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.limit(limit > 0 ? limit : 50)
				.map(e -> new RiskyRoomEntry(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	public static class RiskyUserEntry {
		private final String username;
		private final long denyCount;

		public RiskyUserEntry(String username, long denyCount) {
			this.username = username;
			this.denyCount = denyCount;
		}
		public String getUsername() { return username; }
		public long getDenyCount() { return denyCount; }
	}

	public static class RiskyRoomEntry {
		private final String roomId;
		private final long denyCount;

		public RiskyRoomEntry(String roomId, long denyCount) {
			this.roomId = roomId;
			this.denyCount = denyCount;
		}
		public String getRoomId() { return roomId; }
		public long getDenyCount() { return denyCount; }
	}
}
