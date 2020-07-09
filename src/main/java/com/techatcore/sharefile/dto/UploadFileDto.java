package com.techatcore.sharefile.dto;

import com.techatcore.sharefile.domain.User;
import lombok.Data;

/**
 * @author Shylendra Madda
 */
@Data
public class UploadFileDto {
    private String id;
    private User from;
    private String fileName;
    private String fileType;
}
