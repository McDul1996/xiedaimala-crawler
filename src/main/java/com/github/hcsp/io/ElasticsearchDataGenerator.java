package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<News> newsFromMySql = selectNews(sqlSessionFactory);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> writeSingleThread(newsFromMySql)).start();
        }

    }
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private static void writeSingleThread(List<News> newsFromMySql) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 100; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromMySql) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> data = new HashMap<>();
                    data.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    data.put("title", news.getTitle());
                    data.put("url", news.getUrl());
                    data.put("created_at", news.getCreatedAt());
                    data.put("modified", news.getModifiedAt());
                    request.source(data, XContentType.JSON);
                    bulkRequest.add(request);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Current thread: " + Thread.currentThread().getName() + "finishes" + i + ":" + bulkResponse.status().getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> selectNews(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.hcsp.MockMapper.selectNews");
        }
    }
}
