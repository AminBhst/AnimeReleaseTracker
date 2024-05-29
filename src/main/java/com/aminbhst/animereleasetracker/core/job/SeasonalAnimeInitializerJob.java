package com.aminbhst.animereleasetracker.core.job;

import com.aminbhst.animereleasetracker.config.ConfigProperties;
import com.aminbhst.animereleasetracker.core.initializer.SeasonalAnimeInitializer;
import com.aminbhst.quartzautoconfigboot.annotation.QuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
@QuartzJob(cron = "0 0 12 * * ?")
public class SeasonalAnimeInitializerJob implements Job {

    private final SeasonalAnimeInitializer seasonalAnimeInitializer;
    private final ConfigProperties configProperties;

    @Autowired
    public SeasonalAnimeInitializerJob(SeasonalAnimeInitializer seasonalAnimeInitializer,
                                       ConfigProperties configProperties) {
        this.seasonalAnimeInitializer = seasonalAnimeInitializer;
        this.configProperties = configProperties;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        if (StringUtils.isNotBlank(configProperties.getForceInitializeSeason())) {
            try {
                seasonalAnimeInitializer.initializeSeason(
                        configProperties.getForceInitializeYear(),
                        configProperties.getForceInitializeSeason()
                );
            } catch (Throwable t) {
                log.error("Failed to force initialize season", t);
            }
        }

        try {
            seasonalAnimeInitializer.initializeCurrentSeasonalAnime();
        } catch (Throwable t) {
            log.error("Failed to initialize currnet season", t);
        }

    }
}
