package hm.app.calculatorapp;

import hm.shell.MathOperations;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.math.BigDecimal;
import java.util.List;


// Gets our math operations


public class CalculatorController {

    @FXML private Label     operationText;
    @FXML private TextField resultText;
    @FXML private Button    equals;
    @FXML private Button    clear;
    @FXML private AnchorPane buttonPane;
    @FXML private Button    leftBracket;
    @FXML private Button    rightBracket;
    @FXML private AnchorPane root;

    // flag so the very next digit press replaces the previous result
    private boolean clearInput = false;


    /**
     * Initializes all buttons and text fields for the calculator to run
     */
    @FXML
    private void initialize() {
        resultText.clear();
        operationText.setText("");
        List<Button> reserved = List.of(equals, clear, leftBracket, rightBracket);

        for (Node node : buttonPane.getChildren()) {
            if (node instanceof Button btn && !reserved.contains(btn)) {
                btn.setOnAction(e -> {
                    if (clearInput) {
                        resetForNextEntry();
                    }
                    resultText.appendText(btn.getText());

                });
            }
        }
    }


    /**
     * Triggers when the equals button is pressed, calculates whatever is in the result text field
     */
    @FXML
    private void calculate() {
        String expr = resultText.getText();
        resultText.clear();

        if (isValidFormula(expr)) {
            setError();
            return;
        }

        operationText.setText(expr);

        // format bracket multiplication
        expr = checkForBracketMultiplication(expr);

        // 1. deal with parentheses
        expr = evaluateParentheses(expr);
        if (isError(expr))
        {
            clearInput = true;
            return;
        }

        System.out.println(expr);

        // 2. evaluate remaining expression
        expr = calculateFormatted(expr);
        if (isError(expr))
        {
            clearInput = true;
            return;
        }

        resultText.setText(expr);
    }

    /**
     * Triggers when the brackets button is pressed, places either an opening or closing bracket in the view depending on which is already placed
     */
    @FXML
    private void insertLeftBracket() {
        if (clearInput) {
            resetForNextEntry();
        }

        resultText.appendText("(");
    }

    @FXML
    private void insertRightBracket() {
        if (clearInput) {
            resetForNextEntry();
        }

        resultText.appendText(")");
    }

    private String checkForBracketMultiplication(String expr) {
        for (int i = 0; i < expr.length(); i++) {
            if (expr.charAt(i) == '(' && i != 0 && isNumber(String.valueOf(expr.charAt(i - 1)))) {
                // insert a multiplication symbol into the operation
                expr = expr.substring(0, i) + "x" + expr.substring(i);
            }
            if (expr.charAt(i) == ')' && i + 1 < expr.length() && isNumber(String.valueOf(expr.charAt(i + 1)))) {
                expr = expr.substring(0, i + 1) + "x" + expr.substring(i + 1);
            }
            if (expr.charAt(i) == ')' && i + 1 < expr.length() && expr.charAt(i + 1) == '(' ) {
                expr = expr.replace(")(", ")x(");
            }
        }
        return expr;
    }



    private boolean isNumber(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }


    /**
     * clears the input section
     */
    @FXML
    private void deleteOne() {
        if (!isError(resultText.getText())) {
            resultText.setText(resultText.getText().replaceAll(".$", ""));
        }

    }


    /**
     * checks if a provided formula is valid.
     * @param s the formula to check
     * @return true if valid
     */
    private boolean isValidFormula(String s) {
        return s.isEmpty() || !hasBalancedParentheses(s) || !hasOnlyAllowedChars(s) || hasConsecutiveOperators(s) || startsOrEndsWithOperator(s);
    }

    /**
     * checks if the parentheses in an equation are equal, e.g. same amount of ( as )
     * @param s the formula to check
     * @return true if valid
     */
    private boolean hasBalancedParentheses(String s) {
        int depth = 0;
        for (char c : s.toCharArray()) {
            if (c == '(')
            {
                depth++;
            } else if (c == ')')
            {
                depth--;
            }
            if (depth < 0)
            {
                return false;
            }
        }
        return depth == 0;
    }

