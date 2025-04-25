package hm.app.calculatorapp;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;


public class CalculatorController {

    @FXML private Label     operationText;
    @FXML private TextField resultText;
    @FXML private Button    equals;
    @FXML private Button    copy;
    @FXML private Button    clear;
    @FXML private AnchorPane buttonPane;
    @FXML private Button    brackets;

    // flag so the very next digit press replaces the previous result
    private boolean clearInput = false;


    /**
     * Initializes all buttons and text fields for the calculator to run
     */
    @FXML private void initialize() {
        resultText.clear();
        operationText.setText("");

        List<Button> reserved = List.of(equals, copy, clear, brackets);

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
    @FXML private void calculate() {
        String expr = resultText.getText();
        resultText.clear();

        if (isValidFormula(expr)) {
            setError();
            return;
        }

        operationText.setText(expr);

        // 1. deal with parentheses
        expr = evaluateParentheses(expr);
        if (isError(expr)) return;

        // 2. evaluate remaining expression
        expr = calculateFormatted(expr);
        if (isError(expr)) return;

        resultText.setText(expr);
        clearInput = true;
        brackets.setText("(");
    }

    /**
     * Triggers when the brackets button is pressed, places either an opening or closing bracket in the view depending on which is already placed
     */
    @FXML private void insertBrackets() {
        if (clearInput) {
            resetForNextEntry();
        }

        if ("(".equals(brackets.getText())) {
            resultText.appendText("(");
            brackets.setText(")");
        } else {
            resultText.appendText(")");
            brackets.setText("(");
        }
    }

    /**
     * clears the input section
     */
    @FXML private void clear() {
        resultText.clear();
        operationText.setText("");
        brackets.setText("(");
    }

    /**
     * copies whatever is in the input section to clipboard
     */
    @FXML private void copy() {
        StringSelection sel = new StringSelection(resultText.getText());
        Clipboard copy = Toolkit.getDefaultToolkit().getSystemClipboard();
        copy.setContents(sel, null);
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
            if (!Character.isDigit(c) && "x-+÷()".indexOf(c) == -1) {
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
        for (char symbol : new char[]{'÷', 'x', '+', '-'}) {
            int i = indexOfTopLevel(expr, symbol);
            if (i != -1) {
                String left  = expr.substring(0, i);
                String right = expr.substring(i + 1);
                if (left.isEmpty() || right.isEmpty())
                {
                    return setError();
                }
                String l = calculateFormatted(left);
                String r = calculateFormatted(right);
                if (isError(l) || isError(r))
                {
                    return setError();
                }
                return switch (symbol) {
                    case '÷' -> divide(l, r);
                    case 'x' -> multiply(l, r);
                    case '+' -> add(l, r);
                    case '-' -> subtract(l, r);
                    default -> setError();
                };
            }
        }
        try {
            Double.parseDouble(expr);
            return expr;
        } catch (NumberFormatException e) {
            return setError();
        }
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

    /**
     * Preforms division on two numbers
     * @param numerator the numerator
     * @param denominator the denominator
     * @return the quotient
     */
    private String divide(String numerator, String denominator) {
        if (denominator.equals("0"))
        {
            return setError();
        }
        try {
            return String.valueOf(Double.parseDouble(numerator) / Double.parseDouble(denominator));
        }
        catch (NumberFormatException e) {
            return setError();
        }
    }

    /**
     * multiplies two numbers together
     * @param a number 1
     * @param b number 2
     * @return the product of the two numbers
     */
    private String multiply(String a, String b) {
        try {
            return String.valueOf(Double.parseDouble(a) * Double.parseDouble(b));
        }
        catch (NumberFormatException e) {
            return setError();
        }
    }

    /**
     * adds two numbers together
     * @param a number 1
     * @param b number 2
     * @return the sum of the two numbers
     */
    private String add(String a, String b) {
        try {
            return String.valueOf(Double.parseDouble(a) + Double.parseDouble(b));
        }
        catch (NumberFormatException e) {
            return setError();
        }
    }

    /**
     * subtracts two numbers
     * @param a number 1
     * @param b number 2
     * @return the subtraction of the two numbers
     */
    private String subtract(String a, String b) {
        try {
            return String.valueOf(Double.parseDouble(a) - Double.parseDouble(b));
        }
        catch (NumberFormatException e) {
            return setError();
        }
    }

    /**
     * Puts an error message on the result / input screen
     * @return returns that there is an error
     */
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
        brackets.setText("(");
        operationText.setText(resultText.getText());
        resultText.clear();
        clearInput = false;
    }
}