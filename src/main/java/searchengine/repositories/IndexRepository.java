package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    Lemma findIndexByPage(Page page);
    Index findIndexByLemma(Lemma lemma);
}