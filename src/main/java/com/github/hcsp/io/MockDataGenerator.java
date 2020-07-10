package com.github.hcsp.io;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    private static void mockData(int howMany, SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("com.github.hcsp.MockMapper.selectNews");
            int count = howMany - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    System.out.println(count);
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInserted = currentNews.get(index);
                    Instant currentTime = newsToBeInserted.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 12 * 365));
                    newsToBeInserted.setCreatedAt(currentTime);
                    newsToBeInserted.setModifiedAt(currentTime);
                    session.insert("com.github.hcsp.MockMapper.insertNews", newsToBeInserted);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }

            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
            session.commit();
        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mockData(1000020, sqlSessionFactory);
    }
}
