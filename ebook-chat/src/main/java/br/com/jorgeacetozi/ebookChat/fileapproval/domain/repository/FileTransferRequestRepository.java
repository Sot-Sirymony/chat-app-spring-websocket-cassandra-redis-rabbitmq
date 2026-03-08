package br.com.jorgeacetozi.ebookChat.fileapproval.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgeacetozi.ebookChat.fileapproval.domain.model.FileTransferRequest;

public interface FileTransferRequestRepository extends JpaRepository<FileTransferRequest, String> {

	List<FileTransferRequest> findByStatusOrderByRequestedAtDesc(String status);
	List<FileTransferRequest> findByRequesterOrderByRequestedAtDesc(String requester);
}
