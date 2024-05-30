package com.aminbhst.animereleasetracker.core.service;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.GroupRepository;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    private final GroupRepository groupRepository;
    private final AnimeTitleRepository animeTitleRepository;


    @Autowired
    public TelegramUserService(TelegramUserRepository telegramUserRepository,
                               GroupRepository groupRepository,
                               AnimeTitleRepository animeTitleRepository) {
        this.telegramUserRepository = telegramUserRepository;
        this.groupRepository = groupRepository;
        this.animeTitleRepository = animeTitleRepository;
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

}
