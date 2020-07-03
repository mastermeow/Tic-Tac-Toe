package com.weixigu.reactandspringdatarest.service;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.TicTacToe;
import com.weixigu.reactandspringdatarest.repo.ITicTacToeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

//Service provider that performs CRUD operations on TicTacToe repository.
@Service("tictactoeService")
public class TicTacToeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    private final ITicTacToeRepository repository;

    @Autowired
    public TicTacToeService(ITicTacToeRepository repository){

        this.repository = repository;
    }

    // Set the field 'isCurrentGame' = true for all TicTacToe moves stored in repo.
    public String resetGame(){

        List<TicTacToe> prevMoves = this.repository.findByIsCurrentGame(true);

        if(!prevMoves.isEmpty()){

            for(TicTacToe move : prevMoves){

                move.removeFromCurrentGame();
                this.repository.save(move);
            }
        }

        String message = "Reset Tic-Tac-Toe game by removing all existing moves.";
        LOGGER.info(message);

        return message;
    }

    //Return ticTacToe if successfully saved it to repo; otherwise, return a string type error message.
    public TicTacToe saveMove(TicTacToe ticTacToe) throws RuntimeException{

        if(!ticTacToe.isCurrentGame()){

            throw new RuntimeException("The move (to be saved) is NOT of current game.");
        }

        TicTacToe savedMove = this.repository.save(ticTacToe);
        LOGGER.info("Saved a move made in Tic-Tac-Toe.");

        return savedMove;
    }

    //Throws a RuntimeException if the given index of a move in Tic-Tac-Toe is invalid.
    private void validateIndexOfMove(int indexOfMove) throws RuntimeException{

        if(indexOfMove < 0){

            throw new RuntimeException(String.format("The index %d of the target move is negative.", indexOfMove));
        }

        List<TicTacToe> currentGame = this.repository.findByIsCurrentGame(true);

        if(indexOfMove > currentGame.size()-1){

            throw new RuntimeException(String.format("The index %d of the target move is too large.", indexOfMove));
        }
    }

    //Retrieve a move of the given index in the current TicTacToe game without reverting the game to this move.
    public TicTacToe viewPrevMove(int indexOfMove) throws RuntimeException {

        this.validateIndexOfMove(indexOfMove);

        List<TicTacToe> currentGame = this.repository.findByIsCurrentGame(true);
        TicTacToe move = currentGame.get(indexOfMove);

        LOGGER.info(String.format("Retrieved Tic Tac Toe move #%d", indexOfMove));

        return move;
    }

    //Reverts the current TicTacToe game to a previous move that is of the given index; return the move if succeeded.
    public TicTacToe revertToPrevMove(int indexOfMove) throws RuntimeException{

        this.validateIndexOfMove(indexOfMove);

        List<TicTacToe> currentGame = this.repository.findByIsCurrentGame(true);

        TicTacToe newLastMove = currentGame.get(indexOfMove);

        //Set all the moves made after the target move as invalid.
        for(int i = indexOfMove+1; i < currentGame.size(); i++){
            TicTacToe move = currentGame.get(i);
            move.removeFromCurrentGame();
            this.repository.save(move);
        }

        LOGGER.info(String.format("Reverted Tic-Tac-Toe to move #%d", indexOfMove));

        return newLastMove;
    }
}
