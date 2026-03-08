package br.com.jorgeacetozi.ebookChat.fileapproval.api;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.fileapproval.domain.model.FileTransferRequest;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.service.FileTransferRequestService;

/**
 * BR-3.1: Approve/reject and list file transfer requests.
 */
@RestController
@RequestMapping("/api/file-requests")
public class FileTransferRequestController {

	private final FileTransferRequestService service;

	@Autowired
	public FileTransferRequestController(FileTransferRequestService service) {
		this.service = service;
	}

	@GetMapping("/pending")
	@Secured("ROLE_ADMIN")
	public List<FileTransferRequest> listPending() {
		return service.findPending();
	}

	@GetMapping("/my")
	public List<FileTransferRequest> myRequests(Principal principal) {
		return principal == null ? java.util.Collections.emptyList() : service.findByRequester(principal.getName());
	}

	@PostMapping("/{id}/approve")
	@Secured("ROLE_ADMIN")
	public void approve(@PathVariable String id, Principal principal) {
		service.approve(id, principal != null ? principal.getName() : "admin");
	}

	@PostMapping("/{id}/reject")
	@Secured("ROLE_ADMIN")
	public void reject(@PathVariable String id, Principal principal) {
		service.reject(id, principal != null ? principal.getName() : "admin");
	}
}
