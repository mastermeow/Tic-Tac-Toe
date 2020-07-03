package com.weixigu.reactandspringdatarest.endpoint;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.Player;
import com.weixigu.reactandspringdatarest.service.PlayerService;
import com.weixigu.reactandspringdatarest.validation.PlayerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;

@RestController
@RequestMapping("/players")
public class PlayerEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    private static final String DEFAULT_PAGE_NUMBER = "0";
    private static final String DEFAULT_PAGE_SIZE = "2147483647"; //Integer.MAX_VALUE
    private static final String DEFAULT_SORT_BY = "id";

    private static final Set<String> SORT_PARAMS = new HashSet<>(Arrays.asList("id", "firstName", "lastName",
            "nickName", "numTicTacToeDraw", "numTicTacToeLoss", "numTicTacToeWin", "score"));
    private static final String PLAYER_STATUS_OLD = "old";
    private static final String PLAYER_STATUS_NEW = "new";

    private final PlayerService playerService;

    @Autowired
    public PlayerEndpoint(PlayerService playerService){
        this.playerService = playerService;
    }

    //Throw a RuntimeException if the Player object is malformed.
    public void validatePlayer(Player player) throws RuntimeException{

        DataBinder binder = new DataBinder(player);
        binder.setValidator(new PlayerValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if(bindingResult.hasErrors()){

            String errorMessage = this.printFieldErrors(bindingResult);
            throw new RuntimeException(errorMessage);
        }
    }

    //Return field errors from the binding result (of binding Spring Validator to Web Data) in String format.
    private String printFieldErrors(BindingResult bindingResult){

        StringBuilder errorMessage = new StringBuilder();

        if(bindingResult.hasErrors()){

            errorMessage.append("Field error(s): ");
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for(FieldError fieldError : fieldErrors){
                errorMessage.append(String.format("%s [%s]", fieldError.getField(), fieldError.getCode()));
            }
        }

        return errorMessage.toString();
    }

    //throws a RuntimeException if the input arguments are illegal (malformed) arguments
    private void validateArgsOfGetPlayerRepo(String pageNumber, String pageSize, String sortBy) throws RuntimeException{

        String regexDigitsOnly = "\\d+"; //(non-negative) digit(s) only.

        if(!pageNumber.matches(regexDigitsOnly)){

            throw new RuntimeException(String.format("Request param 'page' should be digits only: %s", pageNumber));
        }

        BigInteger pageNumberBigInt = new BigInteger(pageNumber);
        BigInteger maxOfPageAndSize = BigInteger.valueOf(Integer.MAX_VALUE);

        if(pageNumberBigInt.compareTo(maxOfPageAndSize) > 0){

            throw new RuntimeException(String.format("Request param 'page' is too large (> %d): %s.",
                    Integer.MAX_VALUE, pageNumber));
        }

        if(!pageSize.matches(regexDigitsOnly)){

            throw new RuntimeException(String.format("Request param 'size' should be digits only: %s", pageSize));
        }

        BigInteger pageSizeBigInt = new BigInteger(pageSize);

        if(pageSizeBigInt.compareTo(maxOfPageAndSize) > 0){

            throw new RuntimeException(String.format("Request param 'size' is too large (> %d): %s.",
                    Integer.MAX_VALUE, pageSize));
        }

        if(!SORT_PARAMS.contains(sortBy)){

            throw new RuntimeException(String.format("Request param 'sortBy' is invalid: %s", sortBy));
        }
    }

    //Return a Page of Player entities meeting the paging restriction.
    @GetMapping(value="/repository")
    public ResponseEntity<?> getPlayerRepo(
            @RequestParam(defaultValue= DEFAULT_PAGE_NUMBER, name="page") String pageNumber,
            @RequestParam(defaultValue= DEFAULT_PAGE_SIZE, name="size") String pageSize,
            @RequestParam(defaultValue= DEFAULT_SORT_BY, name ="sortBy") String sortBy){

        try{
            validateArgsOfGetPlayerRepo(pageNumber, pageSize, sortBy);
            Page<Player> page = this.playerService.getPlayerRepo(Integer.parseInt(pageNumber),
                    Integer.parseInt(pageSize), sortBy);
            return ResponseEntity.status(HttpStatus.OK).body(page);

        }catch (RuntimeException exception){

            String errorMessage = exception.getMessage();
            LOGGER.info(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    //Create the given player in repo without overwriting any existing copy.
    @PostMapping("/create-player")
    public ResponseEntity<?> createPlayer(@NotNull @RequestBody Player player){

        try{

            this.validatePlayer(player);
            Player savedPlayer = this.playerService.createPlayer(player);
            return ResponseEntity.status(HttpStatus.OK).body(savedPlayer);

        }catch (RuntimeException exception){

            String errorMessage = exception.getMessage();
            LOGGER.info(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    //Set the given player (which exists in repo) as 'isDeleted = true'.
    @PostMapping("/delete-player")
    public ResponseEntity<String> deletePlayer(@NotNull @RequestBody Player player){

        try{

            this.validatePlayer(player);
            String succeedingMessage = this.playerService.deletePlayer(player);
            return ResponseEntity.status(HttpStatus.OK).body(succeedingMessage);

        }catch (RuntimeException exception){

            String failingMessage = exception.getMessage();
            LOGGER.info(failingMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failingMessage);
        }

    }

    //Replace an "old" player with a "new" player. Return the "new" player if succeeded; an error message if failed.
    @PostMapping("/replace-player")
    public ResponseEntity<?> replacePlayer(@NotNull @RequestBody Map<String, Player> statusToPlayer){

        //verify existence of web data.
        Player oldPlayer = statusToPlayer.getOrDefault(PLAYER_STATUS_OLD, null);
        Player newPlayer = statusToPlayer.getOrDefault(PLAYER_STATUS_NEW, null);

        if(oldPlayer == null || newPlayer == null){

            String errorMessage = "Replace player: 'old' player and/or 'new' player is null.";
            LOGGER.info(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }

        //Validate web data (Spring Validator can't validate nested data structures).
        try{

            this.validatePlayer(oldPlayer);
            this.validatePlayer(newPlayer);
            Player savedNewPlayer = this.playerService.replaceOldPlayerWithNewPlayer(oldPlayer, newPlayer);
            return ResponseEntity.status(HttpStatus.OK).body(savedNewPlayer);

        }catch (RuntimeException exception){

            String errorMessage = exception.getMessage();
            LOGGER.info(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    //Save the record of a player based on whether the player won/lost/drew.
    @PostMapping("/save-record")
    public ResponseEntity<String> saveRecord(@NotNull @RequestBody Player player){

        try{

            String succeedingMessage = this.playerService.saveRecord(player);
            return ResponseEntity.status(HttpStatus.OK).body(succeedingMessage);

        }catch (RuntimeException exception){

            String errorMessage = exception.getMessage();
            LOGGER.info(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}