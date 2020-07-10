package com.techatcore.sharefile.controller;

import com.techatcore.sharefile.dto.ShareFileDto;
import com.techatcore.sharefile.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Shylendra Madda
 */
@RequestMapping("/api/file")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping
    private ResponseEntity<Object> getFiles() {
        return fileService.getFiles();
    }

    @PostMapping("/upload")
    private ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @GetMapping("/{id}")
    private ResponseEntity<Object> downloadFile(@PathVariable String fileId) {
        return fileService.downloadFile(fileId);
    }

    @PostMapping("/share")
    private ResponseEntity<Object> shareFile(@RequestBody ShareFileDto shareFileDto) {
        return fileService.shareFile(shareFileDto);
    }
}
