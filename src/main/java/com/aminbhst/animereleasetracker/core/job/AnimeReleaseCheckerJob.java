package com.aminbhst.animereleasetracker.core.job;

import com.aminbhst.animereleasetracker.core.notifier.TelegramNotifier;
import com.aminbhst.animereleasetracker.core.tracker.AbstractAnimeReleaseTracker;
import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.tracker.AnimeListReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.MyAnimeListReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.NyaaReleaseTracker;
import com.aminbhst.animereleasetracker.core.tracker.TrackerResult;
import com.aminbhst.animereleasetracker.util.JPAPageProcessor;
import com.aminbhst.quartzautoconfigboot.annotation.QuartzJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;


@Component
@DisallowConcurrentExecution
@QuartzJob(repeatInterval = 1000)
public class AnimeReleaseCheckerJob extends JPAPageProcessor<AnimeTitle> implements Job {


    private final AnimeTitleRepository animeTitleRepository;

    private final TelegramNotifier telegramNotifier;

    private final AbstractAnimeReleaseTracker[] releaseTrackers;

    @Autowired
    public AnimeReleaseCheckerJob(
//            NyaaReleaseTracker nyaaReleaseTracker,
                                  AnimeListReleaseTracker animeListReleaseTracker,
//                                  MyAnimeListReleaseTracker myAnimeListReleaseTracker,
                                  AnimeTitleRepository animeTitleRepository,
                                  TelegramNotifier telegramNotifier) {
        this.animeTitleRepository = animeTitleRepository;
        this.telegramNotifier = telegramNotifier;
        this.releaseTrackers = new AbstractAnimeReleaseTracker[]{
//                nyaaReleaseTracker,
                animeListReleaseTracker,
//                myAnimeListReleaseTracker
        };
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        super.process();
    }

    @Override
    public Page<AnimeTitle> fetch(PageRequest pageRequest) {
        return animeTitleRepository.findAll(pageRequest);
    }

    @Override
    public void processItem(AnimeTitle anime) {
        Arrays.stream(releaseTrackers)
                .forEach(tracker -> this.handleReleaseCheck(tracker, anime));

        animeTitleRepository.save(anime);
    }

    private void handleReleaseCheck(AbstractAnimeReleaseTracker releaseTracker, AnimeTitle animeTitle) {
        if (!animeTitle.getTitle().contains("Kaijuu "))
            return;

        TrackerResult result = releaseTracker.checkNewEpisode(animeTitle);
        if (result.getNewEpisode() == 0 || !result.isNewEpisodeReleased())
            return;

        if (releaseTracker instanceof NyaaReleaseTracker) {
            animeTitle.setNyaaLatestTrackedEpisode(result.getNewEpisode());
        } else if (releaseTracker instanceof AnimeListReleaseTracker) {
            animeTitle.setAnimeListLatestTrackedEpisode(result.getNewEpisode());
        } else if (releaseTracker instanceof MyAnimeListReleaseTracker) {
            animeTitle.setMyAnimeListLatestTrackedEpisode(result.getNewEpisode());
        }
        animeTitle.setLatestCheckDate(new Date());
        telegramNotifier.notifyRelease(animeTitle);
    }
}
