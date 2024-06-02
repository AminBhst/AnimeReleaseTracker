package com.aminbhst.animereleasetracker;

import com.aminbhst.animereleasetracker.bot.AnimeReleaseTrackerBot;
import com.aminbhst.animereleasetracker.core.job.SeasonalAnimeInitializerJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class AnimeReleaseTrackerApplication {

    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext ctx = SpringApplication.run(AnimeReleaseTrackerApplication.class, args);
        AnimeReleaseTrackerBot bot = ctx.getBean(AnimeReleaseTrackerBot.class);
        SeasonalAnimeInitializerJob initializer = ctx.getBean(SeasonalAnimeInitializerJob.class);
//        initializer.initialize_forced();
//        initializer.execute(null);
        registerBot(bot);
    }

    public static void registerBot(AnimeReleaseTrackerBot bot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

}
