package com.github.hcsp.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static String url = "https://news.sina.cn/";
    private static List<String> linkPoor = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Set<String> processedLinks = new HashSet<>();
        linkPoor.add(url);
        while (true) {
            if (linkPoor.isEmpty()) {
                break;
            }
            String link = linkPoor.remove(linkPoor.size() - 1);
//            System.out.println(link);
            if (processedLinks.contains(link)) {
                continue;
            }
            processedLinks.add(link);
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                doc.select("a").stream().map(aTag->aTag.attr("href")).forEach(linkPoor::add);

                storeIntoDatabaseIfItIsNewsPage(doc);
            }
        }
    }
    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTag = doc.select("h1");
        if (!articleTag.isEmpty()) {
            doc.select("h1").stream().map(tag->articleTag.get(0).text()).forEach(System.out::println);
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
