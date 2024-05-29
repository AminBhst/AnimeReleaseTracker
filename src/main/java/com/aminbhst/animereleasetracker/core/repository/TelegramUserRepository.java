package com.aminbhst.animereleasetracker.core.repository;

import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long>,
        Repository<TelegramUser, Long>, CrudRepository<TelegramUser, Long> {
    TelegramUser findByTelegramId(Long id);
}
