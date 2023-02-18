package searchengine.developer;

import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.sql.SQLException;

public interface Lemma {
    void writeLemmaToBase(String content, Site site, Page page) throws SQLException, IOException;
}
