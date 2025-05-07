package shoong.web_backend.domain.user.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByBrandBrandId(Long brandId);
}