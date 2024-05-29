package com.aminbhst.animereleasetracker.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table
public class AnimeTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String title;

    @Column
    private String englishTitle;

    @Column
    private Integer year;

    @Column
    private String releaseSeason;

    @Column
    private Boolean finishedAiring = false;

    @Column
    private Date latestCheckDate;

    @Column
    private int seasonNumber;

    @Column
    private int partNumber;

    @Column(unique = true)
    private int myAnimeListId;

    @Column
    private String myAnimeListUrl;

    @Column
    private String animeListUrl;

    @Column
    private String largeImageUrl;

    @Column
    private String mediumImageUrl;

    @Column
    private Integer myAnimeListLatestTrackedEpisode = 0;

    @Column
    private Integer animeListLatestTrackedEpisode = 0;

    @Column
    private Integer nyaaLatestTrackedEpisode = 0;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_cached_watch_list",
            joinColumns = @JoinColumn(name = "anime_title_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<TelegramUser> users = new HashSet<>();

}
