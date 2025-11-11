package com.example.drive.controller;

import com.example.drive.dto.FolderDTO;
import com.example.drive.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(
            @RequestParam String folderName,
            @RequestParam(required = false) Long parentId) {
        FolderDTO folder = folderService.createFolder(folderName, parentId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "FOLDER_CREATED");
        message.put("folder", folder);
        message.put("parentId", parentId);
        messagingTemplate.convertAndSend("/topic/drive", message);

        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "FOLDER_DELETED");
        message.put("folderId", id);
        messagingTemplate.convertAndSend("/topic/drive", message);

        return ResponseEntity.ok().build();
    }
}