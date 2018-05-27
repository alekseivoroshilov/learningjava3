package checkers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

import static checkers.PlayCheckers.BOX_SIZE;

//класс, описывающий шашку
public class Checker extends StackPane {

    private CheckerType type;

    private Image blackChecker = new Image("black.png");
    private Image whiteChecker = new Image("white.png");
    private Image blackQueenChecker = new Image("blackqueen.png");
    private Image whiteQueenChecker = new Image("whitequeen.png");
    private double mouseX, mouseY;
    private double oldX, oldY;
    private boolean queen = false;
    private boolean mustEatThisRound = false;

    public CheckerType getType() {
        return type;
    }
    public boolean isQueen() { return queen; }
    public double getOldX() {
        return oldX;
    }
    public double getOldY() {
        return oldY;
    }
    public boolean mustEat() {
        return mustEatThisRound;
    }
    public void setMustEat(boolean b) { this.mustEatThisRound = b; }

    public Checker(CheckerType type, int x, int y, boolean queen) {
        this.queen = queen;
        this.type = type;
        move(x, y);
        ImageView iv = new ImageView();
        if (queen){
            System.out.println("Changing the image");
            iv = new ImageView(type == CheckerType.BLACK ? blackQueenChecker: whiteQueenChecker);
        }
        else{
            iv = new ImageView(type == CheckerType.BLACK ? blackChecker: whiteChecker);
        }

        iv.setTranslateX((BOX_SIZE - BOX_SIZE * 0.38 * 2) / 2);
        iv.setTranslateY((BOX_SIZE - BOX_SIZE * 0.38 * 2) / 2);

        getChildren().addAll(iv);
        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
        });
    }

    public void move(int x, int y) {
        oldX = x * BOX_SIZE;
        oldY = y * BOX_SIZE;
        relocate(oldX, oldY);
    }

    public void wrongMove() {
        relocate(oldX, oldY);
    }
}
class weirdGreenThing extends Pane {
    private double mouseX, mouseY;
    private double oldX, oldY;
    void weirdGreenThing(int x, int y){

    }
}

enum CheckerType {
    //определяет движение по цвету
    //белые всегда ходят обратно по координатам
    BLACK(1), WHITE(-1);
    final int moveDir;
    CheckerType(int moveDir) {
        this.moveDir = moveDir;
    }
}