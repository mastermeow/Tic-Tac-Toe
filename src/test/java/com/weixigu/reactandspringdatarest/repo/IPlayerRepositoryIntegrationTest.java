package com.weixigu.reactandspringdatarest.repo;

import com.weixigu.reactandspringdatarest.ReactAndSpringDataRESTApplication;
import com.weixigu.reactandspringdatarest.domain.Player;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IPlayerRepositoryIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactAndSpringDataRESTApplication.class);

    @Autowired
    private IPlayerRepository playerRepository; 
    
    @Test
    @Transactional
    public void findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted_ensureExistingPlayerDoesExist_shouldSucceed(){

        LOGGER.info("Integration test: " +
                "findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted_ensureExistingPlayerDoesExist_shouldSucceed().");

        boolean isDeleted = false;

        Player player = Player.playerBuilder().firstName("fat").lastName("chicken")
                .isDeleted(isDeleted).build();

        assertThat(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), isDeleted))
                .doesNotContain(player);

        this.playerRepository.save(player);

        assertThat(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), isDeleted))
                .contains(player);
    }

    @Test
    @Transactional
    public void findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted_ensureDeletedPlayerIsDeleted_shouldSucceed(){

        LOGGER.info("Integration test: " +
                "findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted_ensureDeletedPlayerIsDeleted_shouldSucceed().");

        boolean isDeleted = true;

        Player player = Player.playerBuilder().firstName("skinny").lastName("duck")
                .isDeleted(isDeleted).build();
        
        assertThat(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), isDeleted))
                .doesNotContain(player);

        this.playerRepository.save(player);

        assertThat(this.playerRepository.findByFirstNameAndLastNameAllIgnoreCaseAndIsDeleted(
                player.getFirstName(), player.getLastName(), isDeleted))
                .contains(player);
    }

    @Test
    @Transactional
    public void findByIsDeleted_ensureExistingPlayerDoesExist_shouldSucceed(){

        LOGGER.info("Integration test: findByIsDeleted_ensureExistingPlayerDoesExist_shouldSucceed().");

        boolean isDeleted = false;

        Player player = Player.playerBuilder().firstName("fat").lastName("chicken")
                .isDeleted(isDeleted).build();

        assertThat(this.playerRepository.findByIsDeleted(isDeleted)).doesNotContain(player);

        this.playerRepository.save(player);

        assertThat(this.playerRepository.findByIsDeleted(isDeleted)).contains(player);
    }

    @Test
    @Transactional
    public void findByIsDeleted_ensureDeletedPlayerIsDeleted_shouldSucceed(){

        LOGGER.info("Integration test: findByIsDeleted_ensureDeletedPlayerIsDeleted_shouldSucceed().");

        boolean isDeleted = true;

        Player player = Player.playerBuilder().firstName("skinny").lastName("duck")
                .isDeleted(isDeleted).build();

        assertThat(this.playerRepository.findByIsDeleted(isDeleted)).doesNotContain(player);

        this.playerRepository.save(player);

        assertThat(this.playerRepository.findByIsDeleted(isDeleted)).contains(player);
    }
}
