package tech.investbuddy.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.investbuddy.userservice.dto.UserRequest;
import tech.investbuddy.userservice.dto.UserResponse;
import tech.investbuddy.userservice.model.User;
import tech.investbuddy.userservice.service.UserService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

//    @PostMapping("/api/v1/users")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void createUser(@Valid @RequestBody UserRequest userRequest) {
//        userService.save(userRequest);
//    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return  userService.getAllUsers();
    }

//    @GetMapping("/test")
//    public ResponseEntity<User> test() {
//        System.out.println("Test endpoint reached");
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User currentUser = (User) auth.getPrincipal();
//        System.out.println("currentUser " + currentUser);
//        return new ResponseEntity<>(currentUser, HttpStatus.CREATED);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/exists")
    @ResponseStatus(HttpStatus.OK)
    public boolean UserExists(@PathVariable UUID id) {
        return userService.isUser(id);
    }

}
