package dev.ng5m.minesweeper.game;

import java.nio.ByteBuffer;

public record Match(boolean lost,
                    int width, int height,
                    int mines, int flagsLeft,
                    int hiddenCount,
                    long seed,
                    long startTime, long endTime
) {
    public static final int SIZE = 1 + Short.BYTES * 5 + Long.BYTES * 3;

    static void serialize(ByteBuffer buffer, Match match) {
        buffer.put((byte) (match.lost ? 1 : 0));
        buffer.putShort((short) match.width);
        buffer.putShort((short) match.height);
        buffer.putShort((short) match.mines);
        buffer.putShort((short) match.flagsLeft);
        buffer.putShort((short) match.hiddenCount);
        buffer.putLong(match.seed);
        buffer.putLong(match.startTime);
        buffer.putLong(match.endTime);
    }

    static Match deserialize(ByteBuffer buffer) {
        return new Match(
                buffer.get() == 1,
                buffer.getShort(),
                buffer.getShort(),
                buffer.getShort(),
                buffer.getShort(),
                buffer.getShort(),
                buffer.getLong(),
                buffer.getLong(),
                buffer.getLong()
        );
    }

    static void serializeAll(ByteBuffer buffer, Match... matches) {
        buffer.putInt(matches.length);
        for (Match match : matches)
            serialize(buffer, match);
    }

    static Match[] deserializeAll(ByteBuffer buffer) {
        Match[] matches = new Match[buffer.getInt()];
        for (int i = 0; i < matches.length; i++)
            matches[i] = deserialize(buffer);

        return matches;
    }
}
