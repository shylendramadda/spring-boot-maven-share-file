package com.techatcore.sharefile.domain;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * @author Shylendra Madda
 */
@Entity
@Data
public class FileEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String fileName;
    private String fileType;
    private boolean isOwned;
    @ManyToOne
    private User from;
    @ManyToMany
    private List<User> to;
}
