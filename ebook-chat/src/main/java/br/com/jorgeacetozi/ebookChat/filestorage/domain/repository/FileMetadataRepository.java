package br.com.jorgeacetozi.ebookChat.filestorage.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgeacetozi.ebookChat.filestorage.domain.model.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
}
