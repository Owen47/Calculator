package hm.app.calculatorapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CalculatorController {
    @FXML
    private Label operationText;
    @FXML
    private TextField resultText;

    @FXML
    private void initialize() {
        resultText.clear();
        operationText.setText("");
    }

    @FXML
    private void calculate() {

    }

}