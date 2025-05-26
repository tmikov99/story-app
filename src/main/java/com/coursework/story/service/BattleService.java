package com.coursework.story.service;

import com.coursework.story.model.*;
import com.coursework.story.repository.BattleRepository;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.stream.IntStream;

@Service
public class BattleService {

    private final BattleRepository battleRepository;

    public BattleService(BattleRepository battleRepository) {
        this.battleRepository = battleRepository;
    }

    public Battle startBattle(Page page, PlayerStats stats, Playthrough playthrough) {
        Enemy enemy = page.getEnemy();

        Battle battle = new Battle();
        battle.setEnemyName(enemy.getEnemyName());
        battle.setEnemySkill(enemy.getEnemySkill());
        battle.setEnemyStamina(enemy.getEnemyStamina());

        battle.setPlayerSkill(stats.getSkill());
        battle.setPlayerStamina(stats.getStamina());
        battle.setPlayerLuck(stats.getLuck());

        battle.setRoundFinalized(true);
        battle.setPlaythrough(playthrough);

        return battleRepository.save(battle);
    }

    public Battle resolveRound(Battle battle) {
        int playerRoll = rollDice(2);
        int enemyRoll = rollDice(2);
        int playerAttack = playerRoll + battle.getPlayerSkill();
        int enemyAttack = enemyRoll + battle.getEnemySkill();

        String log;

        if (playerAttack > enemyAttack) {
            battle.setPendingDamageTarget(DamageTarget.ENEMY);
            battle.setPendingDamageAmount(2);
            log = "You hit the enemy for 2 damage.";
        } else if (enemyAttack > playerAttack) {
            battle.setPendingDamageTarget(DamageTarget.PLAYER);
            battle.setPendingDamageAmount(2);
            log = "Enemy hits you for 2 damage.";
        } else {
            battle.setPendingDamageTarget(DamageTarget.NONE);
            battle.setPendingDamageAmount(0);
            log = "You parried each other's blows.";
        }

        battle.setLastPlayerRoll(playerRoll);
        battle.setLastEnemyRoll(enemyRoll);
        battle.setLastRoundLuckUsed(false);
        battle.setBattleLog(log);
        battle.setRoundFinalized(false);

        return battleRepository.save(battle);
    }

    public Battle applyLuck(Battle battle) {
        if (battle.getPendingDamageTarget() == DamageTarget.NONE || battle.isLastRoundLuckUsed() || battle.isRoundFinalized()) {
            throw new IllegalStateException("No eligible round for luck.");
        }

        int diceRoll = rollDice(2);
        boolean lucky = diceRoll <= battle.getPlayerLuck();
        battle.setPlayerLuck(battle.getPlayerLuck() - 1);
        battle.setLastRoundLuckUsed(true);

        String updatedLog = battle.getBattleLog();

        switch (battle.getPendingDamageTarget()) {
            case ENEMY -> {
                if (lucky) {
                    battle.setPendingDamageAmount(battle.getPendingDamageAmount() + 2);
                    updatedLog += " Lucky! Extra damage dealt.";
                } else {
                    battle.setPendingDamageAmount(Math.max(1, battle.getPendingDamageAmount() - 1));
                    updatedLog += " Unlucky... damage reduced.";
                }
            }
            case PLAYER -> {
                if (lucky) {
                    battle.setPendingDamageAmount(Math.max(1, battle.getPendingDamageAmount() - 1));
                    updatedLog += " Lucky! Damage reduced.";
                } else {
                    battle.setPendingDamageAmount(battle.getPendingDamageAmount() + 1);
                    updatedLog += " Unlucky... extra damage taken.";
                }
            }
            case NONE -> updatedLog += " Luck had no effect.";
        }

        return finalizeRound(battle, updatedLog);
    }

    public Battle finalizeWithoutLuck(Battle battle) {
        return finalizeRound(battle, battle.getBattleLog());
    }

    public Battle finalizeRound(Battle battle, String currentLog) {
        if (battle.isRoundFinalized()) return battle;

        switch (battle.getPendingDamageTarget()) {
            case ENEMY -> battle.setEnemyStamina(battle.getEnemyStamina() - battle.getPendingDamageAmount());
            case PLAYER -> battle.setPlayerStamina(battle.getPlayerStamina() - battle.getPendingDamageAmount());
            case NONE -> {}
        }

        battle.setRoundFinalized(true);
        battle.setPendingDamageAmount(0);
        battle.setPendingDamageTarget(DamageTarget.NONE);
        battle.setBattleLog(currentLog);
        return battleRepository.save(battle);
    }

    public boolean isBattleOver(Battle battle) {
        return battle.getEnemyStamina() <= 0 || battle.getPlayerStamina() <= 0;
    }

    public void finalizeBattle(Battle battle) {
        battle.setCompleted(true);
        battle.setPlayerWon(battle.getEnemyStamina() <= 0);
        battleRepository.save(battle);
    }

    private int rollDice(int number) {
        Random random = new Random();
        return IntStream.range(0, number)
                .map(i -> random.nextInt(6) + 1)
                .sum();
    }
}