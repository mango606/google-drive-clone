package com.example.drive.service;

import com.example.drive.dto.FileDTO;
import com.example.drive.entity.FileEntity;
import com.example.drive.entity.FolderEntity;
import com.example.drive.repository.FileRepository;
import com.example.drive.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<FileDTO> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(FileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FileDTO> getFilesByFolderId(Long folderId) {
        if (folderId == null) {
            return fileRepository.findByFolderIsNull().stream()
                    .map(FileDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return fileRepository.findByFolderId(folderId).stream()
                .map(FileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public FileDTO uploadFile(MultipartFile file, Long folderId) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 폴더 조회
        FolderEntity folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId).orElse(null);
        }

        FileEntity fileEntity = FileEntity.builder()
                .fileName(originalFilename)
                .storedFileName(storedFileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .filePath(filePath.toString())
                .folder(folder)
                .build();

        FileEntity savedFile = fileRepository.save(fileEntity);
        return FileDTO.fromEntity(savedFile);
    }

    public Resource downloadFile(Long fileId) throws IOException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        }
    }

    public String getFileName(Long fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        return file.getFileName();
    }

    public void deleteFile(Long fileId) throws IOException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(file.getFilePath());
        Files.deleteIfExists(filePath);

        fileRepository.delete(file);
    }
}