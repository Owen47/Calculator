package hm.app.calculatorapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CalculatorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CalculatorApplication.class.getResource("Calculator.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 455, 671);
        stage.setTitle("Calculator");
        // Load and set icon
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/app-icon.png"))));

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}