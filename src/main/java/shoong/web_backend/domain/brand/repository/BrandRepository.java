package shoong.web_backend.domain.brand.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.brand.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
