package br.com.jorgeacetozi.ebookChat.audit.domain.repository;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;

import br.com.jorgeacetozi.ebookChat.audit.domain.model.AuditEvent;

public interface AuditEventRepository extends CassandraRepository<AuditEvent> {

	List<AuditEvent> findByUsername(String username);
}
