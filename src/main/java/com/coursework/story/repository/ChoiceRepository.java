package com.coursework.story.repository;

import com.coursework.story.model.Choice;
import com.coursework.story.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    Optional<Choice> findByIdAndPage(Long choiceId, Page page);
}
