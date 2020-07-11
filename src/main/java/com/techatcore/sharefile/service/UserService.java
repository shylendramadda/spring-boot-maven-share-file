package com.techatcore.sharefile.service;

import com.techatcore.sharefile.domain.User;
import com.techatcore.sharefile.dto.UserDto;
import com.techatcore.sharefile.repo.UserRepo;
import com.techatcore.sharefile.utils.SecurityUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Shylendra Madda
 */
@Slf4j
@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ModelMapper dataMapper;

    public ResponseEntity<Object> registerUser(UserDto userDto) {
        try {
            log.debug("Triggered user registration API");
            User user = dataMapper.map(userDto, User.class);
            Optional<User> userByEmail = userRepo.findByEmail(user.getEmail());
            User existingUser;
            if (userByEmail.isPresent()) {
                existingUser = userByEmail.get();
                log.warn("User already exists with this email: " + existingUser.getEmail());
                UserDto userDtoToSend = getUserDto(existingUser);
                return new ResponseEntity<>(userDtoToSend, HttpStatus.ACCEPTED);
            } else {
                user.setRoles("USER");
                existingUser = userRepo.save(user);
                log.info("User saved with Id: " + existingUser.getId());
                UserDto userDtoToSend = getUserDto(existingUser);
                return new ResponseEntity<>(userDtoToSend, HttpStatus.CREATED);
            }
        } catch (Exception e) {
            log.error("Internal server error");
            return new ResponseEntity<>("Internal server error. " + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UserDto getUserDto(User existingUser) {
        return dataMapper.map(existingUser, UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            return new SecurityUserDetails(user);
        }
        throw new RuntimeException("You have provided invalid data for register");
    }
}
