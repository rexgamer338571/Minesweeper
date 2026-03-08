package dev.ng5m.minesweeper.game;

public interface MinesweeperPlatform {
    MinesweeperPlatform DUMMY = sound -> {};

    void playSound(Sound sound);

    enum Sound {
        CLICK,
        BOMB,
        FLAG;
    }
}
