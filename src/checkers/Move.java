package checkers;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MoveResult {

    private MoveType type;

    public MoveType getType() {
        return type;
    }

    private Checker checker;

    public Checker getChecker() {
        return checker;
    }

    public MoveResult(MoveType type) {
        this(type, null);
    }

    public MoveResult(MoveType type, Checker checker) {
        this.type = type;
        this.checker = checker;
    }
}
enum MoveType { //Состояние на месте шашки
    NONE, NORMAL, KILL, QUEEN
}