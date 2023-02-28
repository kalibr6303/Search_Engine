package searchengine.sql;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.morphology.Morphology;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.*;
import java.util.*;


@Component
@RequiredArgsConstructor
public class LemmaWriter implements Lemma{
    private  final Morphology morphology;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;



    public  int addOfBaseLemma(Site site, String lemma) throws SQLException, IOException, InterruptedException {

            String siteId = String.valueOf(site.getId());
            lemmaRepository.insertLemma(lemma, siteId);
        return lemmaRepository.getLemmaId(lemma, siteId);

    }


    @Transactional
    public   void writeLemmaToBase(String content, Site site, Page page) throws  InterruptedException {
        if (!Thread.interrupted()) {
            HashMap<String, Integer> storage = morphology.getLemmaList(content);
            if (storage.size() != 0) {
                storage.entrySet().forEach(l ->{
                    int lemmaId = 0;
                    try {
                        lemmaId = addOfBaseLemma(site, l.getKey());
                    } catch (SQLException  | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    indexRepository.insertIndexInBase(lemmaId, page.getId(), l.getValue() );
                });
            }
        } else {
            throw new InterruptedException();
        }
    }
}
