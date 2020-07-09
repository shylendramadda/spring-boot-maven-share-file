package com.techatcore.sharefile.controller;

import com.techatcore.sharefile.dto.UserDto;
import com.techatcore.sharefile.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author Shylendra Madda
 */
@RequestMapping("api/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ExceptionHandler(RuntimeException.class)
    @PostMapping("/register")
    private ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto, HttpSession httpSession) {
        return userService.registerUser(userDto, httpSession);
    }

}
