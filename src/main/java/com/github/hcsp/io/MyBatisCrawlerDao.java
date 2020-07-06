package com.github.hcsp.io;

import java.sql.SQLException;

public class MyBatisCrawlerDao implements CrawlerDao {
    @Override
    public String loadUrlsFromDatabase(String sql) throws SQLException {
        return null;
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        return null;
    }

    @Override
    public void executeSql(String link, String sql) throws SQLException {

    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {

    }

    @Override
    public boolean isAlreadyProcessed(String link, String sql) throws SQLException {
        return false;
    }
}
