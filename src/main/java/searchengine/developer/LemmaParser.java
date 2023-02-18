package searchengine.developer;


import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======

>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47
import org.springframework.stereotype.Component;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.morphology.Morphology;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

<<<<<<< HEAD
@Component
@RequiredArgsConstructor
public class LemmaParser implements Lemma{
    private  final Morphology morphology;
    private final ConnectionSql connectionSql;



    public  synchronized  int addOfBaseLemma(Site site, String lemma) throws SQLException {
=======

@Component
@RequiredArgsConstructor
public class LemmaParser implements Lemma {
    private   final Morphology morphology;
    private  final ConnectionSql connectionSql = new ConnectionSql();



    public synchronized  int addOfBaseLemma(Site site, String lemma) throws SQLException {
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47

        String siteId = String.valueOf(site.getId());
        String sql = "INSERT INTO lemma(lemma, site_id, `frequency`) VALUE('" + lemma + "', '" + siteId + "', 1) " +
                "ON DUPLICATE KEY UPDATE `frequency` = `frequency` + 1";
        connectionSql.getConnection().createStatement().execute(sql);

        String sql2 = "SELECT id FROM lemma WHERE lemma ='" + lemma + "' and site_id ='" + siteId + "'";
        Statement statement = connectionSql.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery(sql2);
        if (!resultSet.next()) return -1;
        else {
            int idLemma = resultSet.getInt("id");
            resultSet.close();
            return idLemma;
        }
    }


<<<<<<< HEAD
    public void executeMultiInsert(String count, StringBuilder stringBuilder) throws SQLException {
=======
    public  void executeMultiInsert(String count, StringBuilder stringBuilder) throws SQLException {
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47
        String sql = "INSERT INTO indexed(lemma_id, page_id, `ranks`) " +
                "VALUES" + stringBuilder.toString() +
                "ON DUPLICATE KEY UPDATE `ranks`=`ranks`+ '" + count + "'";
        connectionSql.getConnection().createStatement().execute(sql);

    }

<<<<<<< HEAD
    public   void writeLemmaToBase(String content, Site site, Page page) throws SQLException {
=======
    public void writeLemmaToBase(String content, Site site, Page page) throws SQLException {
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47

        AtomicReference<Integer> ranks = new AtomicReference<>(0);
        HashMap<String, Integer> storage = morphology.getLemmaList(content);
        StringBuilder stringBuilderIndex = new StringBuilder();
        Set<String> listLemmas = storage.keySet();

        if (listLemmas.size() != 0) {
            listLemmas.forEach(s -> {
                int lemmaId = 0;
                try {

                    lemmaId = addOfBaseLemma(site, s);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                ranks.set(storage.get(s));
                boolean isStart = stringBuilderIndex.length() == 0;
                stringBuilderIndex.append((isStart ? "" : ",") + "('" + lemmaId + "', '" + page.getId() + "', '" + ranks + "')");
            });
            executeMultiInsert(String.valueOf(ranks), stringBuilderIndex);
        }
    }
}
