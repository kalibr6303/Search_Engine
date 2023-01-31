package searchengine.services;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.sql.DriverManager;
import java.sql.SQLException;


public class ThreadLink extends Thread {

    private String url;
    private String name;
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    private StatisticsServiceImpl statisticsServiceIml;
    private static java.sql.Connection connection;
    private String url1 = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    private String user = "root";
    private String pass = "katiaNIK18";



    public ThreadLink(String url, String name, PageRepository pageRepository, SiteRepository
            siteRepository, StatisticsServiceImpl statisticsServiceIml) {

        this.url = url;
        this.name = name;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;

        this.statisticsServiceIml = statisticsServiceIml;

    }

    @Override
    public void run() {

        try {
            connection = DriverManager.getConnection(url1, user, pass);
            Site site = new Site();
            site.setUrl(url);
            site.setName(name);
            site.setStatus(StatusType.valueOf("INDEXING"));
            siteRepository.save(site);
            new FindLink(connection, site, url, pageRepository, siteRepository).linkRun();
            ThreadLinkService.setIs(false);


        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }



}
