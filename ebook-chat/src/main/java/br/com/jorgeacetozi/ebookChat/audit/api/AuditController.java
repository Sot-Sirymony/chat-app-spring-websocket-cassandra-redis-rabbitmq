package br.com.jorgeacetozi.ebookChat.audit.api;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.audit.domain.model.AuditEvent;
import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;

/**
 * Read-only audit API for admins (BR-5.1).
 */
@RestController
@RequestMapping("/api/audit")
@Secured("ROLE_ADMIN")
public class AuditController {

	private final AuditService auditService;

	@Autowired
	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping
	public List<AuditEvent> getAudit(
			@RequestParam(required = false) String user,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {
		return auditService.findEvents(user, from, to);
	}
}
