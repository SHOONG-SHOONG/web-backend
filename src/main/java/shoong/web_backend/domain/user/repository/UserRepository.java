package shoong.web_backend.domain.user.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}