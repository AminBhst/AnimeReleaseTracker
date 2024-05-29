package com.aminbhst.animereleasetracker;

import com.aminbhst.animereleasetracker.bot.AnimeCrawlerBot;
import com.aminbhst.animereleasetracker.core.job.AnimeReleaseCheckerJob;
import com.aminbhst.animereleasetracker.core.job.SeasonalAnimeInitializerJob;
import com.aminbhst.quartzautoconfigboot.annotation.EnableQuartzConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;

@SpringBootApplication
@EnableQuartzConfiguration
public class AnimeReleaseTrackerApplication {

    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext ctx = SpringApplication.run(AnimeReleaseTrackerApplication.class, args);
        AnimeCrawlerBot bot = ctx.getBean(AnimeCrawlerBot.class);
        SeasonalAnimeInitializerJob initializer = ctx.getBean(SeasonalAnimeInitializerJob.class);
        initializer.initialize_forced();
        initializer.execute(null);
        registerBot(bot);
    }

    public static void registerBot(AnimeCrawlerBot bot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

}
