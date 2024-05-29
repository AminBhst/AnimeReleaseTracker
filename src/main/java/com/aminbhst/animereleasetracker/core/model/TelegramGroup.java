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
public class TelegramGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private Long groupId;

    @OneToMany(mappedBy = "telegramGroup")
    private Set<TelegramUser> registeredMembers = new HashSet<>();

}
