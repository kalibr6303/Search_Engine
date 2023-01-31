package searchengine.services;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {


    private static String url1 = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    private static String user = "root";
    private static String pass = "katiaNIK18";
    private static java.sql.Connection connection;
    private final Random random = new Random();
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SitesList sites;
    String[] statuses = {"INDEXED", "FAILED", "INDEXING"};

    public static int getCountLemma(String name) throws SQLException {
        int a = 0;
        int b = 0;
        connection = DriverManager.getConnection(url1, user, pass);
        String sql2 = "SELECT id FROM site WHERE name ='" + name + "'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql2);
        if (resultSet.next()) a = resultSet.getInt("id");
        String sql = "SELECT id FROM lemma WHERE site_id ='" + a + "'";
        resultSet = statement.executeQuery(sql);
        while (resultSet.next()) b++;
        resultSet.close();
        return b;
    }

    @SneakyThrows
    @Override
    public StatisticsResponse getStatistics() {

        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = getCountPages(site.getUrl());
            int lemmas = getCountLemma(site.getName());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(getStatusPages(sitesList.get(i).getUrl()));
            item.setError(getErrorPages(sitesList.get(i).getUrl()));
            item.setStatusTime(getDataTime(sitesList.get(i).getName()));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    public List<String> getSiteList() {
        List<String> listUrl = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        sitesList.forEach(s -> listUrl.add(s.getUrl()));
        return listUrl;
    }

    public int getCountPages(String url) {
        int count = 0;
        if (pageRepository == null) return 0;
        Iterable<searchengine.model.Page> pagesIterable = pageRepository.findAll();
        for (searchengine.model.Page p : pagesIterable) {
            if (p.getSite().getUrl().equals(url)) count++;
        }
        return count;
    }

    public String getStatusPages(String url) {

        Iterable<searchengine.model.Site> sitesIterable = siteRepository.findAll();
        for (searchengine.model.Site s : sitesIterable) {
            if (s.getUrl().equals(url)) return String.valueOf(s.getStatus());
        }
        return null;
    }

    public String getErrorPages(String url) {
        Iterable<searchengine.model.Site> siteIterable = siteRepository.findAll();
        for (searchengine.model.Site s : siteIterable) {
            if (s.getUrl().equals(url)) return String.valueOf(s.getLastError());
        }
        return null;
    }

    public String getDataTime(String name)  {
        Iterable<searchengine.model.Site> siteIterable = siteRepository.findAll();
        for (searchengine.model.Site s : siteIterable) {
            if (s.getName().equals(name)) return String.valueOf(s.getStatusTime());
        }
        return null;
    }
}



