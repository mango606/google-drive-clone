package com.example.drive.repository;

import com.example.drive.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    List<FolderEntity> findByParentIsNull();

    List<FolderEntity> findByParent(FolderEntity parent);

    List<FolderEntity> findByParentId(Long parentId);
}