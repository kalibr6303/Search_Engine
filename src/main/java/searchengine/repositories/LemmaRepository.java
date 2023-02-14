package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findBySiteId(Site site);
    Lemma findLemmaById(Integer id);
}