package com.aminbhst.animereleasetracker.core.tracker;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.util.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyAnimeListReleaseTracker extends AbstractAnimeReleaseTracker {

    public MyAnimeListReleaseTracker(@Autowired AnimeTitleRepository animeTitleRepository) {
        super(animeTitleRepository);
    }

    @Override
    public int getLatestEpisodeNumber(AnimeTitle animeTitle) {
        String myAnimeListUrl = animeTitle.getMyAnimeListUrl();
        String episodesUrl = myAnimeListUrl + "/episode";
        String htmlResponse = HttpUtils.getHtmlResponse(episodesUrl);
        Document html = Jsoup.parse(htmlResponse);
        Elements episodes = html.getElementsByClass("episode-list-data");
        return episodes.size();
    }

    @Override
    public int getLatestTrackedEpisode(AnimeTitle animeTitle) {
        return animeTitle.getMyAnimeListLatestTrackedEpisode();
    }

}
