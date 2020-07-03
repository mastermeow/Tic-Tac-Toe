package com.weixigu.reactandspringdatarest.validation;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

//Spring Validator API that validates Player instances.
public class PlayerValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    @Override
    public boolean supports(Class<?> clazz){
        return Player.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors){

        LOGGER.info("Calling (Spring Validation API) PlayerValidator to validate Player.");

        Player player = (Player) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required");

        ValidationUtils.rejectIfEmpty(errors, "numTicTacToeLoss", "field.required");
        ValidationUtils.rejectIfEmpty(errors, "numTicTacToeWin", "field.required");
        ValidationUtils.rejectIfEmpty(errors, "score", "field.required");

        String firstName = player.getFirstName();

        if(firstName.trim().length() != firstName.length()){
            errors.rejectValue("firstName", "untrimmed.string");
        }

        String lastName = player.getLastName();

        if(lastName.trim().length() != lastName.length()){
            errors.rejectValue("lastName", "untrimmed.string");
        }

        String nickName = player.getNickName();

        if(nickName != null && (nickName.trim().length() != nickName.length())){
            errors.rejectValue("nickName", "untrimmed.string");
        }

        Integer losses = player.getNumTicTacToeLoss();

        if(losses < 0){
            errors.rejectValue("numTicTacToeLoss", "negative.value");
        }

        Integer wins = player.getNumTicTacToeWin();

        if(wins < 0){
            errors.rejectValue("numTicTacToeWin", "negative.value");
        }

        Integer score = player.getScore();

        if(!score.equals(wins - losses)){
            errors.rejectValue("score", "wrong.value");
        }
    }
}
