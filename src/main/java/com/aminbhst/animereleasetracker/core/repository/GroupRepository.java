package com.aminbhst.animereleasetracker.core.repository;

import com.aminbhst.animereleasetracker.core.model.TelegramGroup;
import com.aminbhst.animereleasetracker.core.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

public interface GroupRepository extends JpaRepository<TelegramGroup, Long>, CrudRepository<TelegramGroup, Long> {

    TelegramGroup findByGroupId(Long groupId);
}
