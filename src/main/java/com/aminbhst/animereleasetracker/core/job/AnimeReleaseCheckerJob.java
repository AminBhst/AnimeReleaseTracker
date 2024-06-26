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
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Slf4j
@Component
@DisallowConcurrentExecution
@QuartzJob(cron = "0 0 * ? * *")
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
        log.info("Started AnimeReleaseCheckerJob");
        super.process();
        log.info("Finished AnimeReleaseCheckerJob");
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
        TrackerResult result = releaseTracker.checkNewEpisode(animeTitle);
        if (result.getNewEpisode() == 0 || !result.isNewEpisodeReleased())
            return;

        releaseTracker.setLatestEpisodes(animeTitle, result);
        telegramNotifier.notifyRelease(animeTitle);
    }

}
