package be.msec.labgrpc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientScreen extends Application {
    private ArrayList<ChatroomClient> clients;
    private ChatroomClient client;

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        clients = new ArrayList<>();
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(makeInitScene(primaryStage));
        primaryStage.show();
    }

    public Scene makeInitScene(Stage primaryStage) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);


        TextField nameField = new TextField();

        Label nameLabel = new Label("Name");
        Label errorLabel = new Label();

        Button submitClientInfoButton = new Button("Done");

        submitClientInfoButton.setOnAction(Event -> {
            client = new ChatroomClient("localhost", 5050);
            if (client.checkName(nameField.getText())) {
                client.connectUser(nameField.getText());
                /* Change the scene of the primaryStage */
                primaryStage.close();
                primaryStage.setScene(makeChatUI(client));
                primaryStage.setTitle(client.getUser());
                primaryStage.show();
            } else {
                errorLabel.setText("Username already exists.");
            }
        });

        rootPane.add(nameField, 0, 0);
        rootPane.add(nameLabel, 1, 0);
        rootPane.add(submitClientInfoButton, 0, 3, 2, 1);
        rootPane.add(errorLabel, 0, 4);

        return new Scene(rootPane, 400, 400);
    }

    public Scene makeChatUI(ChatroomClient client) {
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);


        ListView<String> chatListView = new ListView<>();
        chatListView.setItems(client.messages);

        setupPriveListView(rootPane);

        TextField chatTextField = new TextField();

        chatTextField.setOnAction(event -> {
            client.sendMessages(chatTextField.getText());
            chatTextField.clear();
        });

        rootPane.add(chatListView, 0, 0);
        rootPane.add(chatTextField, 0, 1);

        return new Scene(rootPane, 600, 400);

    }

    public void setupPriveListView(GridPane rootPane) {
        ListView<PriveGesprek> priveListView = new ListView<>();

        priveListView.setItems(client.privegesprekken);
        priveListView.setOnMouseClicked(event -> {

            PriveGesprek pg = priveListView.getSelectionModel().getSelectedItem();
            //System.out.println("clicked on " + pg.getPartner());
            rootPane.getChildren().remove(priveListView);
            handleListClick(pg, rootPane);

        });
        rootPane.add(priveListView, 1, 0);
    }

    public void handleListClick(PriveGesprek pg, GridPane rootPane) {
        ListView<String> priveListView = new ListView<>();
        priveListView.setItems(pg.getBerichten());
        rootPane.add(priveListView, 1, 0);

        GridPane pane = new GridPane();

        TextField priveTextField = new TextField();
        priveTextField.setOnAction(event -> {
            client.sendPrivateMessage(priveTextField.getText(), pg.getPartner());
            priveTextField.clear();
        });

        Button back = new Button("Back");
        back.setOnAction(Event -> {
            rootPane.getChildren().remove(pane);
            rootPane.getChildren().remove(priveListView);
            setupPriveListView(rootPane);
        });

        pane.add(priveTextField, 0, 0);
        pane.add(back, 1, 0);

        rootPane.add(pane, 1, 1);

    }

    @Override
    public void stop() throws InterruptedException {
        client.stopUser();
        Platform.exit();
        System.exit(0);
    }

}
