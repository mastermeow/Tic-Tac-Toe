package com.weixigu.boardgame.repo;

import com.weixigu.boardgame.domain.TicTacToe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITicTacToeRepository extends JpaRepository<TicTacToe, Long> {
    List<TicTacToe> findByIsCurrentGame(boolean isMoveOfCurrentGame);
}
