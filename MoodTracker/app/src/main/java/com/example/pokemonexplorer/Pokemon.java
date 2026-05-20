package com.example.pokemonexplorer;

public class Pokemon {

    private final String name;
    private final String imageUrl;

    private final int hp;
    private final int attack;
    private final int defense;
    private final int specialAttack;
    private final int specialDefense;
    private final int speed;

    public Pokemon(String name, String imageUrl, int hp, int attack, int defense,
                   int specialAttack, int specialDefense, int speed) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
    }

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialAttack() { return specialAttack; }
    public int getSpecialDefense() { return specialDefense; }
    public int getSpeed() { return speed; }
}