package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene mainScene = new Scene(root, 300, 275);
        mainScene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        for(Node n : root.getChildrenUnmodifiable()) {
            System.out.println(n.getId());
        }
            primaryStage.setTitle("Hello World");
        primaryStage.setScene(mainScene);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
