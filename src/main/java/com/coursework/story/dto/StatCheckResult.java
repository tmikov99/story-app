package com.coursework.story.dto;

import com.coursework.story.model.Playthrough;

public class StatCheckResult {
    private Integer diceRoll;
    private boolean passed;
    private PlaythroughDTO playthrough;

    public StatCheckResult() {}

    public StatCheckResult(Integer diceRoll, boolean passed, Playthrough playthrough) {
        this.diceRoll = diceRoll;
        this.passed = passed;
        this.playthrough = new PlaythroughDTO(playthrough, playthrough.getCurrentPage());
    }

    public Integer getDiceRoll() {
        return diceRoll;
    }

    public void setDiceRoll(Integer diceRoll) {
        this.diceRoll = diceRoll;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public PlaythroughDTO getPlaythrough() {
        return playthrough;
    }

    public void setPlaythrough(PlaythroughDTO playthrough) {
        this.playthrough = playthrough;
    }
}
