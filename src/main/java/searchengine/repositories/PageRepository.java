package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;


@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findByPath(String path);

}
