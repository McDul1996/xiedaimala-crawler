package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "zhang321088";

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> link = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                link.add(resultSet.getString(1));
            }
        }
        return link;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/C:/Users/Administrator/Desktop/xiedaimala-crawler/news", USER_NAME, PASSWORD);
        do {
            List<String> linkPoor = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");
            if (!linkPoor.isEmpty()) {
                String link = linkPoor.remove(linkPoor.size() - 1);
                executeSql(connection, link, "delete from LINKS_TO_BE_PROCESSED Where link = (?)");
                if (isNotProcessedAndIsInterestingLink(connection, link, "select link from LINKS_ALREADY_PROCESSED where link = (?)")) {
                    updateDatabaseAndInsertNewsIntoDatabase(connection, link);
                }
            }
        } while (true);
    }

    private static void updateDatabaseAndInsertNewsIntoDatabase(Connection connection, String link) throws IOException, SQLException {
        Document doc = httpGetAndParseHtml(link);
        insertIntoLinkPoor(connection, doc);
        putNewsIntoDatabase(connection, isNewsTitle(doc), "INSERT INTO news (TITLE) values (?)");
        executeSql(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) values (?)");
    }


    private static boolean isNotProcessedAndIsInterestingLink(Connection connection, String link, String sql) throws SQLException {
        return !isAlreadyProcessed(connection, link, sql) && isInterestingLink(link);
    }

    private static void insertIntoLinkPoor(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            executeSql(connection, href, "INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)");
        }
    }

    private static void putNewsIntoDatabase(Connection connection, Set<String> isNewsPage, String sql) throws SQLException {
        if (!(isNewsPage == null)) {
            for (String news : isNewsPage) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, news);
                    statement.executeUpdate();
                }
            }
        }
    }

    private static boolean isAlreadyProcessed(Connection connection, String link, String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (!resultSet.next()) {
                return false;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return true;
    }

    private static void executeSql(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static Set<String> isNewsTitle(Document doc) {
        Set<String> news = new HashSet<>();
        if (!doc.select("article").isEmpty()) {
            doc.select("article").stream().map(articleTag1 -> doc.select("article").get(0).child(0).text()).forEach(System.out::println);
            doc.select("article").stream().map(articleTag1 -> doc.select("article").get(0).child(0).text()).forEach(news::add);
            return news;
        }
        return null;
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
