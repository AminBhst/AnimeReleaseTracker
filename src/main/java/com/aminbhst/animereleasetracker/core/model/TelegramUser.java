package com.aminbhst.animereleasetracker.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private String myAnimeListUsername;

    @Column(unique = true)
    private Long telegramId;

    @Column(unique = true)
    private String telegramUsername;

    @Column
    private String trackerType;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private TelegramGroup telegramGroup;

    @ManyToMany(mappedBy = "users")
    private Set<AnimeTitle> cachedWatchList = new HashSet<>();

}
