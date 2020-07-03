package com.weixigu.reactandspringdatarest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

//A TicTacToe object presents a move made in the Tic-Tac-Toe game.
@Entity
@Builder(toBuilder = true, builderMethodName = "tictactoeBuilder")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TicTacToe implements Serializable {
    public static final int BOARD_LENGTH = 3;

    @Id
    @GeneratedValue
    @JsonProperty("id")
    private  Long id;

    @Getter(AccessLevel.NONE)
    @Builder.Default
    @JsonProperty("xNext")
    private final boolean xNext = true; //xNext = true if Player X's turn, false if O's turn. X always plays first.

    @Builder.Default
    @JsonProperty("board")
    private final String[][] board = new String[TicTacToe.BOARD_LENGTH][TicTacToe.BOARD_LENGTH];

    @Builder.Default
    @JsonProperty("currentGame")
    private boolean isCurrentGame = true;

    public void removeFromCurrentGame(){
        this.isCurrentGame = false;
    }

    /**
     * @return Tic-Tac-Toe winner ("X" or "O"), i.e. a player who has
     * 3 of their marks in a row (horizontally, vertically, or diagonally)
     * if a winner has come out; null otherwise.
     */
    public String getWinner(){
        int n = BOARD_LENGTH;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.board[i][j] != null) {
                    if (i < n - 2 && this.board[i][j].equals(this.board[i + 1][j]) &&
                            this.board[i][j].equals(this.board[i + 2][j])) {
                        return this.board[i][j];

                    } else if (j < n - 2 && this.board[i][j].equals(this.board[i][j + 1]) &&
                            this.board[i][j].equals(this.board[i][j + 2])) {
                        return this.board[i][j];

                    } else if (i < n - 2 && j < n - 2 && this.board[i][j].equals(this.board[i + 1][j + 1]) &&
                            this.board[i][j].equals(this.board[i + 2][j + 2])) {
                        return this.board[i][j];

                    } else if (i >= 2 && j < n - 2 && this.board[i][j].equals(this.board[i - 1][j + 1]) &&
                            this.board[i][j].equals(this.board[i - 2][j + 2])) {
                        return this.board[i][j];
                    }
                }
            }
        }
        return null;
    }

    public TicTacToe copy(){
        return TicTacToe.tictactoeBuilder().xNext(this.xNext).board(this.board).build();
    }

    public String printBoard(){
        StringBuilder myBoard = new StringBuilder("board=[");

        for(int i = 0; i < TicTacToe.BOARD_LENGTH; i++){
            myBoard.append("[");

            for(int j = 0; j < TicTacToe.BOARD_LENGTH; j++){
                myBoard.append(this.board[i][j]).append(", ");
            }

            myBoard.append("], ");
        }

        myBoard.append("]");

        return myBoard.toString();
    }

    @Override
    public String toString(){
        return "TicTacToe{id"+this.id+", xNext="+this.xNext
                + ", winner="+this.getWinner()+", "+this.printBoard()+"}";
    }
}
