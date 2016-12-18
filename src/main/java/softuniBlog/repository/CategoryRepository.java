package softuniBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softuniBlog.entity.Category;

/**
 * Created by nikijul on 3.12.2016 г..
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
