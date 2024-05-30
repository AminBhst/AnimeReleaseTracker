package com.aminbhst.animereleasetracker.core.tracker;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.tracker.TrackerResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public abstract class AbstractAnimeReleaseTracker {

    private final AnimeTitleRepository animeTitleRepository;

    protected AbstractAnimeReleaseTracker(AnimeTitleRepository animeTitleRepository) {
        this.animeTitleRepository = animeTitleRepository;
    }

    public TrackerResult checkNewEpisode(AnimeTitle animeTitle){
        try {
            int latestEpisodeNumber = getLatestEpisodeNumber(animeTitle);
            if (latestEpisodeNumber > getLatestTrackedEpisode(animeTitle)) {
                return new TrackerResult(latestEpisodeNumber, true);
            }
        } catch (Throwable t) {
            log.error("Failed to check for new episode");
        }
        return new TrackerResult(0, false);
    }


    public void setLatestEpisodes(AnimeTitle animeTitle, TrackerResult result) {
        if (this instanceof NyaaReleaseTracker) {
            animeTitle.setNyaaLatestTrackedEpisode(result.getNewEpisode());
        } else if (this instanceof AnimeListReleaseTracker) {
            animeTitle.setAnimeListLatestTrackedEpisode(result.getNewEpisode());
        } else if (this instanceof MyAnimeListReleaseTracker) {
            animeTitle.setMyAnimeListLatestTrackedEpisode(result.getNewEpisode());
        }
        animeTitle.setLatestCheckDate(new Date());
    }

    public abstract int getLatestEpisodeNumber(AnimeTitle animeTitle) throws Exception;

    public abstract int getLatestTrackedEpisode(AnimeTitle animeTitle);

}
