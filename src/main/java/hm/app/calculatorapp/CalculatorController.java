package hm.app.calculatorapp;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.util.Arrays;

public class CalculatorController {
    @FXML
    private Label operationText;
    @FXML
    private TextField resultText;
    @FXML
    private Button equals;
    @FXML
    private Button copy;
    @FXML
    private Button clear;
    @FXML
    private AnchorPane buttonPane;

    @FXML
    private void initialize() {
        resultText.clear();
        operationText.setText("");

        Button[] notCalculationButtons = {equals, copy, clear};

        for (Node node : buttonPane.getChildren()) {
            if (node instanceof Button button && !Arrays.asList(notCalculationButtons).contains(button)) {
                button.setOnAction(event -> {
                    System.out.println("Button clicked: " + button.getText());
                    // your logic here
                });
            }
        }

    }

    @FXML
    private void calculate() {

    }

}