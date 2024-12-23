package tech.investbuddy.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.investbuddy.userservice.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByVerificationToken(String verificationToken);
}
