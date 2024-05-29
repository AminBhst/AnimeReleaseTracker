package com.aminbhst.animereleasetracker.core.tracker;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;

public interface ReleaseTracker {
    TrackerResult checkNewRelease(AnimeTitle animeTitle);
}
