package checkers;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static java.lang.Math.abs;

import java.util.ArrayList;

public class PlayCheckers extends Application {
    static final int OBJECTS_SIZE = 5; // множитель размеров всех объектов из 10 возможных
    static final int BOX_SIZE = 113 * OBJECTS_SIZE / 10;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    public Box[][] board = new Box[WIDTH][HEIGHT];
    public ArrayList<Checker> checkersThatMustEat = new ArrayList<>(); //хранит ссылки на шашки, которые должны есть
    //заставляет игроков ходить по очереди
    public static CheckerType turn = CheckerType.WHITE;

    //умерла ли какая-ниб удь фишка в предыдущий ход? (для серии убийств)
    public boolean previousTurnCheckerKilled = false;

    Group tileGroup = new Group();
    Group checkersGroup = new Group();
    Image boardImage = new Image("board.png", BOX_SIZE * 8, BOX_SIZE * 8, false, false);
    Parent createContent() {
        ImageView borderImageViev = new ImageView(boardImage);
        StackPane sp = new StackPane();
        Pane boardPane = new Pane();
        Pane root = new Pane();
        root.setPrefSize(WIDTH * BOX_SIZE, HEIGHT * BOX_SIZE);
        boardPane.getChildren().addAll(borderImageViev);
        //root.setTranslateY(60 * OBJECTS_SIZE / 10);
        //root.setTranslateX(60 * OBJECTS_SIZE / 10);
        root.getChildren().addAll(checkersGroup);
        sp.getChildren().add(boardPane);
        sp.getChildren().add(root);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Box tile = new Box((x + y) % 2 == 0, x, y);
                board[x][y] = tile;
                tileGroup.getChildren().add(tile);
                Checker checker = null;
                if (y <= 2 && (x + y) % 2 != 0) checker = makeChecker(CheckerType.BLACK, x, y, false);
                if (y >= 5 && (x + y) % 2 != 0) checker = makeChecker(CheckerType.WHITE, x, y, false);
                if (checker != null) {
                    tile.setChecker(checker);
                    checkersGroup.getChildren().add(checker);
                }
            }
        }
        return sp;
    }

    public MoveResult tryMove(Checker checker, int newX, int newY) {
        if (board[newX][newY].hasChecker() || (newX + newY) % 2 == 0 || turn != checker.getType()) {
            return new MoveResult(MoveType.NONE);
        }

        int x0 = toBoard(checker.getOldX());
        int y0 = toBoard(checker.getOldY());

        if (abs(newX - x0) == 1 && abs(newY - y0) == 1 && checker.isQueen()) {
            return new MoveResult(MoveType.QUEEN);
        } else if (abs(newX - x0) == 1 && newY - y0 == checker.getType().moveDir) {
            return new MoveResult(MoveType.NORMAL);
        } else if (abs(newX - x0) == 2 && ((newY - y0 == checker.getType().moveDir * 2) ||
                checker.isQueen() && abs(newX - x0) == 2 && abs(newY - y0) == 2)) {

            int x1 = x0 + (newX - x0) / 2;
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasChecker() && board[x1][y1].getChecker().getType() != checker.getType()) {
                return new MoveResult(MoveType.KILL, board[x1][y1].getChecker());
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    //метод, позволяющий понять, какой координате доски 8x8 соответствует координата выбранной точки
    public int toBoard(double pixel) {
        return (int) (pixel + BOX_SIZE / 2) / BOX_SIZE;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("EnglishDraughts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Checker makeChecker(CheckerType type, int x, int y, boolean isQueen) {
        Checker checker = new Checker(type, x, y, isQueen);

        checker.setOnMouseReleased(e -> {
            int newX = toBoard(checker.getLayoutX());
            int newY = toBoard(checker.getLayoutY());

            MoveResult result;

            if (newX < 0 || newY < 0 || newX >= WIDTH || newY >= HEIGHT) {
                result = new MoveResult(MoveType.NONE);
            } else {
                result = tryMove(checker, newX, newY);
            }

            int x0 = toBoard(checker.getOldX()); //перевод в удобные для восприятия координаты от 1 до 8
            int y0 = toBoard(checker.getOldY());
            //
            boolean playerMustEat = !checkersThatMustEat.isEmpty(); //есть ли шашки, что обязаны есть?
            //
            System.out.println("Should i eat a checker?: " + playerMustEat);
            switch (result.getType()) {
                case NONE:
                    checker.wrongMove();
                    break;
                case NORMAL:
                    //если пользователь сделает обычное движение после убийства, движение не засчитывается
                    if (!previousTurnCheckerKilled && !playerMustEat){
                        checker.move(newX, newY);
                        board[x0][y0].setChecker(null);
                        if (newY == 0 || abs(newY) == 7) { //если этот ход пришёлся на верхний-нижний край доски
                            Checker upgradedChecker = makeChecker(checker.getType(), newX, newY, true);
                            board[newX][newY].setChecker(upgradedChecker);
                            checkersGroup.getChildren().add(upgradedChecker);
                            checkersGroup.getChildren().remove(checker);
                        } else //если обычный ход
                            board[newX][newY].setChecker(checker);
                        //всегда меняю очередь хода
                        turn = turn == CheckerType.WHITE ? CheckerType.BLACK : CheckerType.WHITE;
                        shouldEatFun();

                    } else checker.wrongMove(); // после убийства, если есть ещё вражеские шашки, просто перемещаться нельзя
                    break;
                case QUEEN:
                    if(!previousTurnCheckerKilled && !playerMustEat) {
                        checker.move(newX, newY);
                        board[x0][y0].setChecker(null);
                        board[newX][newY].setChecker(checker);

                        turn = turn == CheckerType.WHITE ? CheckerType.BLACK : CheckerType.WHITE;
                        shouldEatFun();
                    }else checker.wrongMove();
                    break;
                case KILL:
                    checker.move(newX, newY);
                    board[x0][y0].setChecker(null);

                    if ((newY == 0 || abs(newY) == 7) && !checker.isQueen()) { //после убийства стал королевой
                        Checker upgradedChecker = makeChecker(checker.getType(), newX, newY, true);
                        board[newX][newY].setChecker(upgradedChecker);
                        checkersGroup.getChildren().add(upgradedChecker);
                        checkersGroup.getChildren().remove(checker);
                    } else
                        board[newX][newY].setChecker(checker);


                    Checker otherChecker = result.getChecker();
                    board[toBoard(otherChecker.getOldX())][toBoard(otherChecker.getOldY())].setChecker(null);
                    checkersGroup.getChildren().remove(otherChecker);

                    previousTurnCheckerKilled = enemiesNearbyCanBeKilled(checker, newX, newY);

                    // нет шашек противника в досигаемости? ход передаётся противнику
                    if (!previousTurnCheckerKilled) {
                        turn = (turn == CheckerType.WHITE) ? CheckerType.BLACK : CheckerType.WHITE;
                        checkersThatMustEat.clear();
                        shouldEatFun();
                    }
                    break;

            }
        });

        return checker;
    }

    public boolean enemiesNearbyCanBeKilled(Checker checker, int x, int y) {
        Box tileUpRight = null;
        Box tileUpLeft = null;
        Box tileDownRight = null;
        Box tileDownLeft = null;

        Box tileUpRight2 = null;
        Box tileUpLeft2 = null;
        Box tileDownRight2 = null;
        Box tileDownLeft2 = null;
        boolean specialPosition = false;
        boolean outBorderSafePositionByX = x <= 5 && x >= 2;
        boolean outBorderSafePositionByY = y <= 5 && y >= 2;

        if (x <= 1 && y <= 1) {
            tileDownRight = board[x + 1][y + 1]; //вражеская шашка справа внизу
            tileDownRight2 = board[x + 2][y + 2]; //вражеская шашка справа внизу
            specialPosition = true;
        }
        if (x <= 1 && y >= 6) {
            tileUpRight = board[x + 1][y - 1]; //вражеская шашка справа вверху
            tileUpRight2 = board[x + 2][y - 2]; //вражеская шашка справа вверху
            specialPosition = true;
        }
        if (x >= 6 && y >= 6) {
            tileUpLeft = board[x - 1][y - 1]; //вражеская шашка слева вверху
            tileUpLeft2 = board[x - 2][y - 2]; //вражеская шашка слева вверху
            specialPosition = true;
        }
        if (x >= 6 && y <= 1) {
            tileDownLeft = board[x - 1][y + 1]; //вражеская шашка слева внизу
            tileDownLeft2 = board[x - 2][y + 2]; //вражеская шашка слева внизу
            specialPosition = true;
        }
        if (outBorderSafePositionByX && !outBorderSafePositionByY) {
            if (y <= 1) {
                tileDownRight = board[x + 1][y + 1]; //вражеская шашка справа внизу
                tileDownRight2 = board[x + 2][y + 2]; //вражеская шашка справа внизу
                tileDownLeft = board[x - 1][y + 1]; //вражеская шашка слева внизу
                tileDownLeft2 = board[x - 2][y + 2]; //вражеская шашка слева внизу
            } else {
                tileUpLeft = board[x - 1][y - 1]; //вражеская шашка слева вверху
                tileUpLeft2 = board[x - 2][y - 2]; //вражеская шашка слева вверху
                tileUpRight = board[x + 1][y - 1]; //вражеская шашка справа вверху
                tileUpRight2 = board[x + 2][y - 2]; //вражеская шашка справа вверху
            }
            specialPosition = true;
        }
        if (!outBorderSafePositionByX && outBorderSafePositionByY) {
            if (x <= 1) {
                tileDownRight = board[x + 1][y + 1]; //вражеская шашка справа внизу
                tileDownRight2 = board[x + 2][y + 2]; //вражеская шашка справа внизу
                tileUpRight = board[x + 1][y - 1]; //вражеская шашка справа вверху
                tileUpRight2 = board[x + 2][y - 2]; //вражеская шашка справа вверху


            } else {
                tileDownLeft = board[x - 1][y + 1]; //вражеская шашка слева внизу
                tileDownLeft2 = board[x - 2][y + 2]; //вражеская шашка слева внизу
                tileUpLeft = board[x - 1][y - 1]; //вражеская шашка слева вверху
                tileUpLeft2 = board[x - 2][y - 2]; //вражеская шашка слева вверху
            }
            specialPosition = true;
        }

        if (!specialPosition) {
            tileDownRight = board[x + 1][y + 1]; //вражеская шашка справа внизу
            tileDownRight2 = board[x + 2][y + 2]; //вражеская шашка справа внизу
            tileUpRight = board[x + 1][y - 1]; //вражеская шашка справа вверху
            tileUpRight2 = board[x + 2][y - 2]; //вражеская шашка справа вверху
            tileUpLeft = board[x - 1][y - 1]; //вражеская шашка слева вверху
            tileUpLeft2 = board[x - 2][y - 2]; //вражеская шашка слева вверху
            tileDownLeft = board[x - 1][y + 1]; //вражеская шашка слева внизу
            tileDownLeft2 = board[x - 2][y + 2]; //вражеская шашка слева внизу
        }


            boolean downLeftCheckersCanBeEaten =  tileDownLeft != null && tileDownLeft.hasChecker() &&
                    tileDownLeft.getChecker().getType() != checker.getType() && !tileDownLeft2.hasChecker();

            boolean downRightCheckersCanBeEaten = tileDownRight != null && tileDownRight.hasChecker() &&
                    tileDownRight.getChecker().getType() != checker.getType() && !tileDownRight2.hasChecker();

            boolean upLeftCheckersCanBeEaten = tileUpLeft != null && tileUpLeft.hasChecker() &&
                    tileUpLeft.getChecker().getType() != checker.getType() && !tileUpLeft2.hasChecker();

            boolean upRightCheckersCanBeEaten = tileUpRight != null &&tileUpRight.hasChecker() &&
                    tileUpRight.getChecker().getType() != checker.getType() && !tileUpRight2.hasChecker();

        switch (checker.getType()) {
            case WHITE:
                return upLeftCheckersCanBeEaten || upRightCheckersCanBeEaten;
            case BLACK:
                return downLeftCheckersCanBeEaten || downRightCheckersCanBeEaten;
        }
        return checker.isQueen() && (upLeftCheckersCanBeEaten || upRightCheckersCanBeEaten ||
                downLeftCheckersCanBeEaten || downRightCheckersCanBeEaten);
    }

    public void shouldEatFun() {
        Checker currentChecker;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                currentChecker = board[i][j].hasChecker()? board[i][j].getChecker() : null;
                //&& currentChecker.getType().equals(turn)
                if (currentChecker != null && enemiesNearbyCanBeKilled(currentChecker, i, j)) {
                    currentChecker.setMustEat(true);
                    checkersThatMustEat.add(currentChecker);
                }
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
