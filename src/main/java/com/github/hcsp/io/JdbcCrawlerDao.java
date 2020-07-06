package com.github.hcsp.io;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "zhang321088";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/C:/Users/Administrator/Desktop/xiedaimala-crawler/news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String loadUrlsFromDatabase() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED limit 1"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public String getNextLinkThenDelete() throws SQLException {
        String link = loadUrlsFromDatabase();
        deleteLink(link);
        return link;
    }

    public void executeSql(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertLinkToBeProcessed(String link) throws SQLException {
        executeSql(link, "INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)");
    }


    public void insertAlreadyProcessed(String link) throws SQLException {
        executeSql(link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) values (?)");
    }

    public void deleteLink(String link) throws SQLException {
        executeSql(link, "delete from LINKS_TO_BE_PROCESSED Where link = (?)");
    }

    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (url,title,content,created_at,modified_at) values (?,?,?,now(),now())")) {
            statement.setString(1, url);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    public boolean isAlreadyProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = (?)")) {
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
}
