package checkers;

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
    } //шашки нет
    public MoveResult(MoveType type, Checker checker) { //шашка есть и её тип движения известен
        this.type = type;
        this.checker = checker;
    }
}
enum MoveType { //Состояние на месте шашки
    NONE, NORMAL, KILL, QUEEN
}