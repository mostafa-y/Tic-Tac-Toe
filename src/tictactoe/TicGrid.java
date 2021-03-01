package tictactoe;

import client.ClientThread;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static tictactoe.logic.findBestMove;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import tictactoe.MainWindow.GameType;

/**
 *
 * @author Mohamed Ali
 */
public class TicGrid {

    GameType gameType;
    String threadMsgFromServer = "";
    String[] threadMsgArrFromServer;
    static Thread replayThread = null, gameEndedPopupThreadS = null, stateThread = null, checkStateThread = null;
    static Thread handleIncomingRequestsThread = null;
    static Thread handleOutgoingRequestsThread = null;
    static boolean outGame = true, STOPGAME = false;
    final public static Combo miniCombo = new Combo();
    public final static ClientThread client = ClientThread.getInstance();
    public static Tile[][] board = new Tile[3][3];
    public final static Pane root = new Pane();
    String FILENAME = "savelastgame.txt";
    MediaPlayer mediaplayer;
    Pane winvideo;
    Scene winscene;
    Stage window;
    public DataInputStream dis;
    int CLICKED = 0;

    Thread updatableThread = null;
    static char passboard[][] = new char[3][3];
    int level, passX = -1, passY = -1, posX = 0, posY = 0;
    public static boolean playable = true, turnX = true, firstround = true, returntox = false;
    public String winner, user = "X", computer = "O", xoro;

    public static List< Combo> combos = new ArrayList<>();
    String message;

    public static void drawThread(int x, int y) {
        board[x][y].drawX();
    }

    public static void drawThreadO(int x, int y) {
        board[x][y].drawO();
    }

    PrintWriter pW = null;

