package JavaToVapor;

import Utils.Pair;

public class VaporGlobalState {
    static int nullx_size = 0;
    static String nullx() {
        return String.format("null%d", ++nullx_size);
    }

    static int boundsSize = 0;
    static String boundsx() {
        return String.format("bounds%d", ++boundsSize);
    }

    static int ifSize = 0;
    static Pair<String, String> ifx() {
        ifSize++;
        return new Pair<>(String.format("if%d_else", ifSize), String.format("if%d_end", ifSize));
    }

    static int whileSize = 0;
    static Pair<String, String> whilex() {
        whileSize++;
        return new Pair<>(String.format("while%d_top", whileSize), String.format("while%d_end", whileSize));
    }

    static int ssSize = 0;
    static Pair<String, String> ssx() {
        ssSize++;
        return new Pair<>(String.format("ss%d_else", ssSize), String.format("ss%d_end", ssSize));
    }
}
