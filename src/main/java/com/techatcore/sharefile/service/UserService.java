package com.techatcore.sharefile.service;

import com.techatcore.sharefile.domain.User;
import com.techatcore.sharefile.dto.UserDto;
import com.techatcore.sharefile.repo.UserRepo;
import com.techatcore.sharefile.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Shylendra Madda
 */
@Slf4j
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ModelMapper dataMapper;

    public ResponseEntity<UserDto> registerUser(UserDto userDto, HttpSession httpSession) {
        try {
            log.debug("Triggered user registration API");
            User user = dataMapper.map(userDto, User.class);
            Optional<User> userByEmail = userRepo.findByEmail(user.getEmail());
            User existingUser;
            if (userByEmail.isPresent()) {
                existingUser = userByEmail.get();
                log.warn("User already exists with this email: " + existingUser.getEmail());
            } else {
                existingUser = userRepo.save(user);
                log.info("User saved with Id: " + existingUser.getId());
            }
            httpSession.setAttribute(Constants.USER, existingUser);
            UserDto userDtoToSend = dataMapper.map(existingUser, UserDto.class);
            return new ResponseEntity<>(userDtoToSend, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Internal server error");
        }
    }
}
