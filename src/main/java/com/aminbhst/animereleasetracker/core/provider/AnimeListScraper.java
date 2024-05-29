package com.aminbhst.animereleasetracker.core.provider;

import com.aminbhst.animereleasetracker.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AnimeListScraper {

    public static final String URL_FORMAT_SEARCH_ANIME = "https://animelist.tv/search?type=anime&s=%s";

    public static String findAnimeListUrl(String animeTitle, int myAnimeListId) {
        String query = animeTitle.replaceAll("\"", " ").replaceAll(" ", "+");
        String url = String.format(URL_FORMAT_SEARCH_ANIME, query);
        String htmlResponse;
        try {
            htmlResponse = HttpUtils.getHtmlResponse(url);
        } catch (Throwable t) {
            log.error("Failed to get myAnimeList URL {}", url);
            return null;
        }
        Document html = Jsoup.parse(htmlResponse);
        Elements searchResults = html.getElementsByClass("animeh-list__item articleSlider");
        List<String> resultLinks = new ArrayList<>();
        for (Element element : searchResults) {
            Element parent = element.parents().get(0);
            String link = parent.attr("href");
            resultLinks.add(link);
        }
        return findAccurateAnimeListLink(resultLinks, myAnimeListId);
    }

    private static String findAccurateAnimeListLink(List<String> links, int myAnimeListId) {
        for (String link : links) {
            String htmlResponse;
            try {
                htmlResponse = HttpUtils.getHtmlResponse(link);
            } catch (Throwable t) {
                log.error("Failed to get anime list url {}", link);
                continue;
            }
            Document html = Jsoup.parse(htmlResponse);
            Elements header = html.getElementsByClass("header-single__meta--rate-imdb");
            if (header.size() != 1) {
                log.error("Failed to find accurate animelist link for anime id {}", myAnimeListId);
                continue;
            }
            Element a = header.get(0).getElementsByTag("a").get(0);
            String text = a.text();
            if (!text.equals("MyAnimeList"))
                continue;

            String myAnimeListLink = a.attr("href");
            if (myAnimeListLink.contains("/" + myAnimeListId))
                return link;
        }
        return null;
    }


}
