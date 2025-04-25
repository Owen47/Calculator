package hm.app.calculatorapp;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private Button brackets;

    private boolean clearInput = false;


    @FXML
    private void initialize() {
        resultText.clear();
        operationText.setText("");

        Button[] notCalculationButtons = {equals, copy, clear, brackets};

        for (Node node : buttonPane.getChildren()) {
            if (node instanceof Button button && !Arrays.asList(notCalculationButtons).contains(button)) {
                button.setOnAction(event -> {
                    if (clearInput) {
                        brackets.setText("(");
                        operationText.setText(resultText.getText());
                        resultText.setText("");
                        clearInput = false;
                    }
                    resultText.setText(resultText.getText() + button.getText());
                });
            }
        }

    }

    @FXML
    private void calculate() {

        String operation = resultText.getText();
        resultText.clear();

        if (isValidFormula(operation)) {

            operationText.setText(operation);
            // Evaluate everything in the parentheses first
            operation = evaluateParentheses(operation);

            // Now calculate the resulting formatted string
            operation = calculateFormatted(operation);

            resultText.setText(operation);
            clearInput = true;
            brackets.setText("(");
        }
        else
        {
            resultText.setText("Error");
        }

    }

    private boolean isValidFormula(String formula) {
        if (!formula.isEmpty()) {
            for (int i = 0; i < formula.length(); i++) {
                if (Character.isDigit(formula.charAt(i)) || isMathSymbol(formula.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isMathSymbol(char symbol) {
        char[] mathItems = {'x', '-', '+', '÷'};
        for (int i = 0; i < mathItems.length; i++) {
            if (symbol == mathItems[i]) {
                return true;
            }
        }
        return false;
    }


    private String evaluateParentheses(String expr) {
        int start;
        while ((start = expr.lastIndexOf('(')) != -1) {
            int close = expr.indexOf(')', start);
            if (close == -1)
            {
                return "Error";
            }

            String inside = expr.substring(start + 1, close);
            String value  = calculateFormatted(inside);

            expr = expr.substring(0, start) + value + expr.substring(close + 1);
        }
        return expr;
    }


    private String calculateFormatted(String operation)
    {
        for (int i = 0; i < operation.length(); i++) {
            if (operation.charAt(i) == '÷') {

                int startIndex = -1;
                int endIndex   = -1;

                // scan left
                for (int j = i - 1; j >= 0; j--) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        startIndex = j + 1;
                        break;
                    }
                }
                if (startIndex == -1) startIndex = 0;

                // scan right
                for (int j = i + 1; j < operation.length(); j++) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        endIndex = j - 1;
                        break;
                    }
                }
                if (endIndex == -1) endIndex = operation.length() - 1;

                // extract and evaluate
                String part  = operation.substring(startIndex, endIndex + 1);
                String value = divide(part);
                if (!value.isEmpty()) {
                    // replace the division part with its result and recurse
                    String newExpr = operation.substring(0, startIndex) +
                            value +
                            operation.substring(endIndex + 1);
                    return calculateFormatted(newExpr);   // do next operator
                }
                else
                {
                    return "Error";
                }
            }

            if (operation.charAt(i) == 'x')
            {
                int startIndex = -1;
                int endIndex   = -1;

                // scan left
                for (int j = i - 1; j >= 0; j--) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        startIndex = j + 1;
                        break;
                    }
                }
                if (startIndex == -1) startIndex = 0;

                // scan right
                for (int j = i + 1; j < operation.length(); j++) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        endIndex = j - 1;
                        break;
                    }
                }
                if (endIndex == -1) endIndex = operation.length() - 1;

                // extract and evaluate
                String part  = operation.substring(startIndex, endIndex + 1);
                String value = multiply(part);
                if (!value.isEmpty()) {
                    // replace the multiplication part with its result and recurse
                    String newExpr = operation.substring(0, startIndex) +
                            value +
                            operation.substring(endIndex + 1);
                    return calculateFormatted(newExpr);   // do next operator
                }
                else
                {
                    return "Error";
                }
            }


            if (operation.charAt(i) == '+')
            {
                int startIndex = -1;
                int endIndex   = -1;

                // scan left
                for (int j = i - 1; j >= 0; j--) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        startIndex = j + 1;
                        break;
                    }
                }
                if (startIndex == -1) startIndex = 0;

                // scan right
                for (int j = i + 1; j < operation.length(); j++) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        endIndex = j - 1;
                        break;
                    }
                }
                if (endIndex == -1) endIndex = operation.length() - 1;

                // extract and evaluate
                String part  = operation.substring(startIndex, endIndex + 1);
                String value = add(part);
                if (!value.isEmpty()) {
                    // replace the addition part with its result and recurse
                    String newExpr = operation.substring(0, startIndex) +
                            value +
                            operation.substring(endIndex + 1);
                    return calculateFormatted(newExpr);   // do next operator
                }
                else
                {
                    return "Error";
                }
            }

            if (operation.charAt(i) == '-')
            {
                int startIndex = -1;
                int endIndex   = -1;

                // scan left
                for (int j = i - 1; j >= 0; j--) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        startIndex = j + 1;
                        break;
                    }
                }
                if (startIndex == -1) startIndex = 0;

                // scan right
                for (int j = i + 1; j < operation.length(); j++) {
                    if ("+-÷x".indexOf(operation.charAt(j)) != -1) {
                        endIndex = j - 1;
                        break;
                    }
                }
                if (endIndex == -1) endIndex = operation.length() - 1;

                // extract and evaluate
                String part  = operation.substring(startIndex, endIndex + 1);
                String value = subtract(part);
                if (!value.isEmpty()) {
                    // replace the subtraction part with its result and recurse
                    String newExpr = operation.substring(0, startIndex) +
                            value +
                            operation.substring(endIndex + 1);
                    return calculateFormatted(newExpr);   // do next operator
                }
                else
                {
                    return "Error";
                }
            }


        }

        // repeat for x, +, - or just return operation if nothing left
        return operation;
    }

    private String divide(String operation)
    {
        String[] nums = operation.split("÷");
        if (nums.length == 2 && !nums[1].equals("0")) {
            return String.valueOf(((Double.parseDouble(nums[0])) / (Double.parseDouble(nums[1]))));
        }
        else
        {
            return "";
        }
    }

    private String multiply(String operation)
    {
        String[] nums = operation.split("x");
        if (nums.length == 2) {
            return String.valueOf(((Double.parseDouble(nums[0])) * (Double.parseDouble(nums[1]))));
        }
        else
        {
            return "";
        }
    }

    private String add(String operation)
    {
        String[] nums = operation.split("\\+");
        if (nums.length == 2) {
            return String.valueOf(((Double.parseDouble(nums[0])) + (Double.parseDouble(nums[1]))));
        }
        else
        {
            return "";
        }
    }

    private String subtract(String operation)
    {

        String[] nums = operation.split("-");
        if (nums.length == 2) {
            return String.valueOf(((Double.parseDouble(nums[0])) - (Double.parseDouble(nums[1]))));
        }
        else
        {
            return "";
        }
    }


    @FXML
    private void insertBrackets() {

        if (clearInput) {
            brackets.setText("(");
            operationText.setText(resultText.getText());
            resultText.setText("");
            clearInput = false;
        }

        if (Objects.equals(brackets.getText(), "("))
        {
            resultText.setText(resultText.getText() + "(");
            brackets.setText(")");
        }else {
            resultText.setText(resultText.getText() + ")");
            brackets.setText("(");
        }

    }



    @FXML
    private void clear() {
        resultText.setText("");
        brackets.setText("(");
    }

    @FXML
    private void copy() {
        StringSelection selection = new StringSelection(resultText.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

}