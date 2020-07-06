package com.github.hcsp.io;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.hcsp.MyMapper.selectNextAvailableLink");
            if (link != null) {
                session.delete("com.github.hcsp.MyMapper.deleteLink", link);
            }
            return link;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertNews", new News(url, title, content));
        }
    }

    @Override
    public boolean isAlreadyProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int i = session.selectOne("com.github.hcsp.MyMapper.isAlreadyProcessed", link);
            return i != 0;
        }
    }

    @Override
    public void insertAlreadyProcessed(String link) {
        insertProcessed(link,"links_already_processed");
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        insertProcessed(link, "LINKS_TO_BE_PROCESSED");
    }

    private void insertProcessed(String link, String value) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", value);
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertAlreadyProcessed", param);
        }
    }

}
