package com.aminbhst.animereleasetracker.config;

import com.aminbhst.animereleasetracker.core.job.AnimeReleaseCheckerJob;
import com.aminbhst.animereleasetracker.core.job.SeasonalAnimeInitializerJob;
import com.aminbhst.animereleasetracker.core.job.UsersCachedWatchListSyncJob;
import com.aminbhst.animereleasetracker.util.QuartzUtil;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzJobs {

    @Bean
    public JobDetailFactoryBean usersCachedWatchListSyncJobJobDetail() {
        return QuartzUtil.createJobDetail(UsersCachedWatchListSyncJob.class, "UsersCachedWatchListSyncJob Job");
    }

    @Bean
    public CronTriggerFactoryBean UsersCachedWatchListSyncJobTrigger(@Qualifier("usersCachedWatchListSyncJobJobDetail") JobDetail jobDetail) {
        return QuartzUtil.createCronTrigger(jobDetail, "0 0 0/2 * * ?", "UsersCachedWatchListSyncJob Trigger");
    }

    @Bean
    public JobDetailFactoryBean seasonalAnimeInitializerJobJobDetail() {
        return QuartzUtil.createJobDetail(SeasonalAnimeInitializerJob.class, "SeasonalAnimeInitializerJob Job");
    }

    @Bean
    public CronTriggerFactoryBean SeasonalAnimeInitializerJobTrigger(@Qualifier("seasonalAnimeInitializerJobJobDetail") JobDetail jobDetail) {
        return QuartzUtil.createCronTrigger(jobDetail, "0 0 12 * * ?", "SeasonalAnimeInitializerJob Trigger");
    }

    @Bean
    public JobDetailFactoryBean animeReleaseCheckerJobJobDetail() {
        return QuartzUtil.createJobDetail(AnimeReleaseCheckerJob.class, "AnimeReleaseCheckerJob Job");
    }

    @Bean
    public SimpleTriggerFactoryBean AnimeReleaseCheckerJobTrigger(@Qualifier("animeReleaseCheckerJobJobDetail") JobDetail jobDetail) {
        return QuartzUtil.createSimpleTrigger(jobDetail, 1000, "AnimeReleaseCheckerJob Trigger");
    }

}