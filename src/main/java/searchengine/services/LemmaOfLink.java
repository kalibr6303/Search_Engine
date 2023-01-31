package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Page;
import searchengine.model.Site;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class LemmaOfLink {
    LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    public Site site;
    public searchengine.model.Page page;


    public LemmaOfLink(Page page) throws IOException {
        this.page = page;
    }

    public LemmaOfLink() throws IOException {
    }

    public Boolean isContainsServicePartSpeech(String word) throws IOException {
        String word1 = word.trim();
        if (!word1.matches("[а-яА-Я]+")) return true;
        List<String> wordBaseForms = luceneMorphology.getMorphInfo(word1);
        for (String l : wordBaseForms) {
            if (l.matches("([\\W\\w]+ПРЕДЛ)|([\\W\\w]+МЕЖД)|" +
                    "([\\w\\w]+)|([\\W\\w]+СОЮЗ)")) return true;
        }
        return false;
    }




    public static List<String> getLinkString(String word) {

        String wordResult = word.replaceAll("[^а-яА-Я\\s]+", " ");
        String wordResultNext = wordResult.toLowerCase();
        List<String> linkString;
        String[] list = wordResultNext.split("\\s+");
        linkString = Arrays.asList(list);
        return linkString;
    }


    public synchronized static int addOfBaseLemma(Site site, String lemma, java.sql.Connection connection) throws SQLException {

        String siteId = String.valueOf(site.getId());

        String sql = "INSERT INTO lemma(lemma, site_id, `frequency`) VALUE('" + lemma + "', '" + siteId + "', 1) " +
                "ON DUPLICATE KEY UPDATE `frequency` = `frequency` + 1";
        connection.createStatement().execute(sql);

        String sql2 = "SELECT id FROM lemma WHERE lemma ='" + lemma + "' and site_id ='" + siteId + "'";
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(sql2);
        if (!resultSet.next()) return -1;
        else {
            int s = resultSet.getInt("id");
            resultSet.close();
            return s;
        }
    }


    public static void executeMultiInsert(String count, StringBuilder stringBuilder, java.sql.Connection connection) throws SQLException {
        String sql = "INSERT INTO indexed(lemma_id, page_id, `ranks`) " +
                "VALUES" + stringBuilder.toString() +
                "ON DUPLICATE KEY UPDATE `ranks`=`ranks`+ '" + count + "'";
        connection.createStatement().execute(sql);


    }

    public HashMap<String, Integer> getLemma(String word) throws IOException {
        HashMap<String, Integer> storageString = new HashMap<>();
        Integer count = 1;
        getLinkString(word).forEach(s -> {
            try {
                if (!isContainsServicePartSpeech(s) && !s.matches("[а-яА-Я]")) {
                    List<String> forms = luceneMorphology.getNormalForms(s);
                    forms.forEach(d -> {
                        if (storageString.containsKey(d)) {
                            storageString.put(d, storageString.get(d) + 1);
                        } else storageString.put(d, count);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return storageString;
    }

    public void getLemma1(HashMap<String, Integer> storage, Site site, java.sql.Connection connection) throws SQLException {
        AtomicReference<Integer> ranks = new AtomicReference<>(0);
        StringBuilder stringBuilderIndex = new StringBuilder();
        Set<String> listLemmas = storage.keySet();
        if (listLemmas.size() != 0) {
            listLemmas.forEach(s -> {
                int lemmaId = 0;
                try {
                    lemmaId = addOfBaseLemma(site, s, connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ranks.set(storage.get(s));
                boolean isStart = stringBuilderIndex.length() == 0;
                stringBuilderIndex.append((isStart ? "" : ",") + "('" + lemmaId + "', '" + page.getId() + "', '" + ranks + "')");
            });
            executeMultiInsert(String.valueOf(ranks), stringBuilderIndex, connection);
        }
    }

}
