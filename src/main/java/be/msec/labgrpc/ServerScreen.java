package be.msec.labgrpc;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerScreen extends Application {

    public static List<ChatroomClient> clients;
    public static ChatroomServer server;
    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        clients = new ArrayList<>();
        primaryStage.setTitle("Chat Server");
        primaryStage.setScene(makeUI(primaryStage));
        primaryStage.show();

    }

    public Scene makeUI(Stage primaryStage) throws IOException {
        GridPane rootPane = new GridPane();
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);


        server = new ChatroomServer(5050);
        server.start();

        Label clientLabel = new Label("Clients Connected");
        ListView<String> clientView = new ListView<String>();
        ObservableList<String> clientList = server.userNames;
        clientView.setItems(clientList);

        rootPane.add(clientLabel, 0, 0);
        rootPane.add(clientView, 0, 1);

        primaryStage.show();

        return new Scene(rootPane, 400, 300);
    }

    @Override
    public void stop() {
        server.stop();
        System.exit(0);
    }
}
