package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.dto.StatCheckResult;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.ChoiceRepository;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class PlaythroughService {
    private final PlaythroughRepository playthroughRepository;
    private final StoryRepository storyRepository;
    private final PageRepository pageRepository;
    private final ChoiceRepository choiceRepository;
    private final BattleService battleService;
    private final AuthService authService;

    public PlaythroughService(PlaythroughRepository playthroughRepository, StoryRepository storyRepository,
                              PageRepository pageRepository, ChoiceRepository choiceRepository,
                              BattleService battleService, AuthService authService) {
        this.playthroughRepository = playthroughRepository;
        this.storyRepository = storyRepository;
        this.pageRepository = pageRepository;
        this.choiceRepository = choiceRepository;
        this.battleService = battleService;
        this.authService = authService;
    }

    @Transactional
    public PlaythroughDTO startPlaythrough(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, story);

        boolean isFirstPlaythrough = playthroughRepository.countByUserAndStory(user, story) == 0;

        Integer startPageNumber = story.getStartPageNumber();
        Page startPage = story.getPages().stream()
                .filter(p -> p.getPageNumber() == startPageNumber)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Start page not found"));

        Playthrough playthrough = new Playthrough();
        playthrough.setUser(user);
        playthrough.setStory(story);
        playthrough.setCurrentPage(startPage);
        playthrough.setPath(new ArrayList<>(List.of(startPageNumber)));
        playthrough.setActive(true);
        playthrough.setCompleted(false);
        playthrough.setStartedAt(LocalDateTime.now());
        playthrough.setLastVisited(LocalDateTime.now());
        //TODO: Add custom stats/presets
        playthrough.setStats(generateRandomStats());

        boolean luckRequired = startPage.getChoices().stream()
                .anyMatch(Choice::getRequiresLuckCheck);
        playthrough.setLuckRequired(luckRequired);
        playthrough.setLuckPassed(false);

        if (startPage.getEnemy() != null) {
            playthrough.setBattlePending(true);
        }

        StatModifiers modifiers = startPage.getStatModifiers();
        if (modifiers != null) {
            PlayerStats stats = playthrough.getStats();

            if (modifiers.getSkill() != null) {
                stats.setSkill(stats.getSkill() + modifiers.getSkill());
            }
            if (modifiers.getStamina() != null) {
                stats.setStamina(stats.getStamina() + modifiers.getStamina());
            }
            if (modifiers.getLuck() != null) {
                stats.setLuck(stats.getLuck() + modifiers.getLuck());
            }
        }

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        if (isFirstPlaythrough) {
            story.incrementReads();
            storyRepository.save(story);
        }

        return new PlaythroughDTO(savedPlaythrough);
    }

    @Transactional
    public PageDTO choosePage(Long playthroughId, int pageNumber) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);

        Page nextPage = pageRepository.findByStoryIdAndPageNumber(playthrough.getStory().getId(), pageNumber)
                .orElseThrow(() -> new NotFoundException("Page not found"));

        playthrough.setCurrentPage(nextPage);
        playthrough.getPath().add(pageNumber);
        playthrough.setLastVisited(LocalDateTime.now());

        if (nextPage.isEndPage()) {
            playthrough.setCompleted(true);
        }
        playthroughRepository.save(playthrough);

        return new PageDTO(nextPage);
    }

    @Transactional
    public PlaythroughDTO resolveChoice(Long playthroughId, Long choiceId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        Choice choice = choiceRepository.findByIdAndPage(choiceId, playthrough.getCurrentPage())
                .orElseThrow(() -> new NotFoundException("Choice not present on page"));

        //TODO: Add check if choice is from current page

        if (playthrough.isBattlePending()) {
            throw new BadRequestException("There is a battle that isn't completed yet");
        }

        if (playthrough.isLuckRequired()) {
            throw new BadRequestException("Luck check is not completed, perform the luck check");
        }

        if (choice.getRequiresLuckCheck() && !playthrough.isLuckPassed()) {
            throw new BadRequestException("Required luck check failed, select a different choice");
        }

        Page nextPage = pageRepository.findByStoryIdAndPageNumber(playthrough.getStory().getId(), choice.getTargetPage())
                .orElseThrow(() -> new NotFoundException("Page not found in story"));

        playthrough.setCurrentPage(nextPage);
        playthrough.getPath().add(choice.getTargetPage());
        playthrough.setLastVisited(LocalDateTime.now());

        if (nextPage.isEndPage()) {
            playthrough.setCompleted(true);
        }

        boolean luckRequired = nextPage.getChoices().stream()
                .anyMatch(Choice::getRequiresLuckCheck);

        playthrough.setLuckRequired(luckRequired);
        playthrough.setLuckPassed(false);

        if (nextPage.getEnemy() != null) {
            playthrough.setBattlePending(true);
        }

        StatModifiers modifiers = nextPage.getStatModifiers();
        if (modifiers != null) {
            PlayerStats stats = playthrough.getStats();

            if (modifiers.getSkill() != null) {
                stats.setSkill(stats.getSkill() + modifiers.getSkill());
            }
            if (modifiers.getStamina() != null) {
                stats.setStamina(stats.getStamina() + modifiers.getStamina());
            }
            if (modifiers.getLuck() != null) {
                stats.setLuck(stats.getLuck() + modifiers.getLuck());
            }
        }

        playthroughRepository.save(playthrough);

        return new PlaythroughDTO(playthrough, nextPage);
    }

    public StatCheckResult testPlayerLuck(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        if (!playthrough.isLuckRequired()) {
            throw new BadRequestException("No luck check present, refresh page to get updated playthrough data");
        }

        int playerLuck = playthrough.getStats().getLuck();
        int diceRoll = rollDice(2);
        boolean checkPassed = diceRoll <= playerLuck;

        playthrough.getStats().setLuck(playerLuck - 1);
        playthrough.setLuckPassed(checkPassed);
        playthrough.setLuckRequired(false);

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        return new StatCheckResult(diceRoll, checkPassed, savedPlaythrough);
    }

    @Transactional
    public Battle startBattle(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        if (!playthrough.isBattlePending() || playthrough.getCurrentPage().getEnemy() == null) {
            throw new BadRequestException("No battle is required on this page.");
        }

        if (playthrough.getBattle() != null && !playthrough.getBattle().isCompleted()) {
            return playthrough.getBattle();
        }

        Battle battle = battleService.startBattle(
                playthrough.getCurrentPage(),
                playthrough.getStats(),
                playthrough
        );
        playthrough.setBattle(battle);
        playthroughRepository.save(playthrough);
        return battle;
    }

    @Transactional
    public Battle resolveBattleRound(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        Battle battle = playthrough.getBattle();
        if (battle == null || battle.isCompleted()) {
            throw new BadRequestException("No active battle.");
        }

        return battleService.resolveRound(battle);
    }

    @Transactional
    public Battle useLuckInBattle(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        Battle battle = playthrough.getBattle();
        if (battle == null || battle.isCompleted()) {
            throw new BadRequestException("No active battle.");
        }

        Battle updatedBattle = battleService.applyLuck(battle);
        if (battleService.isBattleOver(updatedBattle)) {
            battleService.finalizeBattle(updatedBattle);
        }

        return updatedBattle;
    }

    @Transactional
    public PlaythroughDTO concludeBattle(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        Battle battle = playthrough.getBattle();

        if (battle == null || !battle.isCompleted()) {
            throw new BadRequestException("No completed battle to finalize.");
        }

        playthrough.setBattlePending(false);

        if (!battle.isPlayerWon()) {
            playthrough.setCompleted(true);
        }

        PlayerStats playthroughStats = playthrough.getStats();
        playthroughStats.setStamina(battle.getPlayerStamina());
        playthroughStats.setSkill(battle.getPlayerSkill());
        playthroughStats.setLuck(battle.getPlayerLuck());

        playthrough.setBattle(null);

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);
        return new PlaythroughDTO(savedPlaythrough, savedPlaythrough.getCurrentPage());
    }

    @Transactional
    public Battle continueBattleWithoutLuck(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        Battle battle = playthrough.getBattle();
        if (battle == null || battle.isCompleted()) {
            throw new BadRequestException("No active battle.");
        }

        Battle updatedBattle = battleService.finalizeWithoutLuck(battle);

        if (battleService.isBattleOver(updatedBattle)) {
            battleService.finalizeBattle(updatedBattle);
        }

        return updatedBattle;
    }

    public Battle getCurrentBattle(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return playthrough.getBattle();
    }

    public PlaythroughDTO getPlaythroughById(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return new PlaythroughDTO(playthrough, playthrough.getCurrentPage());
    }

    public List<PlaythroughDTO> getPlaythroughsForUserAndStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        List<Playthrough> playthroughs = playthroughRepository.findByUserAndStoryOrderByLastVisitedDesc(user, story);
        return playthroughs.stream().map(PlaythroughDTO::new).toList();
    }

    @Transactional
    public void loadPlaythrough(Long playthroughId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        if (playthrough.isActive()) return;

        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, playthrough.getStory());
        playthrough.setActive(true);
        playthrough.setLastVisited(LocalDateTime.now());
        playthroughRepository.save(playthrough);
    }

    public PageDTO getCurrentPageForPlaythrough(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return new PageDTO(playthrough.getCurrentPage());
    }

    public org.springframework.data.domain.Page<PlaythroughDTO> getPaginatedPlaythroughsForUser(String query, Pageable pageable) {
        User user = authService.getAuthenticatedUserOrThrow();
        org.springframework.data.domain.Page<Playthrough> page = (query != null && !query.isBlank())
                ? playthroughRepository.searchByUserAndStoryTitle(user, query, pageable)
                : playthroughRepository.findByUser(user, pageable);
        return page.map(PlaythroughDTO::new);
    }

    @Transactional
    public void deletePlaythrough(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        playthroughRepository.delete(playthrough);
    }

    private Playthrough getPlaythroughOwnedByUser(Long playthroughId) {
        Playthrough playthrough = playthroughRepository.findById(playthroughId)
                .orElseThrow(() -> new NotFoundException("Playthrough not found"));
        User currentUser = authService.getAuthenticatedUserOrThrow();
        if (!playthrough.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not own this playthrough.");
        }
        return playthrough;
    }

    private int rollDice(int number) {
        Random random = new Random();
        return IntStream.range(0, number)
                .map(i -> random.nextInt(6) + 1)
                .sum();
    }

    private PlayerStats generateRandomStats() {
        Random rand = new Random();
        int skill = 6 + rollDice(1);    // 1d6 + 6
        int stamina = 2 * (6 + rollDice(1)); // 2d6 + 12
        int luck = 6 + rollDice(1);     // 1d6 + 6

        return new PlayerStats(skill, stamina, luck);
    }
}