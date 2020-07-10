package com.techatcore.sharefile.controller;

import com.techatcore.sharefile.dto.UserDto;
import com.techatcore.sharefile.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * @author Shylendra Madda
 */
@RequestMapping("/api")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ExceptionHandler(RuntimeException.class)
    @PostMapping("/register")
    private ResponseEntity<Object> registerUser(@RequestBody UserDto userDto, Principal principal) {
        return userService.registerUser(userDto);
    }

}
