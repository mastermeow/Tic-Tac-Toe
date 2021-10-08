package com.weixigu.boardgame.repo;

import com.weixigu.boardgame.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IPlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(@Param("firstName") String firstName,
                                                                     @Param("lastName") String lastName,
                                                                     @Param("isDeleted") boolean isDeleted);

    List<Player> findByIsDeleted(@Param("isDeleted") boolean isDeleted);
}
