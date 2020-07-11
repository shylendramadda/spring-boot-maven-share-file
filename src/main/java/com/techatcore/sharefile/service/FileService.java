package com.techatcore.sharefile.service;

import com.techatcore.sharefile.domain.FileEntity;
import com.techatcore.sharefile.domain.User;
import com.techatcore.sharefile.dto.DownloadFileDto;
import com.techatcore.sharefile.dto.FilesDto;
import com.techatcore.sharefile.dto.ShareFileDto;
import com.techatcore.sharefile.dto.UploadFileDto;
import com.techatcore.sharefile.repo.FileRepo;
import com.techatcore.sharefile.repo.UserRepo;
import com.techatcore.sharefile.utils.SessionUtil;
import com.techatcore.sharefile.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shylendra Madda
 */
@Slf4j
@Service
@Transactional
public class FileService {

    @Autowired
    private FileRepo fileRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ModelMapper dataMapper;
    @Value("${file.upload-path}")
    private String fileUploadPath;

    private User getUserFromSession() {
        return SessionUtil.getUser();
    }

    public ResponseEntity<Object> getFiles() {
        try {
            log.debug("Triggered user getFiles API");
            // Get user from the session
            User user = getUserFromSession();
            // Get all uploaded entity files related to the user and convert to Dto
            List<FileEntity> fileEntities = fileRepo.findByFrom(user);
            List<UploadFileDto> ownedFileDtoList = fileEntities
                    .stream()
                    .map(fileEntity -> dataMapper.map(fileEntity, UploadFileDto.class))
                    .collect(Collectors.toList());
            // Get all shared entity files and convert to Dto
            List<ShareFileDto> sharedFilesDtoList = fileEntities
                    .stream()
                    .map(fileEntity -> fileEntity.getTo().stream().map(user1 -> fileRepo.findById(user1.getId())))
                    .map(sharedFile -> dataMapper.map(sharedFile, ShareFileDto.class))
                    .collect(Collectors.toList());
            // Set both into files Dto
            FilesDto filesDto = new FilesDto();
            filesDto.setOwnedFiles(ownedFileDtoList);
            filesDto.setSharedFiles(sharedFilesDtoList);
            log.info("Returned uploaded files with count: " + ownedFileDtoList.size() +
                    " shared files with count: " + sharedFilesDtoList.size());
            return new ResponseEntity<>(filesDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return new ResponseEntity<>("Internal server error. " + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> uploadFile(MultipartFile file) {
        try {
            log.debug("Triggered user uploadFile API");
            // Get user from the session
            User user = getUserFromSession();
            // Normalize file name
            String fileName = StringUtils.cleanPath(Utils.getCurrentTime() + "_" + file.getOriginalFilename());
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            file.transferTo(new File(fileUploadPath + fileName));
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFileType(file.getContentType());
            fileEntity.setOwned(true);
            fileEntity.setFrom(user);
            // Save file entity
            FileEntity uploadedFile = fileRepo.save(fileEntity);
            log.info("File uploaded with file Id: " + uploadedFile.getId());
            return new ResponseEntity<>("Successfully uploaded the file with id: " + uploadedFile.getId(),
                    HttpStatus.OK);
        } catch (IOException ex) {
            log.error("Could not store file. Please try again!", ex);
            return new ResponseEntity<>("Could not store file. Please try again! " + ex,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return new ResponseEntity<>("Internal server error. " + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> downloadFile(String fileId) {
        try {
            log.debug("Triggered user downloadFile API");
            // Get user from the session
            User user = getUserFromSession();
            Optional<FileEntity> fileEntity = fileRepo.findById(fileId);
            // Check is this file uploaded by the same user
            if (fileEntity.isPresent() && fileEntity.get().getFrom().getId().equals(Objects.requireNonNull(user).getId())) {
                String file = new String(Files.readAllBytes(Paths.get(fileUploadPath + fileEntity.get().getFileName())));
                String encodedString = Base64.getEncoder().encodeToString(file.getBytes());
                DownloadFileDto downloadFileDto = new DownloadFileDto();
                downloadFileDto.setFile(encodedString);
                log.info("File downloaded with file Id: " + downloadFileDto.getId());
                return new ResponseEntity<>(downloadFileDto, HttpStatus.OK);
            }
            log.error("File not found with this ID: " + fileId + ". Please try again!");
            throw new RuntimeException("File not found with this ID: " + fileId + ". Please try again!");
        } catch (IOException ex) {
            log.error("File not found with this ID: " + fileId + ". Please try again!", ex);
            return new ResponseEntity<>("File not found with this ID: " + fileId + ". Please try again! " + ex.toString(),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return new ResponseEntity<>("Internal server error. " + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> shareFile(ShareFileDto shareFileDto) {
        try {
            log.debug("Triggered user shareFile API");
            // Get user from the session
            User userFromSession = getUserFromSession();
            String fileId = shareFileDto.getId();
            // Check exist or not given file id
            FileEntity dbFileEntity = fileRepo.findById(fileId).orElse(null);
            if (dbFileEntity != null) {
                // Make sure is have a access to sure
                if (dbFileEntity.getFrom().getId().equals(Objects.requireNonNull(userFromSession).getId())) {
                    // Share file to others
                    List<User> toUserIds = shareFileDto.getTo();
                    List<User> toUsers = toUserIds.stream().map(user -> {
                        return userRepo.findById(user.getId()).get();
                    }).collect(Collectors.toList());
                    dbFileEntity.setTo(toUsers);
                    fileRepo.save(dbFileEntity);
                    return new ResponseEntity<>("Successfully shared the file", HttpStatus.OK);
                }
                log.error("User does not having permission to share this file!");
                return new ResponseEntity<>("User does not having permission to share this file!", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>("File does not exist", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return new ResponseEntity<>("Internal server error. " + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
