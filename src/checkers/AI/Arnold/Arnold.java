package checkers.AI.Arnold;

import checkers.AI.Bot;
import checkers.Checker;
import checkers.PlayCheckers;

import java.util.HashMap;

public class Arnold implements Bot {
    //private CheckerType color;
    public void move(Checker checker, int x, int y) {
        PlayCheckers thisTurn = new PlayCheckers();
        thisTurn.tryMove(checker, x, y);
    }

    public HashMap<String, String> getTheNextMoveList() {
        return null;
    }
}
