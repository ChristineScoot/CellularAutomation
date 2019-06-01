package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../view/grainGrowthView.fxml"));
        primaryStage.setTitle("Cellular automation");
        primaryStage.setScene(new Scene(root, 1000, 700, Color.WHITE));
        primaryStage.getIcons().add(new Image("file:icons03.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
