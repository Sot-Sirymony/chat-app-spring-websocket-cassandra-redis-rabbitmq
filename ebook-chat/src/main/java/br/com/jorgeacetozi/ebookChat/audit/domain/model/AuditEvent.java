package br.com.jorgeacetozi.ebookChat.audit.domain.model;

import java.util.Date;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Immutable audit log entry (BR-5.1).
 */
@Table("audit_events")
public class AuditEvent {

	@PrimaryKeyColumn(name = "username", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String username;
	@PrimaryKeyColumn(name = "timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private Date timestamp;
	private String action;
	private String resource;
	private String result; // allow, deny
	@Column("rule_id")
	private String ruleId;
	private String details;

	public AuditEvent() {}

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public Date getTimestamp() { return timestamp; }
	public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
	public String getResource() { return resource; }
	public void setResource(String resource) { this.resource = resource; }
	public String getResult() { return result; }
	public void setResult(String result) { this.result = result; }
	public String getRuleId() { return ruleId; }
	public void setRuleId(String ruleId) { this.ruleId = ruleId; }
	public String getDetails() { return details; }
	public void setDetails(String details) { this.details = details; }
}
