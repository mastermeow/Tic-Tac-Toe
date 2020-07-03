package com.weixigu.reactandspringdatarest.service;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.Player;
import com.weixigu.reactandspringdatarest.repo.IPlayerRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PlayerServiceIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private IPlayerRepository playerRepository;

    //see PlayerDataLoader.java
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
    private static final String DEFAULT_SORT_BY = "id";
    private final static int NUM_OF_PRELOADED_PLAYERS = 3;
    private final static Player FIRST_PRELOADED_PLAYER = Player.playerBuilder().firstName("Rick").lastName("Sanchez")
            .nickName("Pickle Rick").numTicTacToeLoss(0).numTicTacToeWin(10).isDeleted(false).build();

    @Test
    void contextLoads(){
        assertThat(this.playerService).isNotNull();
    }

    @Test
    void getPlayerRepo_requestParamsAreValidWithRespectToPreloadedPlayers_shouldSucceed(){

        LOGGER.info("Integration test: " +
                "getPlayerRepo_requestParamsAreValidWithRespectToPreloadedPlayers_shouldSucceed().");

        assertThatCode(()->this.playerService.getPlayerRepo(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY)).doesNotThrowAnyException();

        Page<Player> myPage = this.playerService.getPlayerRepo(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY);

        assertThat(myPage).isNotNull();
        assertThat(myPage.getNumberOfElements()).isEqualTo(NUM_OF_PRELOADED_PLAYERS);
        assertThat(myPage.getTotalElements()).isEqualTo(NUM_OF_PRELOADED_PLAYERS);
        assertThat(Player.haveSameData(myPage.getContent().get(0), FIRST_PRELOADED_PLAYER)).isTrue();
    }

    @Test
    void getPlayerRepo_pageNumberIsGreaterThanMaxInteger_shouldFail(){

        LOGGER.info("Integration test: getPlayerRepo_pageNumberIsGreaterThanMaxInteger_shouldFail().");

        assertThatThrownBy(()-> this.playerService.getPlayerRepo(DEFAULT_PAGE_NUMBER+1, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("page number")
                .hasMessageContaining("too large");
    }

    @Transactional
    @Test
    public void createPlayer_playerDidNotExist_shouldSucceed(){

        LOGGER.info("Integration test: createPlayer_playerDidNotExist_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("sweet").lastName("berry").isDeleted(false).build();

        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);

        Player savedPlayer = this.playerService.createPlayer(player);

        assertThat(savedPlayer).isEqualTo(player);
        assertThat(this.playerRepository.findByIsDeleted(false)).contains(player);
    }

    @Transactional
    @Test
    public void createPlayer_playerDidExist_shouldFail(){

        LOGGER.info("Integration test: createPlayer_playerNameAlreadyExists_shouldFail().");

        Player player = Player.playerBuilder().firstName("bitter").lastName("melon").build();

        this.playerService.createPlayer(player);

        assertThat(this.playerRepository.findByIsDeleted(false)).contains(player);

        assertThatThrownBy(() -> this.playerService.createPlayer(player)) //create player again
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(player.fullName())
                .hasMessageContaining("already exists");
    }

    @Transactional
    @Test
    public void deletePlayer_playerDidExist_shouldSucceed(){

        LOGGER.info("Integration test: deletePlayer_playerDidExist_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("black").lastName("coffee").build();

        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);

        this.playerService.createPlayer(player);
        assertThat(this.playerRepository.findByIsDeleted(false)).contains(player);

        assertThatCode(()-> this.playerService.deletePlayer(player)).doesNotThrowAnyException();
        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);
    }

    @Transactional
    @Test
    public void deletePlayer_firstNameAndLastNameIgnoreCasesDidExist_shouldSucceed(){

        LOGGER.info("Integration test: deletePlayer_firstNameAndLastNameAllIgnoreCasesDidExist_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("silly").lastName("goose").build();
        Player playerUppercase = Player.playerBuilder().firstName("SILLY").lastName("GOOSE").build();

        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);

        this.playerService.createPlayer(player);
        assertThat(this.playerRepository.findByIsDeleted(false)).contains(player);

        assertThatCode(()-> this.playerService.deletePlayer(playerUppercase)).doesNotThrowAnyException();
        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);
    }

    @Transactional
    @Test
    public void deletePlayer_playerDidNotExist_shouldFail(){

        LOGGER.info("Integration test: deletePlayer_playerDidNotExist_shouldFail().");

        Player player = Player.playerBuilder().firstName("chocolate").lastName("milk").build();

        assertThat(this.playerRepository.findByIsDeleted(false)).doesNotContain(player);

        assertThatThrownBy(()->this.playerService.deletePlayer(player))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(player.fullName())
                .hasMessageContaining("DNE");
    }
}
