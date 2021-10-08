package com.weixigu.boardgame.service;

import com.weixigu.boardgame.domain.Player;
import com.weixigu.boardgame.repo.IPlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PlayerServiceUnitTest {

    private PlayerService playerService;

    private static final String DEFAULT_SORT_BY = "id";

    @Mock
    private IPlayerRepository playerRepository;

    @BeforeEach
    void setUp(){

        //Initialize fields annotated with Mockito annotations (e.g. @Mock)
        MockitoAnnotations.initMocks(this);

        //Instantiate an instance of the class to be tested.
        this.playerService = new PlayerService(this.playerRepository);
    }

    @Test
    void testMockSetUp(){
        assertThat(this.playerRepository).isNotNull();
        assertThat(this.playerService).isNotNull();
    }

    @Test
    void getPlayerRepo_eightPlayersInRepoAndPageIsTwoAndSizeIsThree_shouldSucceed(){
        //The retrieved page should contain the 7th and 8th players.
        List<Player> players = new ArrayList<>();
        int totalElements = 8;
        String firstNamePrefix = "Moxxi";

        for(int i = 1; i <= totalElements; i++){

            Player player = Player.playerBuilder().firstName(firstNamePrefix+i).build();
            players.add(player);
        }

        int pageNumber = 2;
        int pageSize = 3;
        int expectedNumOfElements = 2;

        //stubbing
        Mockito.when(this.playerRepository.findByIsDeleted(false)).thenReturn(players);

        //testing
        assertThatCode(()->this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY))
                .doesNotThrowAnyException();
        Page<Player> page = this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY);
        assertThat(page).isNotNull();
        assertThat(page.getNumber()).isEqualTo(pageNumber);
        assertThat(page.getNumberOfElements()).isEqualTo(expectedNumOfElements);
        assertThat(page.getTotalElements()).isEqualTo(totalElements);

        int totalPages = (int) Math.ceil((double) totalElements / (double) pageSize);
        assertThat(page.getTotalPages()).isEqualTo(totalPages);

        for(Player player : page.getContent()){

            assertThat(player.getFirstName()).contains(firstNamePrefix);
        }
    }

    @Test
    void getPlayerRepo_eightPlayersInRepoAndPageNumberIsZeroAndPageSizeIsMaxInteger_shouldSucceed(){
        //The retrieved page should contain all 8 players.
        List<Player> players = new ArrayList<>();
        int totalElements = 8;
        String firstNamePrefix = "bobby";

        for(int i = 1; i <= totalElements; i++){

            Player player = Player.playerBuilder().firstName(firstNamePrefix+i).build();
            players.add(player);
        }

        int pageNumber = 0;
        int pageSize = Integer.MAX_VALUE;

        //stubbing
        Mockito.when(this.playerRepository.findByIsDeleted(false)).thenReturn(players);

        //testing
        assertThatCode(()->this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY))
                .doesNotThrowAnyException();
        Page<Player> page = this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY);
        assertThat(page).isNotNull();
        assertThat(page.getNumber()).isEqualTo(pageNumber);
        assertThat(page.getTotalElements()).isEqualTo(totalElements);
        assertThat(page.getNumberOfElements()).isEqualTo(totalElements);

        int totalPages = (int) Math.ceil((double) totalElements / (double) pageSize);
        assertThat(page.getTotalPages()).isEqualTo(totalPages);

        for(Player player : page.getContent()){

            assertThat(player.getFirstName()).contains(firstNamePrefix);
        }
    }

    @Test
    void getPlayerRepo_eightPlayersInRepoAndPageNumberIsOneAndPageSizeIsMaxInteger_shouldFail(){
        List<Player> players = new ArrayList<>();
        int totalElements = 8;

        for(int i = 0; i < totalElements; i++){

            Player player = Player.playerBuilder().firstName("foo"+i).build();
            players.add(player);
        }

        int pageNumber = 1;
        int pageSize = Integer.MAX_VALUE;

        //stubbing
        Mockito.when(this.playerRepository.findByIsDeleted(false)).thenReturn(players);

        //testing
        assertThatThrownBy(()->this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("page number")
                .hasMessageContaining("too large");
    }

    @Test
    void getPlayerRepo_pageNumberIsNegative_shouldFail(){
        int pageNumber = -1;
        int pageSize = 3;

        assertThatThrownBy(()->this.playerService.getPlayerRepo(pageNumber, pageSize, DEFAULT_SORT_BY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("page number is negative");
    }

    @Test
    void createPlayer_playerDidNotExist_shouldSucceed(){

        Player player = Player.playerBuilder().firstName("silly").lastName("goose").build();
        List<Player> existingCopiesInRepo = new ArrayList<>(); // empty list

        //stubbing
        Mockito.when(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false)).thenReturn(existingCopiesInRepo);
        Mockito.when(this.playerRepository.save(player)).thenReturn(player);

        //Test
        assertThatCode(()-> this.playerService.createPlayer(player)).doesNotThrowAnyException();

        Player savedPlayer = this.playerService.createPlayer(player);
        assertThat(Player.haveSameData(player, savedPlayer));
    }

    @Test
    void createPlayer_playerDidExist_shouldFail(){

        Player player = Player.playerBuilder().firstName("silly").lastName("goose").build();
        List<Player> existingCopiesInRepo = new ArrayList<>(Collections.singletonList(player));

        //stubbing
        Mockito.when(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false)).thenReturn(existingCopiesInRepo);

        Mockito.when(this.playerRepository.save(player)).thenReturn(player);

        //Test
        assertThatCode(()-> this.playerService.createPlayer(player)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining(player.fullName())
                .hasMessageContaining("to be created")
                .hasMessageContaining("already exists in repo.");
    }

    @Test
    void deletePlayer_playerDidExist_shouldSucceed(){

        Player player = Player.playerBuilder().firstName("silly").lastName("goose").build();
        List<Player> existingCopiesInRepo = new ArrayList<>(Collections.singletonList(player));

        //stubbing
        Mockito.when(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false)).thenReturn(existingCopiesInRepo);

        Mockito.when(this.playerRepository.save(player)).thenReturn(player);

        //test
        assertThatCode(()-> this.playerService.deletePlayer(player)).doesNotThrowAnyException();
    }

    @Test
    void deletePlayer_playerDidNotExist_shouldFail(){

        Player player = Player.playerBuilder().firstName("silly").lastName("goose").build();
        List<Player> existingCopiesInRepo = new ArrayList<>();

        //stubbing
        Mockito.when(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false)).thenReturn(existingCopiesInRepo);
        Mockito.when(this.playerRepository.save(player)).thenReturn(player);

        //test
        assertThatCode(()-> this.playerService.deletePlayer(player)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining(player.fullName())
                .hasMessageContaining("DNE");
    }
}
