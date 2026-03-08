package dev.ng5m.minesweeper.game;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public sealed interface Statistics
permits Statistics.V1
{

    void addMatch(Match match);

    record V1(
            List<Match> matches
    ) implements Statistics {
        @Override
        public void addMatch(Match match) {
            matches.add(match);
        }
    }

    static Statistics deserialize(ByteBuffer buffer) {
        int version = buffer.get();
        if (version != 1)
            throw new UnsupportedOperationException("Unsupported statistics version " + version);

        return new V1(Arrays.asList(Match.deserializeAll(buffer)));
    }

    static void serialize(ByteBuffer buffer, Statistics statistics) {
        buffer.put((byte) 1);

        Match.serializeAll(buffer, ((V1) statistics).matches().toArray(Match[]::new));
    }

}
