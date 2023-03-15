package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;


@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findLemmaById(Integer id);

    @Modifying
    @Query(nativeQuery = true, value = "insert into Lemma (lemma, site_id, frequency) values(:lemma, :site, 1)" +
            "ON DUPLICATE KEY UPDATE `frequency` = `frequency` + 1")
            void insertLemma(@Param("lemma") String lemma, @Param("site") String site);


    @Query(nativeQuery = true, value = "SELECT id FROM lemma WHERE lemma=?1  and site_id =?2")
    int getLemmaId(String lemma, String site);

    Lemma findLemmaByLemmaAndSite(String lemma, Site site);

    List<Lemma> findLemmasBySite(Site site);

}