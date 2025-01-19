package tech.investbuddy.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import tech.investbuddy.userservice.dto.UserLoginRequest;
import tech.investbuddy.userservice.dto.UserRequest;
import tech.investbuddy.userservice.model.User;
import tech.investbuddy.userservice.service.KeycloakService;
import tech.investbuddy.userservice.service.UserService;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    private final KeycloakService keycloakService;


    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@Valid @RequestBody UserRequest userRequest) {
        String response = keycloakService.createUser(userRequest);
        System.out.println(response);
        return userService.createUser(userRequest);
    }



    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> authenticateUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        String access_token = keycloakService.authenticate(userLoginRequest);
        UUID user_id = userService.authenticate(userLoginRequest);
        Map<String, String> reponse = new HashMap<>();
        reponse.put("access_token", access_token);
        reponse.put("user_id", String.valueOf(user_id));
        return reponse;
    }

    @GetMapping("/auth/verify-email")
    public RedirectView verifyEmail(@RequestParam String token) {
        // Recherchez l'utilisateur par token
        User user = userService.findByVerificationToken(token);
        // Marquez l'email comme vérifié
        //user.setIsEmailVerified(true);
        user.setEmailVerified(true);
        user.setVerificationToken(null); // Supprimez le token après vérification
        userService.save(user);
        String frontendUrl = "http://localthost:3000/success?userId=" + user.getId() + "&token=" + token;
        //return ResponseEntity.ok("Email verified successfully!");
        return new RedirectView(frontendUrl);
    }

}
