package com.aminbhst.animereleasetracker.core.notifier;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;

public interface TelegramNotifier {
    void notifyRelease(AnimeTitle animeTitle);
}
