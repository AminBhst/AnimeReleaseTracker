package com.aminbhst.animereleasetracker.core.job;

import com.aminbhst.animereleasetracker.config.ConfigProperties;
import com.aminbhst.animereleasetracker.core.initializer.SeasonalAnimeInitializer;
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
public class SeasonalAnimeInitializerJob implements Job {

    private final SeasonalAnimeInitializer seasonalAnimeInitializer;
    private final ConfigProperties configProperties;

    @Autowired
    public SeasonalAnimeInitializerJob(SeasonalAnimeInitializer seasonalAnimeInitializer,
                                       ConfigProperties configProperties) {
        this.seasonalAnimeInitializer = seasonalAnimeInitializer;
        this.configProperties = configProperties;
    }

    public void initialize_forced() {
        if (StringUtils.isBlank(configProperties.getForceInitializeSeason())) {
            log.warn("Will not force initialize! season is null");
            return;
        }

        try {
            seasonalAnimeInitializer.initializeSeason(
                    configProperties.getForceInitializeYear(),
                    configProperties.getForceInitializeSeason()
            );
        } catch (Throwable t) {
            log.error("Failed to force initialize season", t);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            seasonalAnimeInitializer.initializeCurrentSeasonalAnime();
        } catch (Throwable t) {
            log.error("Failed to initialize current season", t);
        }

    }
}
