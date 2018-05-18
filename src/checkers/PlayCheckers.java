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

import javax.xml.bind.annotation.XmlType;

public class PlayCheckers extends Application {
    public static final int BOX_SIZE = 98;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    private Box[][] board = new Box[WIDTH][HEIGHT];

    //заставляет игроков ходить по очереди
    public static CheckerType turn = CheckerType.WHITE;

    //умерла ли какая-нибудь фишка в предыдущий ход? (для серии убийств)
    public boolean previousTurnCheckerKilled = false;

    private Group tileGroup = new Group();
    private Group checkersGroup = new Group();
    private Image boardImage = new Image("board.png");
//    private Stage primaryStage = new Stage();

    private Parent createContent() {

        ImageView borderImageViev = new ImageView(boardImage);
        StackPane sp = new StackPane();

        Pane boardPane = new Pane();
        //note:
        //0.067 - коэффициент, равный отношению одного отступа (60 пикселей сбоку для цифр, например) от длины стороны доски
        Pane root = new Pane();
        root.setPrefSize(WIDTH * BOX_SIZE, HEIGHT * BOX_SIZE);
        //root.getChildren().addAll(tileGroup);
        //root.getChildren().addAll(borderImageViev);
        boardPane.getChildren().addAll(borderImageViev);
        //Scene boardScene = new Scene(boardTexture);
        root.setTranslateY(60);
        root.setTranslateX(60);
        root.getChildren().addAll(checkersGroup);
        sp.getChildren().add(boardPane);
        sp.getChildren().add(root);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Box tile = new Box((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Checker checker = null;

                if (y <= 2 && (x + y) % 2 != 0) {
                    checker = makeChecker(CheckerType.BLACK, x, y, false);
                }

                if (y >= 5 && (x + y) % 2 != 0) {
                    checker = makeChecker(CheckerType.WHITE, x, y, false);
                }

                if (checker != null) {
                    tile.setChecker(checker);
                    checkersGroup.getChildren().add(checker);
                }
            }
        }

        //return root;
        return sp;
    }

    private MoveResult tryMove(Checker checker, int newX, int newY) {
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
    private int toBoard(double pixel) {
        return (int) (pixel + BOX_SIZE / 2) / BOX_SIZE;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        ImageView boardTextureView = new ImageView(boardImage);
        BackgroundImage bi = new BackgroundImage(boardImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);

        primaryStage.setTitle("EnglishDraughts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Checker makeChecker(CheckerType type, int x, int y, boolean isQueen) {
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

            int x0 = toBoard(checker.getOldX());
            int y0 = toBoard(checker.getOldY());

            switch (result.getType()) {
                case NONE:
                    checker.wrongMove();
                    ;
                    break;
                case NORMAL:
                    //если пользователь сделает обычное движение после убийства, движение не засчитывается
                    if (!previousTurnCheckerKilled) {
                        checker.move(newX, newY);
                        board[x0][y0].setChecker(null);
                        if (newY == 0 || abs(newY) == 7) { //если этот ход пришёлся на верхний-нижний край доски
                            System.out.println("QUEEN DETECTED");
                            Checker upgradedChecker = makeChecker(checker.getType(), newX, newY, true);
                            board[newX][newY].setChecker(upgradedChecker);
                            checkersGroup.getChildren().add(upgradedChecker);
                            checkersGroup.getChildren().remove(checker);
                        } else //если обычный ход
                            board[newX][newY].setChecker(checker);
                        //всегда меняю очередь хода
                        turn = turn == CheckerType.WHITE ? CheckerType.BLACK : CheckerType.WHITE;
                    } else {
                        // после убийства, если есть ещё вражеские шашки, просто перемещаться нельзя
                        checker.wrongMove();
                    }
                    break;
                case QUEEN:
                    checker.move(newX, newY);
                    board[x0][y0].setChecker(null);
                    board[newX][newY].setChecker(checker);

                    turn = turn == CheckerType.WHITE ? CheckerType.BLACK : CheckerType.WHITE;
                    break;
                case KILL:
                    checker.move(newX, newY);
                    board[x0][y0].setChecker(null);

                    if ((newY == 0 || abs(newY) == 7) && !checker.isQueen()) {
                        System.out.println("QUEEN DETECTED");
                        Checker upgradedChecker = makeChecker(checker.getType(), newX, newY, true);
                        board[newX][newY].setChecker(upgradedChecker);
                        checkersGroup.getChildren().add(upgradedChecker);
                        checkersGroup.getChildren().remove(checker);
                    } else
                        board[newX][newY].setChecker(checker);


                    Checker otherChecker = result.getChecker();
                    board[toBoard(otherChecker.getOldX())][toBoard(otherChecker.getOldY())].setChecker(null);
                    checkersGroup.getChildren().remove(otherChecker);
                    //переменные с клетками, рядом с местом нашей шашки после убийства (для серии убийств, чтобы понять, куда дальше есть)
                    Box tileUpRight = board[newX + 1][newY - 1]; //вражеская шашка
                    Box tileUpLeft = board[newX - 1][newY - 1];
                    Box tileDownRight = board[newX + 1][newY + 1];
                    Box tileDownLeft = board[newX - 1][newY + 1];

                    boolean canEatUp = false;
                    boolean canEatDown = false;
                    System.out.println(newX + " " + newY);
                    if (checker.getType() == CheckerType.BLACK || checker.isQueen()) { //если убийство делает чёрная шашка
                        try {
                            canEatDown = tileDownLeft.hasChecker() && tileDownLeft.getChecker().getType() != checker.getType() && !board[newX - 2][newY + 2].hasChecker() ||
                                    tileDownRight.hasChecker() && tileDownRight.getChecker().getType() != checker.getType() && !board[newX + 2][newY + 2].hasChecker();
                            System.out.println(canEatDown);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            previousTurnCheckerKilled = false;
                            canEatDown = false;
                        }
                        if (canEatDown)
                            previousTurnCheckerKilled = true; //условие, чтобы можно было совершить серию убийств
                    }
                    //если убийство делает белая шашка
                    if (checker.getType() == CheckerType.WHITE || checker.isQueen()) {
                        try {
                            canEatUp = tileUpLeft.hasChecker() && tileUpLeft.getChecker().getType() != checker.getType() && !board[newX - 2][newY - 2].hasChecker() ||
                                    tileUpRight.hasChecker() && tileUpRight.getChecker().getType() != checker.getType() && !board[newX + 2][newY - 2].hasChecker();
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            previousTurnCheckerKilled = false;
                            canEatUp = false;
                        }
                        if (canEatUp)
                            previousTurnCheckerKilled = true;
                    }

                    System.out.println(previousTurnCheckerKilled);
                    // нет шашек противника в досигаемости? ход передаётся противнику
                    if (!previousTurnCheckerKilled)
                        turn = (turn == CheckerType.WHITE) ? CheckerType.BLACK : CheckerType.WHITE;
                    break;

            }
        });

        return checker;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
