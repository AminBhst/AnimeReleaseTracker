package com.aminbhst.animereleasetracker.core.provider;

import com.aminbhst.animereleasetracker.config.ConfigProperties;
import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.tracker.AbstractAnimeReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.AnimeListReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.NyaaReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.TrackerResult;
import com.aminbhst.animereleasetracker.exception.AnimeTitleExistsException;
import com.aminbhst.animereleasetracker.util.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MyAnimeListApi {

    private final ConfigProperties configProperties;
    private final AnimeTitleRepository animeTitleRepository;
    private final AbstractAnimeReleaseTracker[] releaseTrackers;

    @Autowired
    public MyAnimeListApi(ConfigProperties configProperties,
                          AnimeTitleRepository animeTitleRepository,
                          NyaaReleaseTracker nyaaReleaseTracker,
                          AnimeListReleaseTracker animeListReleaseTracker) {
        this.configProperties = configProperties;
        this.animeTitleRepository = animeTitleRepository;
        this.releaseTrackers = new AbstractAnimeReleaseTracker[]{
                nyaaReleaseTracker,
                animeListReleaseTracker
        };
    }

    public static final String HEADER_CLIENT_ID = "X-MAL-CLIENT-ID";

    public static final String URL_FORMAT_WATCHING = "https://api.myanimelist.net/v2/users/%s/animelist?status=watching";

    public static final String URL_FORMAT_SEASONAL = "https://api.myanimelist.net/v2/anime/season/%s/%s";

    private Map<String, String> headers;

    @PostConstruct
    private void init() {
        headers = Map.of(HEADER_CLIENT_ID, configProperties.getMyAnimeListClientId());
    }

    public List<AnimeTitle> getUserWatchingList(String username) {
        JsonNode json = HttpUtils.getJson(String.format(URL_FORMAT_WATCHING, username), headers);
        List<AnimeTitle> watchingList = getAnimeTitlesFromWatchingList(json);
        JsonNode paging = json.get("paging");
        while (paging != null && !paging.isNull() && paging.get("next") != null && !paging.get("next").isNull()) {
            String nextUrl = paging.get("next").asText();
            JsonNode response = HttpUtils.getJson(nextUrl, headers);
            List<AnimeTitle> animeTitleList = getAnimeTitlesFromWatchingList(response);
            watchingList.addAll(animeTitleList);
            paging = response.get("paging");
        }
        return watchingList;
    }

    private List<AnimeTitle> getAnimeTitlesFromWatchingList(JsonNode json) {
        ArrayNode data = (ArrayNode) json.get("data");
        List<AnimeTitle> watchingList = new ArrayList<>();
        for (JsonNode item : data) {
            JsonNode node = item.get("node");
            int animeId = node.get("id").asInt();
            AnimeTitle anime = animeTitleRepository.findByMyAnimeListId(animeId);
            if (anime != null)
                watchingList.add(anime);
        }
        return watchingList;
    }

    public void initializeSeasonalAnime(int year, String season) {
        String url = String.format(URL_FORMAT_SEASONAL, year, season);
        log.info("Started initializing seasonal anime of {} {}", season, year);
        initializeAnimeFromMyAnimeList(url);
    }

    private void initializeAnimeFromMyAnimeList(String url) {
        JsonNode response = HttpUtils.getJson(url, headers);
        ArrayNode nodes = (ArrayNode) response.get("data");
        JsonNode seasonNode = response.get("season");
        int year = seasonNode.get("year").asInt();
        String season = seasonNode.get("season").asText();
        List<AnimeTitle> animeTitleList = new ArrayList<>();
        for (JsonNode node : nodes) {
            try {
                AnimeTitle animeTitle = createAnimeTitleFromAnimeNode(node, season, year);
                this.setLatestEpisodes(animeTitle);
                animeTitleList.add(animeTitle);
            } catch (Throwable t) {
                log.error("Failed to get animeTitle From anime node", t);
            }
        }
        animeTitleRepository.saveAll(animeTitleList);
        log.info("animeTitles saved successfully");
        JsonNode next = response.get("paging").get("next");
        if (next == null || next.isNull()) {
            log.info("All seasonal anime titles saved successfully");
            return;
        }

        log.info("Moving to the next page");
        initializeAnimeFromMyAnimeList(next.asText());
    }

    private void setLatestEpisodes(AnimeTitle animeTitle) {
        for (AbstractAnimeReleaseTracker releaseTracker : releaseTrackers) {
            TrackerResult result = releaseTracker.checkNewEpisode(animeTitle);
            releaseTracker.setLatestEpisodes(animeTitle, result);
        }
    }

    private AnimeTitle createAnimeTitleFromAnimeNode(JsonNode node, String season, int year) throws AnimeTitleExistsException {
        JsonNode animeNode = node.get("node");
        int id = animeNode.get("id").asInt();
        String title = animeNode.get("title").asText();
        JsonNode mainPic = animeNode.get("main_picture");
        JsonNode mediumPic = mainPic.get("medium");
        JsonNode largePic = mainPic.get("large");
        AnimeTitle animeTitle = new AnimeTitle();

        AnimeTitle existing = animeTitleRepository.findByMyAnimeListId(id);
        if (existing != null)
            throw new AnimeTitleExistsException();

        animeTitle.setMyAnimeListId(id);
        animeTitle.setTitle(title);
        animeTitle.setReleaseSeason(season);
        animeTitle.setYear(year);
        JikanAnimeDetails details = new JikanAnimeDetails();
        String animeListUrl = null;
        try {
            animeListUrl = AnimeListScraper.findAnimeListUrl(title, id);
            details = JikanApi.getAnimeDetails(id);
            Thread.sleep(200); // to prevent rate limit errors
        } catch (Throwable t) {
            log.error("Data extraction failed!", t);
        }
        animeTitle.setEnglishTitle(details.getEnglishTitle());
        animeTitle.setSeasonNumber(details.getSeasonNumber());
        animeTitle.setMyAnimeListUrl(details.getMyAnimeListUrl());
        animeTitle.setAnimeListUrl(animeListUrl);
        if (mediumPic != null && !mediumPic.isNull())
            animeTitle.setMediumImageUrl(mediumPic.asText());
        if (largePic != null && !largePic.isNull())
            animeTitle.setLargeImageUrl(largePic.asText());

        log.info("Successfully fetched data for {}", animeTitle.getTitle());
        return animeTitle;
    }

}
