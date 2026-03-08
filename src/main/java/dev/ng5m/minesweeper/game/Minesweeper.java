package dev.ng5m.minesweeper.game;

import dev.ng5m.minesweeper.client.MinesweeperClient;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Minesweeper {
    public static final byte TILE_EMPTY = 0;
    public static final byte NUMBER_TILE_BASE = 1;
    public static final byte TILE_HIDDEN = 9;
    public static final byte TILE_FLAG = 10;
    public static final byte MINE_TILE = 12;
    public static final byte RED_MINE_TILE = 13;
    public static final byte X_MINE_TILE = 14;

    private final MinesweeperPlatform platform;

    private long seed;
    public Random random;

    public int width;
    public int height;

    public int mines;
    public int flags;

    public long startTime;
    public long endTime = 0;
    private long pauseTime;
    private long timeWhilePaused;

    private byte[][] grid;
    public byte[][] displayGrid;

    public boolean ended = false;
    private boolean started = false;
    public boolean lost = false;

    private int hiddenCount;

    public Minesweeper(MinesweeperPlatform platform, long seed, int width, int height, int mines) {
        this.platform = platform;
        rebuild(seed, width, height, mines);
    }

    public Minesweeper(MinesweeperPlatform platform, int width, int height, int mines) {
        this(platform, ThreadLocalRandom.current().nextLong(), width, height, mines);
    }

    public void populateGrid() {
        for (int i = 0; i < mines; ) {
            int rx = random.nextInt(width), ry = random.nextInt(height);
            if (grid[rx][ry] == MINE_TILE) continue;

            grid[rx][ry] = MINE_TILE;
            i++;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == TILE_EMPTY)
                    grid[x][y] = (byte) (numberTile(countBombs(x, y)) - 1);
            }
        }
    }

    public void clickDave(boolean effects) {
        rebuild(width, height, mines);
    }

    public void clickCell(int x, int y, boolean rightClick, boolean effects) {
        if (ended) return;
        if (!started) {
            started = true;
            startTime = System.currentTimeMillis() / 1000;
        }

        if (rightClick) {
            if (displayGrid[x][y] == TILE_HIDDEN) {
                displayGrid[x][y] = TILE_FLAG;

                flags--;
            } else if (displayGrid[x][y] == TILE_FLAG) {
                flags++;

                displayGrid[x][y] = TILE_HIDDEN;
            }

            if (effects) platform.playSound(MinesweeperPlatform.Sound.FLAG);

            checkWinCondition();

            return;
        }

        if (grid[x][y] == MINE_TILE && displayGrid[x][y] != TILE_FLAG) {
            displayGrid[x][y] = RED_MINE_TILE;
            if (effects) platform.playSound(MinesweeperPlatform.Sound.BOMB);
            endGame(true);
            return;
        }

        if (effects) platform.playSound(MinesweeperPlatform.Sound.CLICK);

        reveal(x, y);

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (hiddenCount == mines - flags) {
            endGame(false);
        }
    }

    private void reveal(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0
                || displayGrid[x][y] != TILE_HIDDEN) return;

        byte current = grid[x][y];
        displayGrid[x][y] = current;
        hiddenCount--;

        if (current == TILE_EMPTY) {
            reveal(x + 1, y);
            reveal(x - 1, y);
            reveal(x, y + 1);
            reveal(x, y - 1);
        }
    }

    private void endGame(boolean loss) {
        ended = true;
        lost = loss;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                byte tile = grid[x][y];
                byte displayedTile = displayGrid[x][y];

                if (tile == MINE_TILE) {
                    if (displayedTile == TILE_HIDDEN) {
                        displayGrid[x][y] = MINE_TILE;
                    }
                } else {
                    if (displayedTile == TILE_FLAG) {
                        displayGrid[x][y] = X_MINE_TILE;
                    } else {
                        displayGrid[x][y] = grid[x][y];
                    }
                }
            }
        }

        endTime = System.currentTimeMillis() / 1000;

        MinesweeperClient.statistics.addMatch(new Match(
                loss, width, height, mines, flags, hiddenCount,
                seed, startTime, endTime
        ));
    }

    public void pause() {
        if (startTime > 0 && endTime == 0 && pauseTime == 0) {
            pauseTime = System.currentTimeMillis() / 1000;
        }
    }

    public void unpause() {
        if (pauseTime > 0) {
            long toAdd = (System.currentTimeMillis() / 1000 - pauseTime);
            timeWhilePaused += toAdd;
            startTime += toAdd;

            pauseTime = 0;
        }
    }

    public long getTimeString() {
        long timeString;

        if (endTime == 0) {
            if (startTime == 0) {
                timeString = 0;
            } else {
                if (pauseTime > 0) {
                    timeString = pauseTime - startTime;
                } else {
                    timeString = System.currentTimeMillis() / 1000 - startTime;
                }
            }
        } else {
            timeString = endTime - startTime;
        }

        return timeString;
    }

    private int countBombs(int x, int y) {
        int i = 0;
        if (isBomb(x + 1, y)) i++;
        if (isBomb(x - 1, y)) i++;
        if (isBomb(x, y + 1)) i++;
        if (isBomb(x, y - 1)) i++;
        if (isBomb(x + 1, y + 1)) i++;
        if (isBomb(x + 1, y - 1)) i++;
        if (isBomb(x - 1, y + 1)) i++;
        if (isBomb(x - 1, y - 1)) i++;

        return i;
    }

    private boolean isBomb(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0) return false;

        return grid[x][y] == MINE_TILE;
    }

    private static byte numberTile(int n) {
        return (byte) (NUMBER_TILE_BASE + n & 0xff);
    }

    // TODO pausing when exiting spectator

    public void rebuild(int width, int height, int mines) {
        rebuild(ThreadLocalRandom.current().nextLong(), width, height, mines);
    }

    public void rebuild(long seed, int width, int height, int mines) {
        this.seed = seed;
        this.random = new Random(seed);

        this.width = width;
        this.height = height;
        this.hiddenCount = width * height;

        this.mines = Math.min(mines, width * height);
        this.flags = this.mines;

        this.ended = false;
        this.started = false;
        this.lost = false;

        this.startTime = 0;
        this.endTime = 0;

        this.grid = new byte[width][height];
        this.displayGrid = new byte[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = TILE_EMPTY;
                displayGrid[x][y] = TILE_HIDDEN;
            }
        }

        populateGrid();
    }

    public static void main(String[] args) {
        Minesweeper minesweeper = new Minesweeper(MinesweeperPlatform.DUMMY, 1, 10, 10, 10);
        minesweeper.populateGrid();

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < minesweeper.height; y++) {
            sb.append('|');
            for (int x = 0; x < minesweeper.width; x++) {
                byte tile = minesweeper.grid[x][y];

                if (tile == TILE_EMPTY) {
                    sb.append(' ');
                } else if (tile == MINE_TILE) {
                    sb.append('#');
                } else {
                    sb.append(String.valueOf(tile - NUMBER_TILE_BASE));
                }
            }

            sb.append("|\n");
        }

        String sep = ' ' + "-".repeat(minesweeper.width);
        System.out.println(sep);
        System.out.println(sb);
        System.out.println(sep);
    }

}
