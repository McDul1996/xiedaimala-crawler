package com.github.hcsp.io;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String url, String title, String content) throws SQLException;

    boolean isAlreadyProcessed(String link) throws SQLException;

    void insertAlreadyProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String href) throws SQLException;
}
