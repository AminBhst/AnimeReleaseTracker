package com.aminbhst.animereleasetracker.core.provider;

import com.aminbhst.animereleasetracker.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class JikanApi {

    public static final String URL_SEASONAL_ANIME = "https://api.jikan.moe/v4/seasons/now";

    public static final String URL_FORMAT_ANIME_DETAILS = "https://api.jikan.moe/v4/anime/%s/full";



    public static JikanAnimeDetails getAnimeDetails(int animeId) {
        String url = String.format(URL_FORMAT_ANIME_DETAILS, animeId);
        JsonNode json = HttpUtils.getJson(url, new HashMap<>());
        JikanAnimeDetails details = new JikanAnimeDetails();
        JsonNode data = json.get("data");

        JsonNode titleEng = data.get("title_english");
        if (JsonUtils.isNotEmpty(titleEng))
            details.setEnglishTitle(titleEng.asText());

        JsonNode malUrl = data.get("url");
        if (JsonUtils.isNotEmpty(malUrl))
            details.setMyAnimeListUrl(malUrl.asText());

        JsonNode episodes = data.get("episodes");
        if (JsonUtils.isNotEmpty(episodes))
            details.setEpisodes(episodes.asInt());

        details.setSeasonNumber(1);

        JsonNode titles = data.get("titles");
        if (JsonUtils.isNotEmpty(titles)) {
            for (JsonNode title : titles) {
                String titleStr = title.get("title").asText();
                StringSearchResult result = StringUtilities.containsAnyAndGet_IgnoreCase(
                        titleStr,
                        Seasons.animeSeasonCountMap.keySet().stream().toList()
                );
                if (result.isFound()) {
                    Integer seasonCount = Seasons.animeSeasonCountMap.get(result.getItem());
                    details.setSeasonNumber(seasonCount);
                }
            }
        }


        return details;
    }


}
