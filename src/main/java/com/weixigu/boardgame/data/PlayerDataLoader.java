package com.weixigu.boardgame.data;

import com.weixigu.boardgame.BoardGameApplication;
import com.weixigu.boardgame.domain.Player;
import com.weixigu.boardgame.repo.IPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//Pre-load some Player data in order to show how PlayerList looks like.
@Component
public class PlayerDataLoader implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardGameApplication.class);
    private final IPlayerRepository repository;

    @Autowired
    public PlayerDataLoader(IPlayerRepository repository){
        this.repository = repository;
    }

    @Override
    public void run(String... strings) throws Exception{
        LOGGER.info("Pre-loading Player data.");

        this.repository.save(Player.playerBuilder().firstName("Rick").lastName("Sanchez").nickName("Pickle Rick")
                .numTicTacToeLoss(0).numTicTacToeWin(10).isDeleted(false).build());

        this.repository.save(Player.playerBuilder().firstName("Tiabeanie").lastName("Whatever").nickName("Bean")
                .numTicTacToeLoss(2).numTicTacToeWin(6).isDeleted(false).build());

        this.repository.save(Player.playerBuilder().firstName("Bender").lastName("Rodríguez").nickName("Shiny Metal Piece")
                .numTicTacToeLoss(101011).numTicTacToeWin(11).isDeleted(false).build());

        this.repository.flush();

        LOGGER.info("Finish pre-loading Player data.");
    }
}
