package com.example.drive.controller;

import com.example.drive.dto.FileDTO;
import com.example.drive.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/upload")
    public ResponseEntity<FileDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long folderId) {
        try {
            FileDTO uploadedFile = fileService.uploadFile(file, folderId);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "FILE_UPLOADED");
            message.put("file", uploadedFile);
            message.put("folderId", folderId);
            messagingTemplate.convertAndSend("/topic/drive", message);

            return ResponseEntity.ok(uploadedFile);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            Resource resource = fileService.downloadFile(id);
            String filename = fileService.getFileName(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "FILE_DELETED");
            message.put("fileId", id);
            messagingTemplate.convertAndSend("/topic/drive", message);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}