    Pane createContent(GameType type, int level) {
        if(type == GameType.Room)
            handleIncomingRequests(true);

        if (type != GameType.Replay) {
            try {
                PrintWriter pW = new PrintWriter(FILENAME);
                pW.print("");
                pW.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resetBoard();
        this.level = level;
        gameType = type;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Tile tile = new Tile();
                tile.setTranslateX(j * 100);
                tile.setTranslateY(i * 100);
                board[j][i] = tile;
                passboard[j][i] = '_';
                root.getChildren().add(tile);
                System.out.print(passboard[j][i] + " ");
            }
            System.out.println(" ");
        }

        for (int y = 0; y < 3; y++) {
            combos.add(new Combo(board[0][y], board[1][y], board[2][y]));
        }

        // vertical
        for (int x = 0; x < 3; x++) {
            combos.add(new Combo(board[x][0], board[x][1], board[x][2]));
        }

        // diagonals
        combos.add(new Combo(board[0][0], board[1][1], board[2][2]));
        combos.add(new Combo(board[2][0], board[1][1], board[0][2]));

        root.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
            System.out.println(e.getX() + "  " + e.getY());

            if (e.getX() > 0 && e.getX() < 100) {
                passX = 0;
            } else if (e.getX() > 100 && e.getX() < 200) {
                passX = 1;
            } else if (e.getX() > 200 && e.getX() < 300) {
                passX = 2;
            }
            if (e.getY() > 0 && e.getY() < 100) {
                passY = 0;
            } else if (e.getY() > 100 && e.getY() < 200) {
                passY = 1;
            } else if (e.getY() > 200 && e.getY() < 300) {
                passY = 2;
            }
            System.out.println(passX + "  " + passY);
        });
        return root;
    }

    public void checkState() {
        System.out.println("Checking the state...");
        for (Combo combo : combos) {
            if (combo.isComplete()) {
                STOPGAME = true;
                playable = false;
                client.PLAY = false;
                winner = combo.tiles[0].getValue();
                System.out.println("the winner player is : " + winner);
                System.out.println(winner);
                if (winner == "X" && gameType == GameType.Room) {
                    client.ps.println("endGame" + client.myName + ",win");
                    System.out.println("the winner player is : " + winner);
                    client.iWon = "x";
                } else if (winner == "X" && gameType == GameType.AI) {
                    System.out.println("the winner player is : " + winner);
                    client.iWon = "draw";
                }
                playWinAnimation(combo);
            }
        }
    }

    public static void playWinAnimation(Combo combo) {
        Line line = new Line();
        line.setStartX(combo.tiles[0].getCenterX());
        line.setStartY(combo.tiles[0].getCenterY());
        line.setEndX(combo.tiles[0].getCenterX());
        line.setEndY(combo.tiles[0].getCenterY());
        line.setStroke(Color.RED);
        line.setStrokeWidth(5);
        root.getChildren().add(line);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(4000),
                new KeyValue(line.endXProperty(), combo.tiles[2].getCenterX()),
                new KeyValue(line.endYProperty(), combo.tiles[2].getCenterY())));
        timeline.play();
        if(gameEndedPopupThreadS==null){
            gameEndedPopupThreadS = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Popup.display();
                                gameEndedPopupThreadS.stop();
                            }
                        });
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }
            );
            gameEndedPopupThreadS.start();
            
        }
    }

    public static class Combo {

        public Tile[] tiles;

        public Combo(Tile... tiles) {
            this.tiles = tiles;
        }

        public boolean isComplete() {
            if (tiles[0].getValue().isEmpty()) {
                return false;
            }

            return tiles[0].getValue().equals(tiles[1].getValue())
                    && tiles[0].getValue().equals(tiles[2].getValue());
        }

        public void AI(Combo combo) {
            System.out.println("hello combo");
            if (combo.tiles[0].getValue().equals(combo.tiles[1].getValue())) {
                combo.tiles[2].drawO();
                returntox = true;
                System.out.println("hello combo 1 ");
            } else if (combo.tiles[0].getValue().equals(combo.tiles[2].getValue())) {
                combo.tiles[1].drawO();
                returntox = true;
                System.out.println("hello combo 3 ");
            } else if (combo.tiles[1].getValue().equals(combo.tiles[2].getValue())) {
                combo.tiles[0].drawO();
                returntox = true;
                System.out.println("hello combo 4");
            }
        }
    }

    public void replayGame() {
        try {
            FileInputStream fis = new FileInputStream(FILENAME);
            Scanner sc = new Scanner(fis);
            replayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (!sc.hasNext()) {
                                break;
                            }
                            String li = sc.nextLine();
                            String[] strarray = li.split(":");
                            System.out.println(strarray[2]);
                            posX = parseInt(strarray[0]);
                            posY = parseInt(strarray[1]);
                            xoro = strarray[2];
                            Thread.sleep(2000);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (new String(xoro).equals("x")) {
                                        board[posX][posY].drawX();
//                                                System.out.println("ddddddd");
                                    } else if (new String(xoro).equals("o")) {
                                        try {
                                            board[posX][posY].drawO();
                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            client.iWon = "draw";
                                        }
                                    }
                                }
                            });
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            );
            replayThread.start();
        } catch (FileNotFoundException ex) {
        }

    }

    public class Tile extends StackPane {

        Text text = new Text();

        //Combo complay = new Combo();
        public Tile() {
            Rectangle border = new Rectangle(100, 100);
            border.setFill(null);

//            border.setFill(new ImagePattern(img));
            border.setArcWidth(70.0);
            border.setArcHeight(50.0);

            border.setStroke(Color.BLACK);
            getChildren().addAll(border, text);
            text.setFont(Font.font(72));
            setAlignment(Pos.CENTER);
            setOnMouseClicked(event -> {
//                System.out.println(gameType);
                if (gameType == GameType.Replay) {
                    if (playable) {
                        replayGame();
                    }
                    playable = false;
                } else {
                    handlePlaying(gameType, event);
                }
            });
        }

        public void AITurn(int level) {
            if (!playable) {
                return;
            }
            switch (level) {
                case 1: {
                    for (int j = 0; j < 3; j++) {
                        for (int i = 0; i < 3; i++) {
                            if (board[j][i].getValue().isEmpty()) {
                                board[j][i].drawO();
                                FileOutputStream fos;
                                try {
                                    fos = new FileOutputStream(FILENAME, true);
                                    PrintWriter pw = new PrintWriter(fos);
                                    //System.out.println("level 11111111");	
                                    pw.println(j + ":" + i + ":o");
                                    pw.close();
                                } catch (FileNotFoundException ex) {
                                    Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                turnX = true;
                                checkState();
                                return;
                            }
                        }
                    }
                }
                break;
                case 2: {
                    logic.Move bestMove = findBestMove(passboard);
                    try {
                        passboard[bestMove.row][bestMove.col] = 'o';
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    try {
                        board[bestMove.row][bestMove.col].drawO();
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }

                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(FILENAME, true);
                        PrintWriter pw = new PrintWriter(fos);
                        pw.println(bestMove.row + ":" + bestMove.col + ":o");
                        System.out.println(bestMove.row + ":" + bestMove.col + ":x");
                        pw.close();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    checkState();
                    turnX = true;

                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            System.out.print(passboard[j][i] + " ");
                        }
                        System.out.println(" ");
                    }
                }
                break;
            }
        }

        public double getCenterX() {
            return getTranslateX() + 50;
        }

        public double getCenterY() {
            return getTranslateY() + 50;
        }

        public String getValue() {
            return text.getText();
        }

        public void drawX() {
            text.setText(user);
//            checkState();
        }

        public void drawO() {
            text.setText(computer);
//            checkState();
        }

        public void handlePlaying(GameType gameType, MouseEvent event) {
            if (gameType != GameType.None) {
                System.out.println(gameType);
                if (!playable) {
                    return;
                }
                // MOSTAFA
                switch (gameType) {
                    case Replay:
                        if (event.getButton() == MouseButton.PRIMARY) {
                            playable = false;
                            gameType = GameType.None;
                            replayGame();
                        }
                    case AI:
                        if (event.getButton() == MouseButton.PRIMARY) {
                            gameAIHandler();
                        }
                        if (level != -1 && !turnX) {
                            System.out.println("AI Turn");
                            AITurn(level);
                        }
                        break;
                    case Local:
                        if (event.getButton() == MouseButton.PRIMARY) {
                            handleLocal();
                        }
                        break;
                    case Room:
                        if (event.getButton() == MouseButton.PRIMARY) {
                            if (turnX){
                                drawX();
                                turnX = false;
                                client.PLAY=false;
//                                runDrawThread();
                                String msg = playNetwork(client.myName, client.opponent, passX, passY);
                            }
                        }
                        break;
                }
            }
        }

        public void handleLocal() {
            if (!turnX) {
                drawO();
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(FILENAME, true);
                    PrintWriter pw = new PrintWriter(fos);
                    System.out.println(passX + ":" + passY + ":x");
                    pw.println(passX + ":" + passY + ":o");
                    pw.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                }
                turnX = !turnX;
                checkState();
            } else {
                passboard[passX][passY] = 'x';
                drawX();
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(FILENAME, true);
                    PrintWriter pw = new PrintWriter(fos);
                    System.out.println(passX + ":" + passY + ":x");
                    pw.println(passX + ":" + passY + ":x");
                    pw.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                }
                turnX = !turnX;
                checkState();
            }
        }

        // Handle AI game
        public void gameAIHandler() {
            if (!turnX) {
                drawO();
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(FILENAME, true);
                    PrintWriter pw = new PrintWriter(fos);
                    System.out.println(passX + ":" + passY + ":x");
                    pw.println(passX + ":" + passY + ":o");
                    pw.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                }
                turnX = !turnX;
                checkState();
            } else {
                passboard[passX][passY] = 'x';
                drawX();
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(FILENAME, true);
                    PrintWriter pw = new PrintWriter(fos);
                    System.out.println(passX + ":" + passY + ":x");
                    pw.println(passX + ":" + passY + ":x");
                    pw.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TicGrid.class.getName()).log(Level.SEVERE, null, ex);
                }
                turnX = !turnX;
                checkState();
            }
        }

        // DRAW THREAD
        public void runDrawThread() {
            Thread drawThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX");
                                drawX();
                                turnX = false;
                                client.PLAY = false;
                            }
                        });
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
            );
            drawThread.start();
        }
    }

    public void handleIncomingRequests(boolean startMe) {
        if (handleIncomingRequestsThread == null) {
            System.out.println("STARTING THE HANDLE INCOMING THREAD REQUESTS");
            //wait and draw from server
            // first thing when initatiated 
            // if I clicked mouse 
            //      then> drawX()
            // then> listen to serever()
            //       when server responds
            //          > drawO()
            handleIncomingRequestsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            System.out.println("Listeneing for incomming moves...");
                            Thread.sleep(1000);
                            threadMsgFromServer = client.message;
//                            threadMsgArrFromServer = threadMsgFromServer.split(",");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run(){
                                if(threadMsgFromServer.split(",")[1].equals(client.myName)){
                                    if(threadMsgFromServer.split(",")[0].equals("play")){
                                        if(threadMsgFromServer.split(",").length == 4){
                                            if(threadMsgFromServer.split(",")[3].equals("-1"))
                                                client.opponent = new String(threadMsgFromServer.split(",")[2]);
                                        }

                                        if (threadMsgFromServer.split(",").length == 6){
                                            if(threadMsgFromServer.split(",")[5].equals(client.myName)){
                                            }
                                            else if(threadMsgFromServer.split(",")[5].equals("non")){
                                                client.opponent = new String(threadMsgFromServer.split(",")[2]);
                                                System.out.println("ENEMEYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
                                                int x = parseInt(threadMsgFromServer.split(",")[3]);
                                                System.out.println("[OPPONENET X]>>> " + x);
                                                int y = parseInt(threadMsgFromServer.split(",")[4]);
                                                System.out.println("[OPPONENET Y]>>> " + x);
                                                board[x][y].drawO();
                                                client.PLAY = true;
                                                turnX=true;
                                            }
                                        }
                                    }
                                }
                                    checkState();
//                                    System.out.println("[threadMsgFromServer]>>> " + threadMsgFromServer);
//                                    System.out.println("[threadMsgFromServer.split(\",\").length]>>> " + threadMsgFromServer.split(",").length);
//                                    System.out.println("[client.PLAY]>>> " + client.PLAY);
//                                    if (threadMsgFromServer.split(",").length == 6){
//                                        if(threadMsgFromServer.split(",")[5].equals("non"))
//                                            client.PLAY = false;
//                                        System.out.println("[OPPONENET] played>>> " + threadMsgFromServer);
//                                        int x = parseInt(threadMsgFromServer.split(",")[3]);
//                                        System.out.println("[OPPONENET X]>>> " + x);
//                                        int y = parseInt(threadMsgFromServer.split(",")[4]);
//                                        System.out.println("[OPPONENET Y]>>> " + x);
//                                        String turn = new String(threadMsgFromServer.split(",")[5]);
//                                        System.out.println("[turn value]>>> " + turn);
//                                        try{
//                                            System.out.println("[Drawing opponent]>>> " + threadMsgFromServer);
//                                            if (turn.equals(client.myName)){
//                                                System.out.println("ENEMEYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
//                                                board[x][y].drawO();
//                                                client.PLAY = true;
//                                                turnX=true;
//                                                checkState();
//                                            }
//                                        } catch (ArrayIndexOutOfBoundsException e) {
//                                            System.out.println("That's a draw");
//                                            client.iWon = "draw";
//                                        }
//                                    }
                                }
                            });
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            );
            handleIncomingRequestsThread.start();
        }
    }

    //play,hossam,chris,0,0
    public String playNetwork(String myName, String opponent, int posX, int posY) {
        client.ps.println("play," + myName + "," + opponent + "," + posX + "," + posY);
        turnX=false;
        boolean answerFlag = false;
        while (!answerFlag) {
            System.out.println("HAAAAI");
            if (client.OK == 1){
                System.out.println("OK");
                answerFlag = true;
            }
            if (client.OK == 0){
                System.out.println("NOT OK :(");
                answerFlag = true;
                client.OK = 2;
            }
        }
        if (client.OK == 1) {
            String response = client.getMessage();
            client.OK = 2;
            answerFlag = false;
            System.out.println("SERVER RESPONSE" + response);
            return response;
        }
        return "";
    }

    public void resetBoard() {
        threadMsgFromServer = "";
        String[] threadMsgArrFromServer;
        replayThread = null;
        gameEndedPopupThreadS = null;
        stateThread = null;
        checkStateThread = null;
        handleIncomingRequestsThread = null;
        handleOutgoingRequestsThread = null;
        outGame = true;
        STOPGAME = false;
        Combo miniCombo = new Combo();
        Tile[][] board = new Tile[3][3];
        Pane root = new Pane();

        DataInputStream dis;
        CLICKED = 0;

        Thread updatableThread = null;

        int level, passX = -1, passY = -1, posX = 0, posY = 0;

        playable = true;
        turnX = true;
        firstround = true;
        returntox = false;

        winner = "";
        user = "X";
        computer = "O";
        xoro = "";

        if (replayThread != null) {
            replayThread.stop();
            replayThread = null;
        }
        if (stateThread != null) {
            stateThread.stop();
            stateThread = null;
        }
        if (stateThread != null) {
            stateThread.stop();
            stateThread = null;
        }
        outGame = true;
        user = "X";
        computer = "O";
        level = -1;
        playable = true;
        turnX = true;
        winner = "";
        board = new Tile[3][3];
        TicGrid.passboard = new char[3][3];
        passX = -1;
        passY = -1;
        posX = 0;
        posY = 0;
//        AIEnabled=false;
//        roomEnabled = false;
//        optionBtnClicked=false;
        xoro = "";
        combos = new ArrayList<>();
        firstround = true;
        returntox = false;
    }

}