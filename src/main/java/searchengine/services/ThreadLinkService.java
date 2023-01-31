package searchengine.services;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.FoundResponse;
import searchengine.dto.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class ThreadLinkService {
    private static StatisticsServiceImpl statisticsServiceIml;

    private static PageRepository pageRepository;
    private static SiteRepository siteRepository;
    public static Boolean is = false;
    public static SitesList sitesList;


    private static searchengine.model.Site siteUrl;
    private static java.sql.Connection connection;
    private static String url1 = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    private  static String user = "root";
    private static String pass = "katiaNIK18";



    public ThreadLinkService(SitesList sitesList, PageRepository pageRepository,
                             SiteRepository siteRepository,
                             StatisticsServiceImpl statisticsServiceIml) {
        this.sitesList = sitesList;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;

        this.statisticsServiceIml = statisticsServiceIml;
    }



    public List<ThreadLink> getThreadList() {
        List<ThreadLink> storage = new ArrayList<>();
        List<Site> sites = sitesList.getSites();


        sites.forEach(s -> {
            ThreadLink threadLink = new ThreadLink(s.getUrl(), s.getName(), pageRepository,
                    siteRepository, statisticsServiceIml);
            storage.add(threadLink);
        });
        return storage;

    }


    public IndexingResponse startIndexing2() throws SQLException, InterruptedException {
        IndexingResponse indexingResponse = new IndexingResponse();
        List<String> sites = statisticsServiceIml.getSiteList();
        if (is == true) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
            return indexingResponse;
        }
        setIs(true);
        indexingResponse.setResult(true);

        if (isAliveTread()) {
            siteRepository.deleteAll();
        }
        FindLink.setIndexStopFlag(true);
        getThreadList().forEach(t -> t.start());

        return indexingResponse;
    }


    public static void setIs(Boolean is) {
        ThreadLinkService.is = is;
    }


    public  static IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        FindLink.setIndexStopFlag(false);
        setStatusSite();
        indexingResponse.setResult(true);
        setIs(false);



        return indexingResponse;
    }

    public static FoundResponse getRequestLemma(String word, String site) throws IOException, SQLException {
        connection= DriverManager.getConnection(url1, user, pass);
        FoundResponse foundResponse = new FoundResponse();
        RequestLemmas requestLemmas = new RequestLemmas();
        List<String> storage = requestLemmas.getLinkString(word);
        List<String> lemmaRequest = requestLemmas.requestLemmaList(storage);
        List<Integer> list = requestLemmas.getRequestList(requestLemmas.requestLemmaList(storage),site, connection);
        List<Integer> lemmas = requestLemmas.getListLemmasRequest(list, connection);
        HashMap<Integer, Float> filtrLemmas = requestLemmas.getFilteredPage(lemmas, list, lemmaRequest, connection);
        foundResponse = requestLemmas.getRequestResponse(filtrLemmas, lemmaRequest,foundResponse, connection);
        connection.close();
        return  foundResponse;


    }


    public static IndexingResponse addUpdate(String url) throws IOException, SQLException {
        connection = DriverManager.getConnection(url1, user, pass);
        IndexingResponse indexingResponse = new IndexingResponse();
        Page pageFound = FindLink.foundPage(url, pageRepository);
        if (FindLink.getUrlWebsite(url, sitesList) == null) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        searchengine.model.Site siteFound = null;
        if (pageFound != null) {
            siteFound = pageFound.getSite();
            FindLink.deletePageFromBase(pageFound.getId(), pageRepository);
        }
        new FindLink(connection, siteFound, url, pageRepository, siteRepository).getOneLink(url);
        indexingResponse.setResult(true);
        connection.close();
        return indexingResponse;
    }


    public static int getMaxIdLemma() throws SQLException {
        connection = DriverManager.getConnection(url1, user, pass);
        String sql = "SELECT MAX(id) FROM lemma";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (!resultSet.next()) return -1;
        int id = resultSet.getInt(1);
        resultSet.close();
        return id;
    }

    public static Boolean isAliveTread() throws InterruptedException, SQLException {
        int id = getMaxIdLemma();

        while (true){
            Thread.sleep(20000);
            int idNext = getMaxIdLemma();
            if (id == idNext) break;
            else id = idNext;
        }
        return  true;
    }

    public static void setStatusSite (){
        Iterable<searchengine.model.Site> iterableSite = siteRepository.findAll();
        for (searchengine.model.Site s : iterableSite) {
            s.setStatus(StatusType.valueOf("FAILED"));
            s.setLastError("Индексация прервана пользователем");
            siteRepository.save(s);
        }
    }

}