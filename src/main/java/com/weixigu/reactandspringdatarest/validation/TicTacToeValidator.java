package com.weixigu.reactandspringdatarest.validation;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.TicTacToe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//Spring Validation API that validates TicTacToe instances.
public class TicTacToeValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);
    private static final Set<String> PLAYERS = new HashSet<>(Arrays.asList("X", "O"));

    @Override
    public boolean supports(Class<?> clazz){
        return TicTacToe.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors){
        LOGGER.info("Calling (Spring Validation API) TicTacToeValidator to validate TicTacToe.");

        TicTacToe ticTacToe = (TicTacToe) target;

        ValidationUtils.rejectIfEmpty(errors, "board", "field.required");

        String[][] board = ticTacToe.getBoard();

        if(board.length != TicTacToe.BOARD_LENGTH){
            errors.rejectValue("board", "wrong.size");

        }else{
            for(String[] row : board){
                if(row.length != TicTacToe.BOARD_LENGTH){
                    errors.rejectValue("board", "wrong.size");
                }
            }
        }

        String winner = ticTacToe.getWinner();

        if(winner != null && !PLAYERS.contains(winner)){
            errors.rejectValue("winner", "invalid.value");
        }
    }
}
