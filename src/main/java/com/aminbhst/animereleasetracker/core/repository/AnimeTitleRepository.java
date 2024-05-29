package com.aminbhst.animereleasetracker.core.repository;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface AnimeTitleRepository extends
        Repository<AnimeTitle, Long>,
        CrudRepository<AnimeTitle, Long>,
        JpaRepository<AnimeTitle, Long> {

    AnimeTitle findByMyAnimeListId(int myAnimeListId);

//    Page<AnimeTitle> findAllByFinishedAiring(boolean finishedAiring, Pageable pageable);

//    @Query(value = "SELECT anime FROM AnimeTitle anime WHERE finishedAiring = :finishedAiring")
//    Page<AnimeTitle> findAllByFinishedAiring(@Param("finishedAiring") boolean finishedAiring, Pageable pageable);


}
