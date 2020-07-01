package com.github.hcsp.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Main {
    private static String url = "https://news.sina.cn/";
    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(url).get();
        System.out.println(document.toString());
    }
}
