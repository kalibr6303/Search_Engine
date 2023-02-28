package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;


@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Query(nativeQuery = true, value =  "INSERT INTO indexed(lemma_id, page_id, `ranks`) " +
            "VALUES(:lemma, :page, :ranks)")
    void insertIndexInBase(@Param("lemma") int lemma, @Param("page") int page, @Param("ranks") int ranks);

    List<Index> getIndexByLemma(Lemma lemma);

    Index getIndexByLemmaAndPage(Lemma lemma, Page page);
}