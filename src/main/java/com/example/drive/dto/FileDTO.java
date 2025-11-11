package com.example.drive.dto;

import com.example.drive.entity.FileEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Long folderId;
    private String uploadedAt;
    private String modifiedAt;

    public static FileDTO fromEntity(FileEntity entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return FileDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .folderId(entity.getFolder() != null ? entity.getFolder().getId() : null)
                .uploadedAt(entity.getUploadedAt().format(formatter))
                .modifiedAt(entity.getModifiedAt().format(formatter))
                .build();
    }

    public String getFileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}