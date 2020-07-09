package com.techatcore.sharefile.service;

import com.techatcore.sharefile.domain.FileEntity;
import com.techatcore.sharefile.domain.User;
import com.techatcore.sharefile.dto.DownloadFileDto;
import com.techatcore.sharefile.dto.FilesDto;
import com.techatcore.sharefile.dto.ShareFileDto;
import com.techatcore.sharefile.dto.UploadFileDto;
import com.techatcore.sharefile.repo.FileRepo;
import com.techatcore.sharefile.repo.UserRepo;
import com.techatcore.sharefile.utils.Constants;
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

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
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

    private User getUserFromSession(HttpSession httpSession) {
        return (User) httpSession.getAttribute(Constants.USER);
    }

    public ResponseEntity<FilesDto> getFiles(HttpSession httpSession) {
        try {
            log.debug("Triggered user getFiles API");
            // Get user from the session
            User user = getUserFromSession(httpSession);
            List<FileEntity> fileEntitiesUploaded = fileRepo.findByFrom(user);
            List<FileEntity> fileEntitiesShared = fileRepo.findByTo(user);
            // Get all uploaded entity files and convert to Dto
            List<UploadFileDto> uploadFileDtoList = fileEntitiesUploaded
                    .stream()
                    .map(fileEntity -> dataMapper.map(fileEntity, UploadFileDto.class))
                    .collect(Collectors.toList());
            // Get all shared entity files and convert to Dto
            List<ShareFileDto> sharedFilesDtoList = fileEntitiesShared
                    .stream()
                    .map(sharedFile -> dataMapper.map(sharedFile, ShareFileDto.class))
                    .collect(Collectors.toList());
            // Set both into files Dto
            FilesDto filesDto = new FilesDto();
            filesDto.setUploadedFiles(uploadFileDtoList);
            filesDto.setSharedFiles(sharedFilesDtoList);
            log.info("Returned uploaded files with count: " + uploadFileDtoList.size() +
                    " shared files with count: " + sharedFilesDtoList.size());
            return new ResponseEntity<>(filesDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new RuntimeException("Internal server error");
        }
    }

    public ResponseEntity<UploadFileDto> uploadFile(MultipartFile file, HttpSession httpSession) {
        try {
            log.debug("Triggered user uploadFile API");
            // Get user from the session
            User user = getUserFromSession(httpSession);
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
            UploadFileDto uploadFileDto = dataMapper.map(uploadedFile, UploadFileDto.class);
            log.info("File uploaded with file Id: " + uploadedFile.getId());
            return new ResponseEntity<>(uploadFileDto, HttpStatus.OK);
        } catch (IOException ex) {
            log.error("Could not store file. Please try again!", ex);
            throw new RuntimeException("Could not store file. Please try again!", ex);
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new RuntimeException("Internal server error", e);
        }
    }

    public ResponseEntity<DownloadFileDto> downloadFile(String fileId, HttpSession httpSession) {
        try {
            log.debug("Triggered user downloadFile API");
            // Get user from the session
            User user = getUserFromSession(httpSession);
            Optional<FileEntity> fileEntity = fileRepo.findById(fileId);
            // Check is this file uploaded by the same user
            if (fileEntity.isPresent() && fileEntity.get().getFrom().getId().equals(user.getId())) {
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
            throw new RuntimeException("File not found with this ID: " + fileId + ". Please try again!", ex);
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new RuntimeException("Internal server error", e);
        }
    }

    public ResponseEntity<ShareFileDto> shareFile(ShareFileDto shareFileDto, HttpSession httpSession) {
        try {
            log.debug("Triggered user shareFile API");
            // Get user from the session
            User userFromSession = getUserFromSession(httpSession);
            FileEntity fileEntity = dataMapper.map(shareFileDto, FileEntity.class);
            // Get the user list from entity to share the file
            List<User> userList = fileEntity.getTo().stream()
                    .map(user -> userRepo.findById(user.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            fileEntity.setTo(userList);
            Optional<FileEntity> fileData = fileRepo.findById(fileEntity.getId());
            // Check is this file uploaded by the same user
            if (fileData.isPresent() && fileData.get().getFrom().getId().equals(userFromSession.getId())) {
                FileEntity entity = fileRepo.save(fileEntity);
                ShareFileDto fileDto = dataMapper.map(entity, ShareFileDto.class);
                return new ResponseEntity<>(fileDto, HttpStatus.OK);
            } else {
                log.error("User does not having permission to share this file!");
                throw new RuntimeException("User does not having permission to share this file!");
            }
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new RuntimeException("Internal server error", e);
        }
    }
}
