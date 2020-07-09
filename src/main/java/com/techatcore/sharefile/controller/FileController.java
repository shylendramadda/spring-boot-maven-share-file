package com.techatcore.sharefile.controller;

import com.techatcore.sharefile.dto.DownloadFileDto;
import com.techatcore.sharefile.dto.FilesDto;
import com.techatcore.sharefile.dto.ShareFileDto;
import com.techatcore.sharefile.dto.UploadFileDto;
import com.techatcore.sharefile.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

/**
 * @author Shylendra Madda
 */
@RequestMapping("api/file")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping
    private ResponseEntity<FilesDto> getFiles(HttpSession httpSession) {
        return fileService.getFiles(httpSession);
    }

    @PostMapping("/upload")
    private ResponseEntity<UploadFileDto> uploadFile(@RequestParam("file") MultipartFile file, HttpSession httpSession) {
        return fileService.uploadFile(file, httpSession);
    }

    @GetMapping("/{id}")
    private ResponseEntity<DownloadFileDto> downloadFile(@PathVariable String fileId, HttpSession httpSession) {
        return fileService.downloadFile(fileId, httpSession);
    }

    @PostMapping("/share")
    private ResponseEntity<ShareFileDto> shareFile(@RequestBody ShareFileDto shareFileDto, HttpSession httpSession) {
        return fileService.shareFile(shareFileDto, httpSession);
    }
}
