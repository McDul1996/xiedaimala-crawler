package com.github.hcsp.io;

import java.sql.SQLException;

public interface CrawlerDao {
    String loadUrlsFromDatabase(String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void executeSql(String link, String sql) throws SQLException;

    void insertNewsIntoDatabase(String url, String title, String content) throws SQLException;

    boolean isAlreadyProcessed(String link, String sql) throws SQLException;

}
