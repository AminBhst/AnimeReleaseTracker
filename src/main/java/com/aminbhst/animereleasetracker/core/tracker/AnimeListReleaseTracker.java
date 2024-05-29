package com.aminbhst.animereleasetracker.core.tracker;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnimeListReleaseTracker extends AbstractAnimeReleaseTracker {


    public AnimeListReleaseTracker(@Autowired AnimeTitleRepository animeTitleRepository) {
        super(animeTitleRepository);
    }

    @Override
    public int getLatestEpisodeNumber(AnimeTitle animeTitle) {
        if (StringUtils.isBlank(animeTitle.getAnimeListUrl()))
            return 0;

        String htmlResponse = HttpUtils.getHtmlResponse(animeTitle.getAnimeListUrl());
        Document document = Jsoup.parse(htmlResponse);
        Elements downloadBoxes = document.getElementsByClass("download-box__movie");
        for (Element downloadBox : downloadBoxes) {
            Element info = downloadBox.getElementsByClass("info__quality").get(0);
            String text = info.text();
            if (StringUtils.isBlank(text) || !text.contains("Episode"))
                continue;

            String episodeNumStr = text.replaceAll("Episode ", "");
            try {
                int episodeNum = Integer.parseInt(episodeNumStr);
                if (episodeNum > animeTitle.getAnimeListLatestTrackedEpisode())
                    return episodeNum;
            } catch (Throwable t) {
                log.error("Failed to parse episode for str {}", text, t);
            }
        }
        return 0;
    }

    @Override
    public int getLatestTrackedEpisode(AnimeTitle animeTitle) {
        return animeTitle.getAnimeListLatestTrackedEpisode();
    }

}
