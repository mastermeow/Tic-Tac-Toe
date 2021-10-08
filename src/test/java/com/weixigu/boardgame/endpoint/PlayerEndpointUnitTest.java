package com.weixigu.boardgame.endpoint;

import com.weixigu.boardgame.domain.Player;
import com.weixigu.boardgame.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class PlayerEndpointUnitTest {

    private PlayerEndpoint playerEndpoint;

    @Mock
    private PlayerService playerService;

    @BeforeEach
    void setUp(){

        //Initialize fields annotated with Mockito annotations (e.g. @Mock)
        MockitoAnnotations.initMocks(this);

        //Instantiate an instance of the class to be tested.
        this.playerEndpoint = new PlayerEndpoint(playerService);
    }

    @Test
    void mockSetUpTest(){
        assertThat(this.playerService).isNotNull();
        assertThat(this.playerEndpoint).isNotNull();
    }

    @Test
    void validatePlayer_validDataFormat_shouldSucceed(){

        Player player = Player.playerBuilder().firstName("foo").lastName("bar").build();

        assertThatCode(()-> this.playerEndpoint.validatePlayer(player))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePlayer_firstNameIsEmpty_shouldFail() {

        Player player = Player.playerBuilder().firstName("").lastName("bar").build();

        assertThatThrownBy(()-> this.playerEndpoint.validatePlayer(player))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("firstName") //firstName is empty or white spaces
                .hasMessageNotContaining("lastName");
    }

    @Test
    void getPlayerRepo_requestParamsAreValidWithRespectToPreloadedPlayers_shouldSucceed(){

        int pageNumber = 0;
        int pageSize = 3; //It's also set to be the number of total elements.
        String sortBy = "id";

        //stubbing
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy));

        List<Player> allPlayers = new ArrayList<>();

        for(int i = 0; i < pageSize; i++){
            Player player = Player.playerBuilder().firstName("Sample"+i).build();
            allPlayers.add(player);
        }

        Page<Player> expectedPage = new PageImpl<>(allPlayers, pageable, allPlayers.size());

        Mockito.when(this.playerService.getPlayerRepo(pageNumber, pageSize, sortBy)).thenReturn(expectedPage);

        //testing
        ResponseEntity<?> responseEntity = this.playerEndpoint.getPlayerRepo(String.valueOf(pageNumber),
                String.valueOf(pageSize), sortBy);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isEqualTo(expectedPage);
    }

    @Test
    void getPlayerRepo_requestParamsAreInvalidWithRespectToPreloadedPlayers_shouldFail(){

        String pageNumber = "0";
        String pageSize = Integer.MAX_VALUE+"0";
        String sortBy = "id";

        //testing
        ResponseEntity<?> responseEntity = this.playerEndpoint.getPlayerRepo(pageNumber, pageSize, sortBy);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isInstanceOf(String.class);

        String errorMessage = (String) responseBody;
        assertThat(errorMessage).contains("Request param 'size' is too large");
    }

    @Test
    void createPlayer_validDataFormat_shouldSucceed() {

        Player player = Player.playerBuilder().firstName("captain").lastName("meow").build();

        //stubbing
        Mockito.when(this.playerService.createPlayer(player)).thenReturn(player);

        //test
        ResponseEntity<?> responseEntity = this.playerEndpoint.createPlayer(player);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(Player.class);

        Player savedPlayer = (Player) responseBody;
        assertThat(savedPlayer.getFirstName()).isEqualTo(player.getFirstName());
        assertThat(savedPlayer.getLastName()).isEqualTo(player.getLastName());
    }

    @Test
    void createPlayer_lastNameIsEmpty_shouldFail() {

        Player player = Player.playerBuilder().firstName("captain").lastName("").build();

        //stubbing
        Mockito.when(this.playerService.createPlayer(player)).thenReturn(player);

        //test
        ResponseEntity<?> responseEntity = this.playerEndpoint.createPlayer(player);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String fieldErrorMessage = (String) responseBody;
        assertThat(fieldErrorMessage).contains("lastName [field.required]"); // see PlayerValidator.java.
    }

    @Test
    void deletePlayer_validDataFormat_shouldSucceed(){

        Player player = Player.playerBuilder().firstName("fluffy").lastName("bunny").build();

        //stubbing
        String messageIfSucceed = "Succeeded in deleting player.";
        Mockito.when(this.playerService.deletePlayer(player)).thenReturn(messageIfSucceed);

        //test
        ResponseEntity<?> responseEntity = this.playerEndpoint.deletePlayer(player);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String message = (String) responseBody;
        assertThat(message).isEqualTo(messageIfSucceed);
    }

    @Test
    void deletePlayer_firstNameIsWhiteSpace_shouldFail(){

        Player player = Player.playerBuilder().firstName("  ").lastName("bunny").build();

        //stubbing
        String messageIfSucceed = "Succeeded in deleting player.";
        Mockito.when(this.playerService.deletePlayer(player)).thenReturn(messageIfSucceed);

        //test
        ResponseEntity<?> responseEntity = this.playerEndpoint.deletePlayer(player);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Object responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isInstanceOf(String.class);

        String message = (String) responseBody;
        assertThat(message).contains("firstName [field.required]"); //See PlayerValidator.java
    }
}
