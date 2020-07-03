package com.weixigu.reactandspringdatarest.domain;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Builder(toBuilder = true, builderMethodName = "playerBuilder")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Player implements Serializable {

    @Id
    @GeneratedValue
    @JsonProperty("id")
    private Long id;

    @Builder.Default
    @JsonProperty("firstName")
    private final String firstName = "Foo";

    @Builder.Default
    @JsonProperty("lastName")
    private final String lastName = "Bar";

    @Builder.Default
    @JsonProperty("nickName")
    private final String nickName = "";

    @Builder.Default
    @JsonProperty("numTicTacToeDraw")
    private final Integer numTicTacToeDraw = 0;

    @Builder.Default
    @JsonProperty("numTicTacToeLoss")
    private final Integer numTicTacToeLoss = 0;

    @Builder.Default
    @JsonProperty("numTicTacToeWin")
    private final Integer numTicTacToeWin = 0;

    //A Player object is considered as 'being deleted from the repository' if isDeleted = true.
    @Builder.Default
    @JsonProperty("deleted")
    private boolean isDeleted = false;

    public static boolean haveSameFirstNameAndLastNameAllIgnoreCase(Player player1, Player player2){
        return Objects.equals(player1.getFirstName().toLowerCase(), player2.getFirstName().toLowerCase()) &&
                Objects.equals(player1.getLastName().toLowerCase(), player2.getLastName().toLowerCase());

    }

    public static boolean haveSameData(Player player1, Player player2){
        return haveSameFirstNameAndLastNameAllIgnoreCase(player1, player2) &&
                Objects.equals(player1.getNickName(), player2.getNickName()) &&
                Objects.equals(player1.getNumTicTacToeDraw(), player2.getNumTicTacToeDraw()) &&
                Objects.equals(player1.getNumTicTacToeLoss(), player2.getNumTicTacToeLoss()) &&
                Objects.equals(player1.getNumTicTacToeWin(), player2.getNumTicTacToeWin()) &&
                (player1.isDeleted() == player2.isDeleted());
    }

    public String fullName(){
        return this.lastName+", "+this.firstName;
    }

    public void setAsDeleted(){
        this.isDeleted = true;
    }

    public Integer getScore(){
        return this.numTicTacToeWin - this.numTicTacToeLoss;
    }

    @Override
    public String toString() {
        return "Player{id=" + this.id + ", isDeleted=" + this.isDeleted+
                ", firstName=" + this.firstName +
                ", lastName=" + this.lastName + ", nickName=" + this.nickName +
                ", numTicTacToeLoss=" + this.numTicTacToeLoss + ", numTicTacToeWin" + this.numTicTacToeWin +
                ", score=" + this.getScore() + "}";
    }
}