package com.aminbhst.animereleasetracker.core.job;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.provider.MyAnimeListApi;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Slf4j
@Component
@DisallowConcurrentExecution
@QuartzJob(cron = "0 0 0/2 * * ?")
public class UsersCachedWatchListSyncJob extends JPAPageProcessor<TelegramUser> implements Job {

    private final TelegramUserRepository telegramUserRepository;
    private final AnimeTitleRepository animeTitleRepository;
    private final MyAnimeListApi myAnimeListApi;

    @Autowired
    public UsersCachedWatchListSyncJob(TelegramUserRepository telegramUserRepository,
                                       AnimeTitleRepository animeTitleRepository,
                                       MyAnimeListApi myAnimeListApi) {
        this.telegramUserRepository = telegramUserRepository;
        this.animeTitleRepository = animeTitleRepository;
        this.myAnimeListApi = myAnimeListApi;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Running UsersCachedWatchListSyncJob...");
        super.process();
        log.info("Finished UsersCachedWatchListSyncJob");
    }

    @Override
    public Page<TelegramUser> fetch(PageRequest pageRequest) {
        return telegramUserRepository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public void processItem(TelegramUser user) {
        log.info("updating user [{}] cached watch list", user.getId());
        List<AnimeTitle> wachingList = myAnimeListApi.getUserWatchingList(user.getMyAnimeListUsername());
        user.setCachedWatchList(new HashSet<>(wachingList));
        List<AnimeTitle> animeTitles = new ArrayList<>();
        for (AnimeTitle animeTitle : wachingList) {
            animeTitle = animeTitleRepository.findById(animeTitle.getId()).orElseThrow();
            animeTitle.getUsers().add(user);
            animeTitles.add(animeTitle);
        }
        animeTitleRepository.saveAll(animeTitles);
        telegramUserRepository.save(user);
        log.info("Successfully updated user [{}] cached watch list", user.getId());
    }
}
