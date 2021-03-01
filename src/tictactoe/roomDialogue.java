package tictactoe;

import client.ClientThread;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class roomDialogue extends AnchorPane {

    private ClientThread client = ClientThread.getInstance();
    protected final TextField textField;
    protected final Button button;
    Stage roomDialog;
    protected final Label label;
    protected Pane gridPane;

    protected final TicGrid grid = new TicGrid();


    public roomDialogue(Stage roomdialogExit) {
        roomDialog= roomdialogExit;
        textField = new TextField();
        button = new Button();
        label = new Label();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(112.0);
        setPrefWidth(217.0);

        textField.setLayoutX(82.0);
        textField.setLayoutY(31.0);
        textField.setPrefHeight(25.0);
        textField.setPrefWidth(73.0);
        textField.setPromptText("room id");

        button.setLayoutX(82.0);
        button.setLayoutY(73.0);
        button.setMnemonicParsing(false);
        button.setText("Add Room");
         button.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                String roomId = textField.getText();
                play(client.myName, roomId, "readyGame");
//                Scene s = new Scene(new MainWindow(currentWindow));
//                s.getStylesheets().add(getClass().getResource("MyStyle.css").toString());
//                currentWindow.setScene(s);

            }
            }
        );
         
         

        

        label.setLayoutX(26.0);
        label.setLayoutY(35.0);
        label.setText("Room Id");

        getChildren().add(textField);
        getChildren().add(button);
        getChildren().add(label);
    }

    private void play(String username, String roomId,String type){
            client.ps.println(type+","+username+","+roomId);
            boolean answerFlag = false;
            while(!answerFlag){
                System.out.println("Witing for player to connect");
                if(client.OK == 1){
                    System.out.println("OK");
                    answerFlag=true;
                }
                if(client.OK == 0){
                    System.out.println("NOT OK :(");
                    answerFlag=true;
                    client.OK = 2;
                }
            }
            if(client.OK == 1){
                client.OK = 2;
                roomDialog.close();
                int level=-1;
                gridPane = grid.createContent(MainWindow.GameType.Room, level=-1);
//                client.guiThreadCreated=0;
                answerFlag=false;
        }
    }
}


