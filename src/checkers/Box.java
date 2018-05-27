package checkers;

import javafx.scene.shape.Rectangle;

public class Box extends Rectangle {

    private Checker checker;

    public boolean hasChecker() {
        return checker != null;
    }

    public Checker getChecker() {
        return checker;
    }

    public void setChecker(Checker checker) {
        this.checker = checker;
    }

    public Box(boolean isblack, int x, int y) {
        setWidth(PlayCheckers.BOX_SIZE);
        setHeight(PlayCheckers.BOX_SIZE);

        relocate(x * PlayCheckers.BOX_SIZE, y * PlayCheckers.BOX_SIZE);
    }
}