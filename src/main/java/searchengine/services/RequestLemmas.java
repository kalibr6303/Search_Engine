package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.DataResponse;
import searchengine.dto.FoundResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLemmas extends LemmaOfLink {
    private LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static List<DataResponse> data = new ArrayList<>();


    //Список лемм, соответствующей запросу
    public RequestLemmas() throws IOException {
    }

    public List<String> requestLemmaList(List<String> storage) throws IOException {
        List<String> listForms = new ArrayList<>();
        for (String s : storage) {
            if (!isContainsServicePartSpeech(s)) {
                List<String> forms = luceneMorphology.getNormalForms(s);
                forms.forEach(f -> listForms.add(f));
            }
        }

        return listForms;
    }

    //получаем список лемм, которые нашлись в базе
    public List<Integer> getRequestList(List<String> request, String url, java.sql.Connection connection) throws SQLException {
        HashMap<Integer, Integer> lemmaFrequency = new HashMap<>();

        int a = 0;
        if (url != null) {
            String sql1 = "SELECT id FROM site WHERE url='" + url + "'";
            int d = getColumnOfBase(sql1, "id", connection);
            request.forEach(s -> {
                String sql = "SELECT * FROM lemma WHERE lemma ='" + s + "' and site_id= '" + d + "'";
                try {
                    int idLemma = getColumnOfBase(sql, "id", connection);
                    int frequencyLemma = getColumnOfBase(sql, "frequency", connection);
                    lemmaFrequency.put(idLemma, frequencyLemma);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            request.forEach(s -> {
                String sql = "SELECT * FROM lemma WHERE lemma ='" + s + "'";
                try {
                    int idLemma = getColumnOfBase(sql, "id", connection);
                    int frequencyLemma = getColumnOfBase(sql, "frequency", connection);
                    lemmaFrequency.put(idLemma, frequencyLemma);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        if (a == request.size()) return null;

        List<Integer> idLemmasSort = new ArrayList<>();
        lemmaFrequency.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(s -> idLemmasSort.add(s.getKey()));
        return idLemmasSort;

    }

    //Получаем список страниц соответствующей последней(редкой) лемме
    public List<Integer> getListLemmasRequest(List<Integer> lemmas, java.sql.Connection connection) throws SQLException {

        List<Integer> idOneOfPage = new ArrayList<>();
        int lemmaId = 0;
        if (lemmas != null) lemmaId = lemmas.get(0);
        Statement statement = connection.createStatement();
        String qsl2 = "SELECT page_id FROM indexed where lemma_id='" + lemmaId + "'";
        ResultSet resultSet = statement.executeQuery(qsl2);
        while (resultSet.next()) {
            idOneOfPage.add(resultSet.getInt("page_id"));
        }
        resultSet.close();


        return idOneOfPage;
    }

    //получаем урезанный список в котором есть все страницы с входящмимм в них леммами
    public HashMap<Integer, Float> getFilteredPage(List<Integer> pages, List<Integer> lemmas, List<String> lemmasRequest, java.sql.Connection connection) {

        if (lemmas == null || lemmas.size() != lemmasRequest.size()) return null;
        HashMap<Integer, Float> pageRanks = new HashMap<>();
        lemmas.forEach(l -> {
            pages.forEach(p -> {
                try {
                    if (pageRanks.containsKey(p) && isOnIndex(p, l, connection) != 0)
                        pageRanks.put(p, pageRanks.get(p) + isOnIndex(p, l, connection));
                    if (!pageRanks.containsKey(p) && isOnIndex(p, l, connection) != 0)
                        pageRanks.put(p, isOnIndex(p, l, connection));
                    if (pageRanks.containsKey(p) && isOnIndex(p, l, connection) == 0) pageRanks.remove(p);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        });

        if (pageRanks.size() == 0) return null;
        Float maxEntry = pageRanks.entrySet()
                .stream().max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null).getValue();
        pageRanks.entrySet().stream().forEach(s -> pageRanks.put(s.getKey(), s.getValue() / maxEntry));

        return pageRanks;
    }


    public float isOnIndex(int page, int lemma, java.sql.Connection connection) throws SQLException {

        Statement statement = connection.createStatement();
        ResultSet resultSet;
        String qsl = "SELECT * FROM indexed where lemma_id= '" + lemma + "' and page_id= '" + page + "'";
        resultSet = statement.executeQuery(qsl);
        if (!resultSet.next()) return 0;
        return resultSet.getFloat("ranks");

    }


    public FoundResponse getRequestResponse(HashMap<Integer, Float> pageRank, List<String> listForms, FoundResponse foundResponse, java.sql.Connection connection) throws SQLException {
        if (pageRank == null) {
            foundResponse.setResult(false);
            foundResponse.setError("Страница не найдена");
        } else {
            pageRank.entrySet().stream().forEach(System.out::println);
            pageRank.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Float>comparingByValue()
                            .reversed())
                    .forEach(s -> {
                        Statement statement = null;
                        DataResponse dataResponse = new DataResponse();
                        dataResponse.setRelevance(s.getValue());
                        List<String> snippet = null;
                        try {
                            statement = connection.createStatement();
                            snippet = getListLemmasOfContent(getSnippetOfContent(s.getKey(), connection), listForms);

                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }
                        if (snippet.size() != 0) dataResponse.setSnippet(snippet.get(0));
                        else dataResponse.setSnippet("NOT FOUND SNIPPET OF TEXT");

                        String qsl = "SELECT * FROM page WHERE id = '" + s.getKey() + "'";
                        ResultSet resultSet = null;

                        try {
                            resultSet = statement.executeQuery(qsl);


                            while (true) {

                                if (!resultSet.next()) break;
                                dataResponse.setUri(resultSet.getString("path"));
                                dataResponse.setTitle(getTitleOfPage(resultSet.getString("content")));
                                dataResponse.setSiteName(getSiteNameById(resultSet.getInt("site_id"), "name", connection));
                                dataResponse.setSite(getSiteNameById(resultSet.getInt("site_id"), "url", connection));
                                data.add(dataResponse);
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    });
            foundResponse.setData(data);
            foundResponse.setCount(pageRank.size());
            data.forEach(System.out::println);
            foundResponse.setResult(true);
        }
        return foundResponse;
    }


    public String getSiteNameById(int id, String name, java.sql.Connection connection) throws SQLException {

        Statement statement = connection.createStatement();
        String qsl = "SELECT * FROM site where id = '" + id + "'";
        ResultSet resultSet = statement.executeQuery(qsl);
        if (resultSet.next()) return resultSet.getString(name);
        return null;

    }

    public String getTitleOfPage(String text) {
        String regex = "<title>[\\W\\w\\s]+</title>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String titleResult = null;
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String title = text.substring(start, end);
            titleResult = title.replaceAll("[^а-яА-Я\\s]+", " ");
        }
        return titleResult;

    }


    public List<String> getSnippetOfContent(Integer pageRank, java.sql.Connection connection) throws SQLException {

        Statement statement = connection.createStatement();
        List<String> storage = new ArrayList<>();
        String qsl = "SELECT content FROM page where id='" + pageRank + "'";
        String content = null;
        ResultSet resultSet = statement.executeQuery(qsl);
        if (resultSet.next()) content = resultSet.getString("content");

       Document document = Jsoup.parse(content);
        List<String> nameTag = Arrays.asList("p", "h1", "h2", "h3", "title", "h4", "h5", "abbr", "bdo",
                "q", "cite","br", "hr", "code", "kbd", "samp", "var", "pre", "a");
        nameTag.forEach(s -> {
            Elements elements = document.select(s);
            elements.forEach(e -> {
                storage.add(String.valueOf(e));
            });
        });
        return storage;
    }


    public List<String> getListLemmasOfContent(List<String> listOfTeg, List<String> requestLemma) throws IOException {
        HashMap<String, Integer> listSnippet = new HashMap<>();
        List<String> snippets = new ArrayList<>();
        listOfTeg.forEach(t -> {

            HashMap<String, String> lemmasOfContent = new HashMap<>();
            List<String> list;
            list = getLinkString(t);


            list.forEach(s -> {
                try {
                    if (!isContainsServicePartSpeech(s) && !s.matches("[а-яА-Я]")) {
                        List<String> forms = luceneMorphology.getNormalForms(s);
                        forms.forEach(d -> {
                            lemmasOfContent.put(d, s);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            HashMap<String, Integer> ddd = new HashMap<>();
            ddd = addBoltFontLemma(t, requestLemma);
            ddd.forEach((k, v) -> listSnippet.putIfAbsent(k, v));

        });
        listSnippet.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()).forEach(a -> snippets.add(a.getKey()));

        return snippets;
    }


    public HashMap<String, Integer> addBoltFontLemma(String teg, List<String> word) {
        HashMap<String, Integer> resultTeg = new HashMap<>();
        List<String> linkString;
        String[] list = teg.split("\\s+");
        linkString = Arrays.asList(list);
        HashMap<String, Integer> resultNext = new HashMap<>();
        final int[] a = {0};
        linkString.forEach(l -> {
            String wordToLemma = l.replaceAll("[^а-яА-Я]+", "");
            String wordToLemmaNext = wordToLemma.toLowerCase();
            if (wordToLemmaNext.matches("[а-я]+")) {
                List<String> forms = luceneMorphology.getNormalForms(wordToLemmaNext);
                forms.forEach(d -> {
                    word.forEach(f -> {
                        if (d.equals(f)) {
                            if (!resultNext.containsKey(l)) {
                                resultNext.put(l, a[0]);
                                a[0]++;
                            }
                        }
                    });
                });
            }
        });
        String tegResult = null;
        for (Map.Entry<String, Integer> s : resultNext.entrySet()) {
            tegResult = teg.replaceAll(s.getKey(), "<b>" + s.getKey() + "</b>");
            teg = tegResult;
        }
        String tegResult1 = null;
        if (tegResult != null) {
            tegResult1 = tegResult.replaceAll("</b> <b>", " ");
        }
        if (a[0] != 0) resultTeg.put(tegResult1, a[0]);
        return resultTeg;
    }

    public int getColumnOfBase(String qsl, String column, java.sql.Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(qsl);
        if (resultSet.next()) {
            int s = resultSet.getInt(column);
            resultSet.close();
            return s;
        }
        return 0;
    }

}
