package com.weixigu.reactandspringdatarest.endpoint;

import com.weixigu.reactandspringdatarest.domain.TicTacToe;
import com.weixigu.reactandspringdatarest.service.TicTacToeService;
import com.weixigu.reactandspringdatarest.validation.TicTacToeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/tictactoes")
public class TicTacToeEndpoint {

    private final TicTacToeService ticTacToeService;

    @Autowired
    public TicTacToeEndpoint(TicTacToeService ticTacToeService){

        this.ticTacToeService = ticTacToeService;
    }

    //Bind request parameters to Spring Validator to detect malformed data.
    @InitBinder("ticTacToe")
    protected void initBinder(@NotNull WebDataBinder binder) {

        binder.addValidators(new TicTacToeValidator());
    }

    //Return field errors from the binding result (of binding Spring Validator to Web Data) in String format.
    private String printFieldErrors(@NotNull BindingResult bindingResult){
        StringBuilder errorMessage = new StringBuilder();

        if(bindingResult.hasErrors()){

            errorMessage.append("Field error(s): ");
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for(FieldError fieldError : fieldErrors){

                errorMessage.append(fieldError.getField()).append(" [").append(fieldError.getCode()).append("]; ");
            }
        }

        return errorMessage.toString();
    }

    //Set 'isCurrentGame' = false for all TicTacToe objects in repo.
    @PostMapping("/reset-game")
    public ResponseEntity<String> resetGame(){

        String message = this.ticTacToeService.resetGame();
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    //Return ticTacToe if successfully saved it to repo; otherwise, return an error message.
    @PostMapping("/save-move")
    public ResponseEntity<?> saveMove(@NotNull @Validated @RequestBody TicTacToe ticTacToe, BindingResult bindingResult){

        if(bindingResult.hasErrors()){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(this.printFieldErrors(bindingResult));
        }

        try{

            TicTacToe savedMove = this.ticTacToeService.saveMove(ticTacToe);
            return ResponseEntity.status(HttpStatus.OK).body(savedMove);

        }catch (RuntimeException exception){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    //Return a TicTacToe object if successfully retrieved this "move"; otherwise, return an error message.
    @PostMapping("/view-prev-move")
    public ResponseEntity<?> viewPrevMove(@RequestParam(name="move")String indexOfMove){

        try {

            TicTacToe prevMove = this.ticTacToeService.viewPrevMove(Integer.parseInt(indexOfMove));
            return ResponseEntity.status(HttpStatus.OK).body(prevMove);

        }catch (RuntimeException exception){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    //Return a TicTacToe object if successfully revert the game to this "move"; otherwise, return an error message.
    @PostMapping("/revert-to-prev-move")
    public ResponseEntity<?> revertToPrevMove(@RequestParam(name="move") String indexOfMove){

        try {

            TicTacToe prevMove = this.ticTacToeService.revertToPrevMove(Integer.parseInt(indexOfMove));
            return ResponseEntity.status(HttpStatus.OK).body(prevMove);

        }catch (RuntimeException exception){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }
}
