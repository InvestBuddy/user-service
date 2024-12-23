package tech.investbuddy.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import tech.investbuddy.userservice.dto.UserLoginRequest;
import tech.investbuddy.userservice.dto.UserRequest;
import tech.investbuddy.userservice.dto.UserResponse;
import tech.investbuddy.userservice.exception.UserAlreadyExistsException;
import tech.investbuddy.userservice.model.User;
import tech.investbuddy.userservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    public UUID createUser(UserRequest userRequest) {

        // Vérifier si l'utilisateur existe déjà
        userRepository.findUserByEmail(userRequest.getEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("User with email " + userRequest.getEmail() + " already exists");
                });

        String hashedPassword = BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt());
        String verificationToken = UUID.randomUUID().toString();

        // Créez et sauvegardez l'utilisateur
        User user = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .password(hashedPassword)
                .address(userRequest.getAddress())
                .birthDate(userRequest.getBirthDate())
                .isEmailVerified(false)
                .verificationToken(verificationToken)
                .build();
        userRepository.save(user);

        // Produire un événement Kafka
        String emailVerificationMessage = String.format("{\"email\":\"%s\",\"verificationToken\":\"%s\"}",
                user.getEmail(),
                verificationToken);
        String kycVerificationMessage = String.format(
                "{\"userId\":\"%s\", \"firstName\":\"%s\", \"lastName\":\"%s\"}",
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );

        kafkaProducer.sendMessage("user-created", emailVerificationMessage);
        kafkaProducer.sendMessage("kyc-verification", kycVerificationMessage);
        return user.getId();
    }

    public void updateUser(UUID userId, UserRequest userRequest) {
        // Vérifiez si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Mettre à jour les champs modifiables
        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }

        if (userRequest.getEmail() != null) {
            // Vérifier si le nouvel email est déjà utilisé par un autre utilisateur
            userRepository.findUserByEmail(userRequest.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new RuntimeException("Email " + userRequest.getEmail() + " is already in use");
                        }
                    });
            user.setEmail(userRequest.getEmail());
        }

        if (userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }

        if (userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }

        if (userRequest.getBirthDate() != null) {
            user.setBirthDate(userRequest.getBirthDate());
        }

        // Gestion du mot de passe
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt()));
        }

        // Sauvegardez les modifications
        userRepository.save(user);
    }


    public void save (User user) {
        userRepository.save(user);
    }


    public User findByVerificationToken(String verificationToken) {
        return userRepository.findUserByVerificationToken(verificationToken)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
    }


    public UUID authenticate(UserLoginRequest userLoginRequest) {
        User user = userRepository.findUserByEmail(userLoginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Vérifiez si le mot de passe est correct
        if (!BCrypt.checkpw(userLoginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        } else if (!user.isEmailVerified()) {
            throw new RuntimeException("User email is not verified");
        }

        // Construisez la réponse utilisateur
        return user.getId();
    }

    public List<UserResponse> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToUserResponse).toList();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .isEmailVerified(user.isEmailVerified())
                .verificationToken(user.getVerificationToken())
                .build();
    }

    public ResponseEntity<UserResponse> getUserById(UUID id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        // Transformation de l'entité User en UserResponse (DTO)
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .birthDate(user.getBirthDate())
                .isEmailVerified(user.isEmailVerified())
                .verificationToken(user.getVerificationToken())
                .build();
        return ResponseEntity.ok(userResponse);
    }

    public boolean isUser(UUID id) {
        return userRepository.findById(id).isPresent();
    }
}
