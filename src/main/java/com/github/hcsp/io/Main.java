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
import java.util.stream.Collectors;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "zhang321088";

    private static String loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");
        executeSql(connection, link, "delete from LINKS_TO_BE_PROCESSED Where link = (?)");
        return link;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/C:/Users/Administrator/Desktop/xiedaimala-crawler/news", USER_NAME, PASSWORD);
        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            if (isNotProcessedAndIsInterestingLink(connection, link, "select link from LINKS_ALREADY_PROCESSED where link = (?)")) {
                updateDatabaseAndInsertNewsIntoDatabase(connection, link);
            }
        }
    }


    private static void updateDatabaseAndInsertNewsIntoDatabase(Connection connection, String link) throws IOException, SQLException {
        System.out.println(link);
        Document doc = httpGetAndParseHtml(link);
        insertIntoLinkPoor(connection, doc);
        putNewsIntoDatabase(connection, doc, link);
        executeSql(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) values (?)");
    }


    private static boolean isNotProcessedAndIsInterestingLink(Connection connection, String link, String sql) throws SQLException {
        return !isAlreadyProcessed(connection, link, sql) && isInterestingLink(link);
    }

    private static void insertIntoLinkPoor(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (!href.toLowerCase().startsWith("javascript") && !href.startsWith("#")) {
                executeSql(connection, href, "INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)");
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

    private static void putNewsIntoDatabase(Connection connection, Document doc, String link) throws SQLException {
        ArrayList<Element> articles = doc.select("article");
        if (!articles.isEmpty()) {
            for (Element article : articles) {
                System.out.println(article.select("h1").get(0).text());
                String title = articles.get(0).child(0).text();
                String content = article.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                try (PreparedStatement statement = connection.prepareStatement("insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())")) {
                    statement.setString(1, link);
                    statement.setString(2, title);
                    statement.setString(3, content);
                    statement.executeUpdate();
                }
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