    /**
     * checks if a formula has illegal characters
     * @param s the formula to check
     * @return true if valid
     */
    private boolean hasOnlyAllowedChars(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c) && "x-+÷().".indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks if multiple operators are put in a row
     * @param s the formula to check
     * @return true if it has multiple operators (invalid formula)
     */
    private boolean hasConsecutiveOperators(String s) {
        String ops = "x-+÷";
        for (int i = 1; i < s.length(); i++) {
            if (ops.indexOf(s.charAt(i)) != -1 && ops.indexOf(s.charAt(i-1)) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if a formula starts or ends with an operator, e.g. +53x3
     * @param s the formula to check
     * @return true if it starts or ends with an operator (invalid formula)
     */
    private boolean startsOrEndsWithOperator(String s) {
        String ops = "x+÷";
        return ops.indexOf(s.charAt(0)) != -1 || "x-+÷".indexOf(s.charAt(s.length()-1)) != -1;
    }

    /**
     * evaluates all the parentheses in an equation, and provides the simplified version
     * @param expr the formula to check
     * @return the simplified formula
     */
    private String evaluateParentheses(String expr) {
        int open;
        while ((open = expr.lastIndexOf('(')) != -1) {
            int close = expr.indexOf(')', open);
            if (close == -1) {
                return setError();
            }
            String inside = expr.substring(open + 1, close);
            if (isValidFormula(inside)) {
                return setError();
            }
            String value = calculateFormatted(inside);
            if (isError(value)) return value;
            expr = expr.substring(0, open) + value + expr.substring(close + 1);
        }
        return expr;
    }

    /**
     * calculates a properly formatted formula, one that doesn't have and parentheses
     * @param expr the formula to check
     * @return the calculated number
     */
    private String calculateFormatted(String expr) {
        for (char op : new char[]{'÷','x','+','-'}) {
            int i = indexOfTopLevel(expr, op);
            if (i != -1) {
                String left  = calculateFormatted(expr.substring(0, i));
                String right = calculateFormatted(expr.substring(i + 1));

                if (isError(left) || isError(right)) return setError();

                try {
                    double val = switch (op) {
                        case '÷' -> MathOperations.divide(left, right);
                        case 'x' -> MathOperations.multiply(left, right);
                        case '+' -> MathOperations.add(left, right);
                        case '-' -> MathOperations.subtract(left, right);
                        default -> throw new AssertionError();
                    };
                    return formatAnswer(val);
                } catch (MathOperations.CalcException ex) {
                    return setError();
                }
            }
        }

        /* leaf should be a pure number */
        try {

            return formatAnswer(Double.parseDouble(expr));
        } catch (NumberFormatException e) {
            return setError();
        }
    }

    private String formatAnswer(Double expr) {
        return BigDecimal.valueOf(expr).stripTrailingZeros().toPlainString();
    }

    /**
     * gets the index position of the top level of parentheses
     * @param expr the formula to check
     * @param symbol the symbol to check for. in this case always either ( or )
     * @return the index of the parentheses
     */
    private int indexOfTopLevel(String expr, char symbol) {
        int depth = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(')
            {
                depth++;
            } else if (c == ')')
            {
                depth--;
            } else if (depth == 0 && c == symbol)
            {
                return i;
            }
        }
        return -1;
    }


    private String setError() {
        resultText.setText("Error");
        clearInput = true;
        return "Error";
    }


    /**
     * checks if there is an error. looks for the word "error"
     * @param s the string to check
     * @return true if there is an error
     */
    private boolean isError(String s) {
        return "Error".equals(s);
    }

    /**
     * Resets the calculator UI so it is ready for another operation
     */
    private void resetForNextEntry() {
        operationText.setText(resultText.getText());
        resultText.clear();
        clearInput = false;
    }

    @FXML
    private void clear() {
        resultText.clear();
    }

}