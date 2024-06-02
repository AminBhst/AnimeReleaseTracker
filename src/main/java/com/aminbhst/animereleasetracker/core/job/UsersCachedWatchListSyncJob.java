package com.aminbhst.animereleasetracker.core.job;

import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
import com.aminbhst.animereleasetracker.core.service.TelegramUserService;
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


@Slf4j
@Component
@DisallowConcurrentExecution
@QuartzJob(cron = "0 0 */6 ? * *")
public class UsersCachedWatchListSyncJob extends JPAPageProcessor<TelegramUser> implements Job {

    private final TelegramUserRepository telegramUserRepository;
    private final TelegramUserService telegramUserService;

    @Autowired
    public UsersCachedWatchListSyncJob(TelegramUserRepository telegramUserRepository,
                                       TelegramUserService telegramUserService) {
        this.telegramUserRepository = telegramUserRepository;
        this.telegramUserService = telegramUserService;
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
    public void processItem(TelegramUser user) {
        log.info("updating user [{}] cached watch list", user.getId());
        telegramUserService.updateUserWatchlist(user);
        log.info("Successfully updated user [{}] cached watch list", user.getId());
    }

}
