package com.techatcore.sharefile.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Shylendra Madda
 */
@Data
public class FilesDto {
    private List<UploadFileDto> uploadedFiles;
    private List<ShareFileDto> sharedFiles;
}
