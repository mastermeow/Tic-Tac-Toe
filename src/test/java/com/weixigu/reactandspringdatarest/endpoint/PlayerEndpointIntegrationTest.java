package com.weixigu.reactandspringdatarest.endpoint;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.Player;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //avoid conflicts in test environment.
class PlayerEndpointIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    private static final String DEFAULT_PAGE_NUMBER = "0"; //see PlayerEndpoint.java
    private static final String DEFAULT_PAGE_SIZE = "2147483647";
    private static final String DEFAULT_SORT_BY = "id";

    private static final int NUM_OF_PRELOADED_PLAYERS = 3;
    private static final Player FIRST_PRELOADED_PLAYER = Player.playerBuilder().firstName("Rick").lastName("Sanchez")
            .nickName("Pickle Rick").numTicTacToeLoss(0).numTicTacToeWin(10).build(); //PlayerDataLoader.java

    @Autowired
    private PlayerEndpoint playerEndpoint;

    @Test
    void contextLoads(){

        assertThat(this.playerEndpoint).isNotNull();
    }

    @Test
    void validatePlayer_validDataFormat_shouldSucceed(){

        LOGGER.info("Integration test: validatePlayer_validDataFormat_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("foo").lastName("bar").build();

        assertThatCode(()-> this.playerEndpoint.validatePlayer(player))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePlayer_firstNameIsWhiteSpace_shouldFail() {

        LOGGER.info("Integration test: validatePlayer_firstNameIsWhiteSpace_shouldFail().");

        Player player = Player.playerBuilder().firstName(" ").lastName("bar").build();

        assertThatThrownBy(()-> this.playerEndpoint.validatePlayer(player))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("firstName")
                .hasMessageNotContaining("lastName");
    }

    @Test
    void validatePlayer_firstNameIsEmpty_shouldFail() {

        LOGGER.info("Integration test: validatePlayer_firstNameIsEmpty_shouldFail().");

        Player player = Player.playerBuilder().firstName("").lastName("bar").build();

        assertThatThrownBy(()-> this.playerEndpoint.validatePlayer(player))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("firstName")
                .hasMessageNotContaining("lastName");
    }

    @Test
    void validatePlayer_negativeNumTicTacToeLoss_shouldFail() {

        LOGGER.info("Integration test: validatePlayer_negativeNumTicTacToeLoss_shouldFail().");

        Player player = Player.playerBuilder().numTicTacToeLoss(-1).build();

        assertThatThrownBy(()-> this.playerEndpoint.validatePlayer(player))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("numTicTacToeLoss")
                .hasMessageContaining("negative");
    }

    @Test
    void getPlayerRepo_verifyNumOfPreloadedPlayers_shouldSucceed(){

        LOGGER.info("Integration test: getPlayerRepo_verifyNumOfPreloadedPlayers_shouldSucceed().");

        ResponseEntity<?> responseEntity = this.playerEndpoint.getPlayerRepo(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(Page.class);

        Page<?> page = (Page<?>) responseBody;
        assertThat(page.getTotalElements()).isEqualTo(NUM_OF_PRELOADED_PLAYERS);

        for(Object ob: page.getContent()){
            assertThat(ob).isInstanceOf(Player.class);
        }
    }

    @Test
    void getPlayerRepo_verifyFirstPreloadedPlayer_shouldSucceed(){

        LOGGER.info("Integration test: getPlayerRepo_verifyFirstPreloadedPlayer_shouldSucceed().");

        ResponseEntity<?> responseEntity = this.playerEndpoint.getPlayerRepo(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(Page.class);

        Page<?> page = (Page<?>) responseBody;
        assertThat(page.getTotalElements()).isEqualTo(NUM_OF_PRELOADED_PLAYERS);

        List<?> pageContent = page.getContent();
        Object ob1 = pageContent.get(0);
        assertThat(ob1).isInstanceOf(Player.class);
        assertThat(Player.haveSameData((Player)ob1, FIRST_PRELOADED_PLAYER)).isTrue();
    }

    @Test
    void getPlayerRepo_pageNumberIsGreaterThanMaxInteger_shouldFail(){

        LOGGER.info("Integration test: getPlayerRepo_pageNumberIsGreaterThanMaxInteger_shouldFail().");

        String pageNumber = "214748364700000000"; //We can use BigInteger. But I'm too lazy to refactor it.

        ResponseEntity<?> responseEntity = this.playerEndpoint.getPlayerRepo(pageNumber, DEFAULT_PAGE_SIZE,
                DEFAULT_SORT_BY);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isInstanceOf(String.class);

        String errorMessage = (String) responseBody;
        assertThat(errorMessage).contains("Request param 'page' is too large");
    }

    @Transactional
    @Test
    public void createPlayer_validDataFormat_shouldSucceed() {

        LOGGER.info("Integration test: createPlayer_validDataFormat_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("captain").lastName("meow").build();

        ResponseEntity<?> responseEntity = this.playerEndpoint.createPlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(Player.class);

        Player savedPlayer = (Player) responseBody;
        assertThat(savedPlayer.getFirstName()).isEqualTo(player.getFirstName());
        assertThat(savedPlayer.getLastName()).isEqualTo(player.getLastName());
    }

    @Transactional
    @Test
    public void createPlayer_lastNameIsEmpty_shouldFail() {

        LOGGER.info("Integration test: createPlayer_lastNameIsEmpty_shouldFail().");

        Player player = Player.playerBuilder().firstName("captain").lastName("").build();

        ResponseEntity<?> responseEntity = this.playerEndpoint.createPlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String fieldErrorMessage = (String) responseBody;
        assertThat(fieldErrorMessage).contains("lastName [field.required]"); // see PlayerValidator.java.
    }

    @Transactional
    @Test
    public void deletePlayer_validDataFormatAndPlayerExists_shouldSucceed(){

        LOGGER.info("Integration test: deletePlayer_validDataFormatAndPlayerExists_shouldSucceed().");

        Player player = Player.playerBuilder().firstName("fat").lastName("cat").build();

        this.playerEndpoint.createPlayer(player);

        ResponseEntity<?> responseEntity = this.playerEndpoint.deletePlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String message = (String) responseBody; // see error code from PlayerService.java
        assertThat(message).contains(player.fullName());
        assertThat(message).contains("deleted");
    }

    @Transactional
    @Test
    public void deletePlayer_validDataFormatAndPlayerDoesNotExist_shouldFail(){

        LOGGER.info("Integration test: deletePlayer_validDataFormatAndPlayerDoesNotExist_shouldFail().");

        Player player = Player.playerBuilder().firstName("fat").lastName("cat").build();

        this.playerEndpoint.createPlayer(player);
        this.playerEndpoint.deletePlayer(player); //player no longer exists.

        ResponseEntity<?> responseEntity = this.playerEndpoint.deletePlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String message = (String) responseBody; // see error code from PlayerService.java
        assertThat(message).contains(player.fullName());
        assertThat(message).contains("DNE");
    }

    @Transactional
    @Test
    public void deletePlayer_firstNameIsEmpty_shouldFail(){

        LOGGER.info("Integration test: deletePlayer_playerDoesNotExistInRepo_shouldFail().");

        Player player = Player.playerBuilder().firstName("").lastName("bunny").build();

        ResponseEntity<?> responseEntity = this.playerEndpoint.deletePlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String message = (String) responseBody;
        assertThat(message).contains("firstName [field.required]"); //See PlayerValidator.java
    }
}
