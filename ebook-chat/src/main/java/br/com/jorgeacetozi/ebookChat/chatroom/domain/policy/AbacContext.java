package br.com.jorgeacetozi.ebookChat.chatroom.domain.policy;

import java.util.Set;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.RoomClassification;

/**
 * ABAC attribute context: user, resource, environment (BR-2.1).
 */
public class AbacContext {

	private String username;
	private Set<String> roles;
	private String department;

	private String roomId;
	private RoomClassification roomLevel;
	private String messageType; // e.g. "public", "private"

	private long timestamp;
	private String clientIp;
	private String deviceType;
	/** T2.1.9: Optional risk score (e.g. from recent denials). */
	private int riskScore;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public Set<String> getRoles() { return roles; }
	public void setRoles(Set<String> roles) { this.roles = roles; }
	public String getDepartment() { return department; }
	public void setDepartment(String department) { this.department = department; }
	public String getRoomId() { return roomId; }
	public void setRoomId(String roomId) { this.roomId = roomId; }
	public RoomClassification getRoomLevel() { return roomLevel; }
	public void setRoomLevel(RoomClassification roomLevel) { this.roomLevel = roomLevel; }
	public String getMessageType() { return messageType; }
	public void setMessageType(String messageType) { this.messageType = messageType; }
	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	public String getClientIp() { return clientIp; }
	public void setClientIp(String clientIp) { this.clientIp = clientIp; }
	public String getDeviceType() { return deviceType; }
	public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
	public int getRiskScore() { return riskScore; }
	public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
