package com.example.bot.spring;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Menu {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String menuId;

    protected Menu() {
    }

    public Employee(String name, String menuId) {
        this.name = name;
        this.menuId = menuId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    @Override
    public String toString() {
        return String.format("Menu[id=%d, name='%s', menuId='%s']", id, name, menuId);
    }

}
