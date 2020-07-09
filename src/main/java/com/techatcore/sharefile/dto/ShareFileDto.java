package com.techatcore.sharefile.dto;

import com.techatcore.sharefile.domain.User;
import lombok.Data;

import java.util.List;

/**
 * @author Shylendra Madda
 */
@Data
public class ShareFileDto {
    private String id;
    private String fileName;
    private String fileType;
    private User from;
    private List<User> to;
}
