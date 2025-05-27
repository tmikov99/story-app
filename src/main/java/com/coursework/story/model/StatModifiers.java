package com.coursework.story.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class StatModifiers {
    private Integer skill;
    private Integer stamina;
    private Integer luck;

    public Integer getSkill() {
        return skill;
    }

    public void setSkill(Integer skill) {
        this.skill = skill;
    }

    public Integer getStamina() {
        return stamina;
    }

    public void setStamina(Integer stamina) {
        this.stamina = stamina;
    }

    public Integer getLuck() {
        return luck;
    }

    public void setLuck(Integer luck) {
        this.luck = luck;
    }
}
