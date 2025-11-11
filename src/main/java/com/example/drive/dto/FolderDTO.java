package com.example.drive.dto;

import com.example.drive.entity.FolderEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderDTO {
    private Long id;
    private String folderName;
    private Long parentId;
    private String createdAt;

    public static FolderDTO fromEntity(FolderEntity entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return FolderDTO.builder()
                .id(entity.getId())
                .folderName(entity.getFolderName())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .createdAt(entity.getCreatedAt().format(formatter))
                .build();
    }
}