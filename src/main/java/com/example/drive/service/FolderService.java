package com.example.drive.service;

import com.example.drive.dto.FolderDTO;
import com.example.drive.entity.FolderEntity;
import com.example.drive.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderService {

    private final FolderRepository folderRepository;

    public List<FolderDTO> getAllFolders() {
        return folderRepository.findAll().stream()
                .map(FolderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FolderDTO> getFoldersByParentId(Long parentId) {
        if (parentId == null) {
            return folderRepository.findByParentIsNull().stream()
                    .map(FolderDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return folderRepository.findByParentId(parentId).stream()
                .map(FolderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public FolderDTO createFolder(String folderName, Long parentId) {
        FolderEntity parent = null;
        if (parentId != null) {
            parent = folderRepository.findById(parentId).orElse(null);
        }

        FolderEntity folder = FolderEntity.builder()
                .folderName(folderName)
                .parent(parent)
                .build();

        FolderEntity savedFolder = folderRepository.save(folder);
        return FolderDTO.fromEntity(savedFolder);
    }

    public void deleteFolder(Long folderId) {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));

        folderRepository.delete(folder);
    }

    public FolderDTO getFolder(Long folderId) {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다."));
        return FolderDTO.fromEntity(folder);
    }
}