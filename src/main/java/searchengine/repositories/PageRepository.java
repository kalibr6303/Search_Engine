package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;


@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findByPath(String path);
    Page findById(int page);
    List<Page> findBySite(Site site);

}
