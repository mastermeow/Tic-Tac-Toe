package com.weixigu.boardgame.service;

import com.weixigu.boardgame.BoardGameApplication;
import com.weixigu.boardgame.domain.Player;
import com.weixigu.boardgame.repo.IPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

//Service provider that performs CRUD operations on Player repository.
@Service("playerService")
public class PlayerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardGameApplication.class);

    private final IPlayerRepository playerRepository;

    @Autowired
    public PlayerService(IPlayerRepository playerRepository){

        this.playerRepository = playerRepository;
    }

    private Page<Player> getPageFromListAndPageable(List<Player> allElements, Pageable pageable){

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        int fromIndex = pageSize*pageNumber;
        int startOfTheNextPage = pageSize*(pageNumber+1);
        int toIndex = Math.min(allElements.size(), startOfTheNextPage);

        List<Player> playersOnPage = allElements.subList(fromIndex, toIndex); //.subList(i, j) contains: i,..., j-1.

        return new PageImpl<>(playersOnPage, pageable, allElements.size());
    }

    //Return a Page of Player entities meeting the paging restriction and isDeleted = false.
    public Page<Player> getPlayerRepo(int pageNumber, int pageSize, String sortBy) throws RuntimeException{

        List<Player> players = this.playerRepository.findByIsDeleted(false);

        //validate the pagination params
        if(pageNumber < 0){

            throw new RuntimeException(String.format("page number is negative: %d", pageNumber));
        }

        if(pageSize < 0){

            throw new RuntimeException(String.format("page size is negative: %d", pageSize));
        }

        int totalPages = (int) Math.ceil((double)players.size()/pageSize);

        if(pageNumber >= totalPages){

            throw new RuntimeException(String.format("page number %d (with size = %d) is too large; " +
                            "total element is %d", pageNumber, pageSize, players.size()));
        }

        //retrieve page
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy));
        Page<Player> page = this.getPageFromListAndPageable(players, pageable);
        LOGGER.info(String.format("Retrieved players (page = %d, size = %d, sort by = %s).",
                pageNumber, pageSize, sortBy));

        return page;
    }

    //throws an exception if player already has a non-deleted copy in repo.
    private void validateCreatingPlayer(Player player) throws RuntimeException{

        LOGGER.info(String.format("Validating creating player %s.", player.fullName()));

        List<Player> nonDeletedCopies = this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false);

        if(!nonDeletedCopies.isEmpty()){

            String errorMessage = String.format("Player name %s (to be created) already exists in repo.",
                    player.fullName(), nonDeletedCopies.size());

            throw new RuntimeException(errorMessage);
        }
    }

    //Create the target player in repo without overwriting any existing copy.
    //The method shall make repo.save(entity) behave the same as EntityManager.persist(), but not EntityManager.merge().
    public Player createPlayer(Player player) throws RuntimeException {

        this.validateCreatingPlayer(player);

        Player savedPlayer = this.playerRepository.save(player);
        LOGGER.info(String.format("Created player %s in repo.", savedPlayer.fullName()));

        return savedPlayer;
    }

    /**
     * Throws an exception if the given player is in one of the following cases:
     * 1) the player does not have exactly one non-deleted copy in repo;
     * 2) the record of the given player does not match the record of its non-deleted copy in repo.
     */
    private void validateDeletingPlayer(Player player) throws RuntimeException {

        LOGGER.info(String.format("Validating deleting player %s.", player.fullName()));

        List<Player> nonDeletedCopies = this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false);
        String errorMessage = null;

        if(nonDeletedCopies.size() > 1){

            errorMessage = String.format("Player name %s has %d non-deleted copies in repo.",
                    player.fullName(), nonDeletedCopies.size());

        }else if(nonDeletedCopies.isEmpty()){

            errorMessage = String.format("Player name %s (to be deleted) DNE in repo. ", player.fullName());

        }else {

            Player targetInRepo = nonDeletedCopies.get(0);

            if(!Player.haveSameData(targetInRepo, player)){

                errorMessage = String.format("Player (to be deleted) DNE in repo:\n %s", player.toString());
            }
        }

        if(errorMessage!=null){

            throw new RuntimeException(errorMessage);
        }
    }

    //Set the target player in repo as deleted. Return a message indicating if the operation succeeds or not.
    public String deletePlayer(Player player) throws RuntimeException{

        this.validateDeletingPlayer(player);

        List<Player> nonDeletedCopies = this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false);

        Player playerToBeDeleted = nonDeletedCopies.get(0);
        playerToBeDeleted.setAsDeleted();
        this.playerRepository.save(playerToBeDeleted);

        String succeedingMessage = String.format("Marked player %s as deleted in repo.", player.fullName());
        LOGGER.info(succeedingMessage);

        return succeedingMessage;
    }

    //Replace an "old" player with a "new" player. Return the "new" player if succeeded; an error message if failed.
    public Player replaceOldPlayerWithNewPlayer(Player oldPlayer, Player newPlayer) throws RuntimeException{

        if(Player.haveSameData(oldPlayer, newPlayer)){
            return oldPlayer;
        }

        this.validateDeletingPlayer(oldPlayer);
        if(!Player.haveSameFirstNameAndLastNameAllIgnoreCase(oldPlayer, newPlayer)){
            this.validateCreatingPlayer(newPlayer);
        }

        oldPlayer.setAsDeleted();
        this.playerRepository.save(oldPlayer);

        Player savedNewPlayer = this.playerRepository.save(newPlayer);

        LOGGER.info(String.format("Replaced player %s with player %s", oldPlayer.fullName(), newPlayer.fullName()));

        return savedNewPlayer;
    }

    //Throws an exception if player is not in one of the three cases: win, lose, or draw.
    private void validateSingleGameRecord(Player player) throws RuntimeException{

        LOGGER.info(String.format("Validating the single-game record of player %s", player.fullName()));

        Integer wins = player.getNumTicTacToeWin();
        Integer losses = player.getNumTicTacToeLoss();
        Integer draws = player.getNumTicTacToeDraw();

        boolean isRecordValid = wins >= 0 && losses >=0 && draws >=0 && wins+losses+draws == 1;

        if(!isRecordValid){

            String errorMessage = String.format("Player %s has invalid single-game record. ", player.fullName());
            throw new RuntimeException(errorMessage);
        }
    }

    //Throws an exception if unable to merge the records of the given player and its non-deleted copy in repo.
    private void validateSavingRecord(Player player) throws RuntimeException{

        LOGGER.info(String.format("Validating saving record of player %s", player.fullName()));

        this.validateSingleGameRecord(player);

        LOGGER.info(String.format("Validating merging the records of player %s and its non-deleted copy in repo",
                player.fullName()));

        List<Player> nonDeletedCopies = this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false);

        //Ensure that the player to be saved has at most one non-deleted copy in repo.
        if(nonDeletedCopies.size() > 1){

            String errorMessage = String.format("Player %s has %d (> 1) non-deleted copies in repo.",
                    player.fullName(), nonDeletedCopies.size());

            throw new RuntimeException(errorMessage);
        }

        //Ensure that the player's existing record has not reach its numerical upper bound.
        if(nonDeletedCopies.size() == 1){

            Player targetInRepo = nonDeletedCopies.get(0);

            boolean isNumWinMax = targetInRepo.getNumTicTacToeWin().equals(Integer.MAX_VALUE);
            boolean isNumDrawMax = targetInRepo.getNumTicTacToeDraw().equals(Integer.MAX_VALUE);
            boolean isNumLossMax = targetInRepo.getNumTicTacToeLoss().equals(Integer.MAX_VALUE);

            boolean playerWon = player.getNumTicTacToeWin() == 1;
            boolean playerDrew = player.getNumTicTacToeDraw() == 1;
            boolean playerLost = player.getNumTicTacToeLoss() == 1;

            String errorMessage = null;

            if(playerWon && isNumWinMax){
                errorMessage = String.format("Unable to increase # of wins because player won too many (> %d) times",
                        Integer.MAX_VALUE);

            }else if(playerDrew && isNumDrawMax){
                errorMessage = String.format("Unable to increase # of draws because player drew too many (> %d) times",
                        Integer.MAX_VALUE);

            }else if(playerLost && isNumLossMax){
                errorMessage = String.format("Unable to increase # of losses because player lost too many (> %d) times",
                        Integer.MAX_VALUE);
            }

            if(errorMessage!=null){
                throw new RuntimeException(errorMessage);
            }
        }
    }

    /**
     * Given a player with a single-game record,
     * if the player does not have any non-deleted copy in repo, save the player to repo;
     * otherwise, merge the single-game record of the player with the existing record of the player.
     */
    public String saveRecord(Player player) throws RuntimeException{

        //validation
        this.validateSavingRecord(player);

        //extract the single-game record
        Integer wins = player.getNumTicTacToeWin();
        Integer losses = player.getNumTicTacToeLoss();
        Integer draws = player.getNumTicTacToeDraw();
        String nickName = player.getNickName();

        //extract the previous record in repo.
        List<Player> existingCopies = this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), false);

        if(!existingCopies.isEmpty()){

            Player existingCopy = existingCopies.get(0);

            //merge the records
            wins += existingCopy.getNumTicTacToeWin();
            losses += existingCopy.getNumTicTacToeLoss();
            draws += existingCopy.getNumTicTacToeDraw();

            //Update nickName based on client's most recent request.
            if(existingCopy.getNickName().trim().length()>0){
                nickName = existingCopy.getNickName();
            }

            //mark the previous record in repo as deleted.
            existingCopy.setAsDeleted();

            this.playerRepository.save(existingCopy);

        }

        //Store the updated record in repo.
        Player newCopy = Player.playerBuilder().firstName(player.getFirstName())
                .lastName(player.getLastName()).nickName(nickName)
                .numTicTacToeLoss(losses).numTicTacToeWin(wins).numTicTacToeDraw(draws).build();

        Player savedNewCopy = this.playerRepository.save(newCopy);

        String message = String.format("Saved the record of player %s.", savedNewCopy.fullName());
        LOGGER.info(message);

        return message;
    }
}
