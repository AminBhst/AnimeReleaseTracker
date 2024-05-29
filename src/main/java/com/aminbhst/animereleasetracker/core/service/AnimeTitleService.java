package com.aminbhst.animereleasetracker.core.service;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimeTitleService {

    private final AnimeTitleRepository animeTitleRepository;

    private final TelegramUserRepository telegramUserRepository;

    public AnimeTitleService(@Autowired AnimeTitleRepository animeTitleRepository,
                             @Autowired TelegramUserRepository telegramUserRepository) {
        this.animeTitleRepository = animeTitleRepository;
        this.telegramUserRepository = telegramUserRepository;
    }

    @Transactional
    public void addToUsersAndReverse(TelegramUser user, AnimeTitle animeTitle) {
        animeTitle = animeTitleRepository.findByMyAnimeListId(animeTitle.getMyAnimeListId());
        animeTitle.getUsers().add(user);
        user.getCachedWatchList().add(animeTitle);
        animeTitleRepository.save(animeTitle);
        telegramUserRepository.save(user);
    }
}
