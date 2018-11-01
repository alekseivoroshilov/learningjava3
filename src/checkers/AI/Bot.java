package checkers.AI;

import checkers.Checker;

import java.util.HashMap;

public interface Bot {
    public void move(Checker checker, int x, int y);
    public HashMap<String,String> getTheNextMoveList();

}
