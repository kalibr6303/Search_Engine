package searchengine.services;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import static searchengine.services.ThreadLinkService.sitesList;


public class FindLink extends RecursiveTask<List<String>> {


    public  String url;
    private  Site site;
    public static PageRepository pageRepository;
    private SiteRepository siteRepository;
    public static Boolean indexStopFlag = true;
    private static java.sql.Connection connection;





    public FindLink(java.sql.Connection connection, Site site, String url, PageRepository pageRepository, SiteRepository siteRepository) {
        this.connection = connection;
        this.site = site;
        this.url = url;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;

    }



    public void linkRun() throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool();
        FindLink href = new FindLink(connection, site, url + "/", pageRepository, siteRepository);
        pool.invoke(href);

        if (indexStopFlag) setStatusSite();
        pool.shutdownNow();
    }


    @SneakyThrows
    @Override
    public List<String> compute() {
        List<String> links = new ArrayList<>();
        ArrayList<FindLink> tasks = new ArrayList<>();
        Document doc = getOneLink(url);
        Elements elements = doc.select("a[href]");
        for (Element element : elements) {
            String lin = element.attr("abs:href");
            if (lin.matches(url + "([^:#]+[^.JPG])|([^:#]+[^.png])|([^:#]+[^.jpg])") &&
                    customSelect(lin, site, connection) == -1) {
                String s1 = lin.replaceAll(site.getUrl(), "");
                Page page = new Page();
                page.setPath(s1);
                page.setSite(site);
                pageRepository.save(page);
                links.add(lin);
            }

        }
        for (int i = 0; i < links.size(); i++) {
            FindLink task = new FindLink(connection, site, links.get(i), pageRepository, siteRepository);
            task.fork();
            tasks.add(task);
        }

        for (FindLink task : tasks) {
            if (task.join() != null)
                links.addAll(task.join());
        }
        return links;
    }


    public Document getOneLink(String url) throws IOException, SQLException {

        Connection.Response res ;
        if (indexStopFlag) {
            res = Jsoup.connect(url).
                    userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0")
                    .ignoreHttpErrors(true).followRedirects(true).timeout(100000).execute();
        } else return null;

        Document  doc = res.parse();
        int status = res.statusCode();

        if (customSelect(url, site, connection) == -1) {
            String s = url.replaceAll(site.getUrl(), "");
            Page page = new Page();
            if (status == 503) {
                site.setLastError("Главная страница сайта недоступна");
                site.setStatus(StatusType.valueOf("FAILED"));
                siteRepository.save(site);
            }
            page.setSite(site);
            page.setPath(s);
            page.setCode(status);
            site.setLastError("NONE");
            setStatusTimeInSite();
            siteRepository.save(site);
            page.setContent(doc.html());
            pageRepository.save(page);
            LemmaOfLink lemmaOfLink = new LemmaOfLink(page);
           // String f = getElementTag(doc);
            String f = doc.html();
            lemmaOfLink.getLemma1(lemmaOfLink.getLemma(f), site, connection);

        } else {

            int number = customSelect(url, site, connection);
            Optional<Page> optionalPage = pageRepository.findById(number);
            optionalPage.get().setCode(status);
            optionalPage.get().setContent(doc.html());
            pageRepository.save(optionalPage.get());
            site.setLastError("NONE");
            setStatusTimeInSite();
            siteRepository.save(site);
            LemmaOfLink lemmaOfLink = new LemmaOfLink(optionalPage.get());
            //String f = getElementTag(doc);
            String f = doc.html();
            lemmaOfLink.getLemma1(lemmaOfLink.getLemma(f), site, connection);

        }
        return doc;
    }



    public static int customSelect(String website, Site site, java.sql.Connection connection) throws SQLException {
        String www = website.replaceAll(site.getUrl(), "");
        String sql = "SELECT id FROM page WHERE path ='" + www + "'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (!resultSet.next()) return -1;
        else {
            return resultSet.getInt("id");
        }
    }

    /*
    public String  getElementTag(Document document) {
        List<String> nameTag = Arrays.asList("p", "h1", "h2", "h3", "title", "h4");
        final String[] result = {null};
        nameTag.forEach(s -> {
            Elements elements = document.getElementsByTag(s);
            elements.forEach(e -> {
                result[0] = result[0] + " " + e.text();
            });
        });
        return result[0];
    }
     */

    public void setStatusSite() {
        Iterable<Site> siteIterable = siteRepository.findAll();
        for (Site s : siteIterable) {
            if (FindLink.getIndexStopFlag() == true) {
                s.setStatus(StatusType.valueOf("INDEXED"));
                siteRepository.save(s);
            }
        }
    }

    public void setStatusTimeInSite() {
        LocalDateTime dateTime = LocalDateTime.now();
        site.setStatusTime(dateTime);
    }

    public static Boolean getIndexStopFlag() {
        return indexStopFlag;
    }

    public static String getUrlWebsite(String url, SitesList sitesList) {
        List<searchengine.config.Site> sites = sitesList.getSites();
        for (searchengine.config.Site s : sites) {
            if (url.matches(s.getUrl() + "[^:#]+")) return s.getUrl();
        }
        return null;
    }

    public static Page foundPage(String url, PageRepository pageRepository) {
        String d = "";
        if (getUrlWebsite(url, sitesList) != null) {
            d = url.replaceAll(getUrlWebsite(url, sitesList), "");
            System.out.println("Объект" + " " + d);

            Iterable<Page> pages = pageRepository.findAll();
            for (Page p : pages) {
                if (p.getPath().equals(d)) return p;
            }
        }

        return null;
    }


    public  static void deletePageFromBase(int id, PageRepository pageRepository) {

        Optional<Page> optionalFolder = pageRepository.findById(id);
        pageRepository.delete(optionalFolder.get());
    }

    public static void setIndexStopFlag(Boolean indexStopFlag) {

        FindLink.indexStopFlag = indexStopFlag;
    }


}