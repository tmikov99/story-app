package com.coursework.story.dto;

import com.coursework.story.model.Item;
import com.coursework.story.model.StatModifiers;

public class ItemDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private StatModifiers statModifiers;

    public ItemDTO() {}

    public ItemDTO(Item item) {
        id = item.getId();
        name = item.getName();
        description = item.getDescription();
        icon = item.getIcon();
        statModifiers = item.getStatModifiers();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public StatModifiers getStatModifiers() {
        return statModifiers;
    }

    public void setStatModifiers(StatModifiers statModifiers) {
        this.statModifiers = statModifiers;
    }
}
