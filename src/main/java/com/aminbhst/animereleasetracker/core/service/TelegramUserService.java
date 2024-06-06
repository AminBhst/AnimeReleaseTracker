package com.aminbhst.animereleasetracker.core.service;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.model.TelegramGroup;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.provider.MyAnimeListApi;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.GroupRepository;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    private final GroupRepository groupRepository;
    private final AnimeTitleRepository animeTitleRepository;
    private final MyAnimeListApi myAnimeListApi;


    @Autowired
    public TelegramUserService(TelegramUserRepository telegramUserRepository,
                               GroupRepository groupRepository,
                               AnimeTitleRepository animeTitleRepository,
                               MyAnimeListApi myAnimeListApi) {
        this.telegramUserRepository = telegramUserRepository;
        this.groupRepository = groupRepository;
        this.animeTitleRepository = animeTitleRepository;
        this.myAnimeListApi = myAnimeListApi;
    }

    @Transactional
    public void addToWatchingListAndReverse(TelegramUser user, AnimeTitle animeTitle) {
        animeTitle = animeTitleRepository.findById(animeTitle.getId()).orElse(null);
        TelegramUser existingUser = telegramUserRepository.findById(user.getId()).orElse(null);
        if (animeTitle == null)
            return;

        if (existingUser == null)
            existingUser = user;

        animeTitle.getUsers().add(existingUser);
        existingUser.getCachedWatchList().add(animeTitle);
        animeTitleRepository.save(animeTitle);
        telegramUserRepository.save(existingUser);
    }


    @Transactional
    public void handleGroupSetup(Update update) {
        log.info("Inside group setup");
        Long userId = update.getMessage().getFrom().getId();
        log.info("User id : {}", userId);
        Long groupId = update.getMessage().getChat().getId();
        log.info("Group id : {}", groupId);
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


    @Transactional
    public void updateUserWatchlist(TelegramUser user) {
        List<AnimeTitle> wachingList = myAnimeListApi.getUserWatchingList(user.getMyAnimeListUsername());
        log.info("{} currently watching anime were found for user {}", wachingList.size(), user.getMyAnimeListUsername());
        user.setCachedWatchList(new HashSet<>(wachingList));
        List<AnimeTitle> animeTitles = new ArrayList<>();
        for (AnimeTitle animeTitle : wachingList) {
            animeTitle = animeTitleRepository.findById(animeTitle.getId()).orElseThrow();
            if (animeTitle.getUsers().contains(user))
                continue;

            animeTitle.getUsers().add(user);
            animeTitles.add(animeTitle);
        }
        animeTitleRepository.saveAll(animeTitles);
        telegramUserRepository.save(user);
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

}
