package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String enemyName;
    private int enemySkill;
    private int enemyStamina;
    private int playerSkill;
    private int playerStamina;
    private int playerLuck;
    private Integer lastPlayerRoll;
    private Integer lastEnemyRoll;

    @Enumerated(EnumType.STRING)
    private DamageTarget pendingDamageTarget = DamageTarget.NONE;

    private int pendingDamageAmount = 0;

    private boolean roundFinalized = false;
    private String battleLog;
    private boolean lastRoundLuckUsed;
    private boolean completed;
    private boolean playerWon;

    @OneToOne
    @JoinColumn(name = "playthrough_id", nullable = false, unique = true)
    @JsonBackReference
    private Playthrough playthrough;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public void setEnemyName(String enemyName) {
        this.enemyName = enemyName;
    }

    public int getEnemySkill() {
        return enemySkill;
    }

    public void setEnemySkill(int enemySkill) {
        this.enemySkill = enemySkill;
    }

    public int getEnemyStamina() {
        return enemyStamina;
    }

    public void setEnemyStamina(int enemyStamina) {
        this.enemyStamina = enemyStamina;
    }

    public int getPlayerSkill() {
        return playerSkill;
    }

    public void setPlayerSkill(int playerSkill) {
        this.playerSkill = playerSkill;
    }

    public int getPlayerStamina() {
        return playerStamina;
    }

    public void setPlayerStamina(int playerStamina) {
        this.playerStamina = playerStamina;
    }

    public int getPlayerLuck() {
        return playerLuck;
    }

    public void setPlayerLuck(int playerLuck) {
        this.playerLuck = playerLuck;
    }

    public Integer getLastPlayerRoll() {
        return lastPlayerRoll;
    }

    public void setLastPlayerRoll(Integer lastPlayerRoll) {
        this.lastPlayerRoll = lastPlayerRoll;
    }

    public Integer getLastEnemyRoll() {
        return lastEnemyRoll;
    }

    public void setLastEnemyRoll(Integer lastEnemyRoll) {
        this.lastEnemyRoll = lastEnemyRoll;
    }


    public DamageTarget getPendingDamageTarget() {
        return pendingDamageTarget;
    }

    public void setPendingDamageTarget(DamageTarget pendingDamageTarget) {
        this.pendingDamageTarget = pendingDamageTarget;
    }

    public int getPendingDamageAmount() {
        return pendingDamageAmount;
    }

    public void setPendingDamageAmount(int pendingDamageAmount) {
        this.pendingDamageAmount = pendingDamageAmount;
    }

    public boolean isRoundFinalized() {
        return roundFinalized;
    }

    public void setRoundFinalized(boolean roundFinalized) {
        this.roundFinalized = roundFinalized;
    }

    public String getBattleLog() {
        return battleLog;
    }

    public void setBattleLog(String battleLog) {
        this.battleLog = battleLog;
    }

    public boolean isLastRoundLuckUsed() {
        return lastRoundLuckUsed;
    }

    public void setLastRoundLuckUsed(boolean lastRoundLuckUsed) {
        this.lastRoundLuckUsed = lastRoundLuckUsed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }

    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
    }

    public Playthrough getPlaythrough() {
        return playthrough;
    }

    public void setPlaythrough(Playthrough playthrough) {
        this.playthrough = playthrough;
    }
}
