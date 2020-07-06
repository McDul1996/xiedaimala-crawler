package com.github.hcsp.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {

    private CrawlerDao dao = new MyBatisCrawlerDao();

    public void run() throws SQLException, IOException {
        String link;
        while ((link = dao.getNextLinkThenDelete()) != null) {
            if (isNotProcessedAndIsInterestingLink(link)) {
                updateDatabaseAndInsertNewsIntoDatabase(link);
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }


    private void updateDatabaseAndInsertNewsIntoDatabase(String link) throws IOException, SQLException {
        System.out.println(link);
        Document doc = httpGetAndParseHtml(link);
        insertIntoLinkPoor(doc);
        putNewsIntoDatabase(doc, link);
        dao.insertAlreadyProcessed(link);
    }

    private boolean isNotProcessedAndIsInterestingLink(String link) throws SQLException {
        return !dao.isAlreadyProcessed(link) && isInterestingLink(link);
    }

    private void insertIntoLinkPoor(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (!href.toLowerCase().startsWith("javascript") && !href.startsWith("#")) {
                dao.insertLinkToBeProcessed(href);
            }
        }
    }

    private void putNewsIntoDatabase(Document doc, String link) throws SQLException {
        ArrayList<Element> articles = doc.select("article");
        if (!articles.isEmpty()) {
            for (Element article : articles) {
                System.out.println(article.select("h1").get(0).text());
                String title = articles.get(0).child(0).text();
                String content = article.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        return Jsoup.connect(link).get();
    }

    private static boolean isInterestingLink(String link) {
        return isIndexPage(link) && isNewsPage(link) && isNotLoginPage(link);
    }

    private static boolean isIndexPage(String link) {
        return link.contains("sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
