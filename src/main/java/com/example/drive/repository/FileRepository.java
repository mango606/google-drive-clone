package com.example.drive.repository;

import com.example.drive.entity.FileEntity;
import com.example.drive.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByFolderIsNull();

    List<FileEntity> findByFolder(FolderEntity folder);

    List<FileEntity> findByFolderId(Long folderId);
}