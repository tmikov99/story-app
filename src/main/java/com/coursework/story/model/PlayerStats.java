package com.coursework.story.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class PlayerStats {
    private int skill;
    private int stamina;
    private int luck;
    private int initialSkill;
    private int initialStamina;
    private int initialLuck;

    public PlayerStats() {}

    public PlayerStats(int skill, int stamina, int luck) {
        this.skill = skill;
        this.initialSkill = skill;
        this.stamina = stamina;
        this.initialStamina = stamina;
        this.luck = luck;
        this.initialLuck = luck;
    }

    public PlayerStats(int skill, int stamina, int luck, int initialSkill, int initialStamina, int initialLuck) {
        this.skill = skill;
        this.stamina = stamina;
        this.luck = luck;
        this.initialSkill = initialSkill;
        this.initialStamina = initialStamina;
        this.initialLuck = initialLuck;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public int getInitialSkill() {
        return initialSkill;
    }

    public void setInitialSkill(int initialSkill) {
        this.initialSkill = initialSkill;
    }

    public int getInitialStamina() {
        return initialStamina;
    }

    public void setInitialStamina(int initialStamina) {
        this.initialStamina = initialStamina;
    }

    public int getInitialLuck() {
        return initialLuck;
    }

    public void setInitialLuck(int initialLuck) {
        this.initialLuck = initialLuck;
    }
}