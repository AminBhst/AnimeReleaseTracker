package com.aminbhst.animereleasetracker.bot;

import com.aminbhst.animereleasetracker.config.ConfigProperties;
import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.model.TelegramGroup;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.notifier.TelegramNotifier;
import com.aminbhst.animereleasetracker.core.provider.MyAnimeListApi;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.GroupRepository;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
import com.aminbhst.animereleasetracker.core.service.TelegramUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class AnimeReleaseTrackerBot extends TelegramLongPollingBot implements TelegramNotifier {

    private final AnimeTitleRepository animeTitleRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final GroupRepository groupRepository;
    private final MyAnimeListApi myAnimeListApi;
    private final TelegramUserService telegramUserService;
    private final ConfigProperties configProperties;

    private final Set<Integer> setupMessageIds = new HashSet<>();

    @Autowired
    public AnimeReleaseTrackerBot(AnimeTitleRepository animeTitleRepository,
                                  TelegramUserRepository telegramUserRepository,
                                  GroupRepository groupRepository,
                                  MyAnimeListApi myAnimeListApi,
                                  TelegramUserService telegramUserService,
                                  ConfigProperties configProperties) {
        super(configProperties.getTelegramBotToken()); // TODO remove before moving to git
        this.animeTitleRepository = animeTitleRepository;
        this.telegramUserRepository = telegramUserRepository;
        this.groupRepository = groupRepository;
        this.myAnimeListApi = myAnimeListApi;
        this.telegramUserService = telegramUserService;
        this.configProperties = configProperties;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String text = update.getMessage().getText();
//        log.info("Caught message! {}", text);
        if (text.startsWith("/setup")) {
            startSetupChain(update);
            return;
        }


        Message replyToMessage = update.getMessage().getReplyToMessage();
        if (replyToMessage == null)
            return;

        if (setupMessageIds.contains(replyToMessage.getMessageId())) {
            handleSetup(update);
        }
    }

    private void startSetupChain(Update update) {
        sendText(update, "Reply to this message with your MyAnimeList username to setup your profile", true);
    }

    private void handleSetup(Update update) {
        this.handleUserSetup(update);
        boolean isGroupMessage = update.getMessage().isGroupMessage();
        if (isGroupMessage) {
            handleGroupSetup(update);
        }
        sendText(update, "Setup completed successfully!", true);
        this.setupMessageIds.remove(update.getMessage().getReplyToMessage().getMessageId());
    }

    private void handleUserSetup(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        TelegramUser user = telegramUserRepository.findByTelegramId(userId);
        String myAnimeListUsername = update.getMessage().getText();
        List<AnimeTitle> userWatchingList;
        try {
            userWatchingList = myAnimeListApi.getUserWatchingList(myAnimeListUsername);
        } catch (Throwable t) {
            sendText(update, "Failed to retrieve your watching list. The provided username may be incorrect!", true);
            return;
        }
        if (user == null) {
            user = new TelegramUser();
        }
        user.setCachedWatchList(new HashSet<>(userWatchingList));
        user.setMyAnimeListUsername(myAnimeListUsername);
        user.setTelegramId(userId);
        user.setTelegramUsername(update.getMessage().getFrom().getUserName());
        telegramUserRepository.save(user);
        for (AnimeTitle animeTitle : userWatchingList) {
            animeTitle = animeTitleRepository.findByMyAnimeListId(animeTitle.getMyAnimeListId());
            telegramUserService.addToWatchingListAndReverse(user, animeTitle);
        }
    }

    private void handleGroupSetup(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Long groupId = update.getMessage().getChat().getId();
        TelegramUser user = telegramUserRepository.findByTelegramId(userId);
        TelegramGroup existingGroup = groupRepository.findByGroupId(groupId);
        if (existingGroup == null) {
            saveNewGroup(groupId, user);
            return;
        }
        user.setTelegramGroup(existingGroup);
        existingGroup.getRegisteredMembers().add(user);
        telegramUserRepository.save(user);
        groupRepository.save(existingGroup);
    }

    private void saveNewGroup(Long groupId, TelegramUser user) {
        TelegramGroup group = new TelegramGroup();
        group.setGroupId(groupId);
        groupRepository.save(group);
        user.setTelegramGroup(group);
        group.getRegisteredMembers().add(user);
        telegramUserRepository.save(user);
        groupRepository.save(group);
    }

    private void sendText(Update update, String text, boolean reply) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        if (reply) {
            sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
        }
        sendMessage.setChatId(update.getMessage().getChatId());
        try {
            Message message = execute(sendMessage);
            this.setupMessageIds.add(message.getMessageId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message!", e);
        }
    }

    private void sendText(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message!", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "animereleasetrackerbot";
    }

    @Override
    @Transactional
    public void notifyRelease(AnimeTitle animeTitle) {
        animeTitle = animeTitleRepository.findById(animeTitle.getId()).orElseThrow();
        List<TelegramUser> notifiedUsers = new ArrayList<>();
        for (TelegramUser user : animeTitle.getUsers()) {
            if (notifiedUsers.contains(user))
                continue;

            user = telegramUserRepository.findById(user.getId()).orElseThrow();
            TelegramGroup group = user.getTelegramGroup();
            if (group != null) {
                StringBuilder text = new StringBuilder();
                for (TelegramUser member : group.getRegisteredMembers()) {
                    if (StringUtils.isNotBlank(member.getTelegramUsername())) {
                        text.append("@").append(member.getTelegramUsername()).append("\n");
                    } else {
                        text.append("[").append(user.getName()).append("]")
                                .append("(tg://user?id=").append(user.getTelegramId()).append(")").append("\n");
                    }
                }

                text.append("New Episode of ")
                        .append(animeTitle.getTitle())
                        .append(" is now available on animelist.tv");

                sendText(group.getGroupId(), text.toString());
                notifiedUsers.addAll(group.getRegisteredMembers());
            }
            String text = "New episode of %s is now released on animelist!";
            sendText(user.getTelegramId(), String.format(text, animeTitle.getTitle()));
        }
    }


}
