package searchengine.developer;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.PageDto;
import searchengine.util.UserAgent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Slf4j

public class PageUrlFound extends RecursiveTask<List<PageDto>> {

    private String url;
    private List<String> urlList;
    private List<PageDto> pageDtoList;
    private static String agent;
    private static String referrer;

    static {
        try {
            agent = UserAgent.getAgent();
            referrer = UserAgent.getReferrer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public PageUrlFound(String url, List<PageDto> pageDtoList, List<String> urlList)  {
        this.url = url;
        this.pageDtoList = pageDtoList;
        this.urlList = urlList;
    }

    public PageUrlFound()  {
    }

    @Override
    protected List<PageDto> compute() {
        try {
            Thread.sleep(150);
            Document doc = getConnect(url);
            String html = doc.html();
            int status = doc.connection().response().statusCode();
            PageDto pageDto = new PageDto(url, html, status);
            pageDtoList.add(pageDto);
            Elements elements = doc.select("a");
            List<PageUrlFound> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");
                if (link.startsWith(el.baseUri()) &&
                        !link.equals(el.baseUri()) &&
                        !link.contains("#") &&
                        !link.contains(".pdf") &&
                        !link.contains(".jpg") &&
                        !link.contains(".JPG") &&
                        !link.contains(".png") &&
                        !urlList.contains(link)) {
                    urlList.add(link);
                    PageUrlFound task = new PageUrlFound(link, pageDtoList, urlList);
                    task.fork();
                    taskList.add(task);
                }
            }
            taskList.forEach(ForkJoinTask::join);

        } catch (Exception e) {
            log.debug("Ошибка парсинга - " + url);
            PageDto pageDto = new PageDto(url, "", 500);
            pageDtoList.add(pageDto);
        }

        return pageDtoList;
    }


    public List<PageDto> getOnePageUrlFound(String url) {
        List<PageDto> listPageDto = new ArrayList<>();
        Document doc = getConnect(url);
        String html = doc.html();
        int status = doc.connection().response().statusCode();
        PageDto pageDto = new PageDto(url, html, status);
        listPageDto.add(pageDto);
        return listPageDto;
    }


    private Document getConnect(String url) {
        Document doc = null;
        try {
            Thread.sleep(150);
            doc = Jsoup.connect(url).userAgent(agent).referrer(referrer).get();

        } catch (Exception e) {
            log.debug("Не удалось установить подключение с " + url);
        }
        return doc;
    }
}