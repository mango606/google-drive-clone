package com.example.drive.controller;

import com.example.drive.dto.FileDTO;
import com.example.drive.dto.FolderDTO;
import com.example.drive.service.FileService;
import com.example.drive.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DriveController {

    private final FileService fileService;
    private final FolderService folderService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) Long folderId, Model model) {
        List<FolderDTO> folders = folderService.getFoldersByParentId(folderId);
        List<FileDTO> files = fileService.getFilesByFolderId(folderId);

        model.addAttribute("folders", folders);
        model.addAttribute("files", files);
        model.addAttribute("currentFolderId", folderId);

        // 현재 폴더 정보 추가
        if (folderId != null) {
            FolderDTO currentFolder = folderService.getFolder(folderId);
            model.addAttribute("currentFolder", currentFolder);
        }

        return "index";
    }
}