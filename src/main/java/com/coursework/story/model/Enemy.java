package com.coursework.story.model;

import jakarta.persistence.Embeddable;


@Embeddable
public class Enemy {
    private String enemyName;
    private int enemySkill;
    private int enemyStamina;

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
}
