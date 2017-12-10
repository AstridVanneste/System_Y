package GUI;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;
import java.awt.event.MouseEvent;

public class View extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        //primaryStage.setTitle("Hello World");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

}
