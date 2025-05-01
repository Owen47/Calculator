package hm.app.calculatorapp;

import hm.shell.MathOperations;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Pattern;


public class CalculatorController {

    /*------------------------------------------------------------------
     *  FXML‑injected view references
     *------------------------------------------------------------------*/
    @FXML private Label      operationText;
    @FXML private TextField  resultText;
    @FXML private Button     equals;
    @FXML private Button     clear;
    @FXML private AnchorPane buttonPane;
    @FXML private Button     leftBracket;
    @FXML private Button     rightBracket;
    @FXML private Button     power;
    @FXML private Button     sqrt;
    @FXML private Button     factorial;
    @FXML private Button     delete;

    /*------------------------------------------------------------------
     *  Constants / helpers
     *------------------------------------------------------------------*/
    private static final char[] SUPERSCRIPT_DIGITS = { '⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹' };
    private static final char[] OPERATORS          = { '÷','x','+','-' };
    private static final char[] PRIMARY_OPERATORS = { '÷','x' };
    private static final char[] SECONDARY_OPERATORS = { '+','-' };

    /** Flag: resets calculator input screen */
    private boolean clearInput = false;

    /** Flag: superscript input toggle */
    private boolean superscriptMode = false;

    /** Flag: square root input toggle */
    private boolean sqrtMode = false;


    /*------------------------------------------------------------------
     *  Initialisation
     *------------------------------------------------------------------*/
    @FXML
    private void initialize() {
        resultText.clear();
        operationText.setText("");

        List<Button> reserved = List.of(equals, clear, leftBracket, rightBracket, power, sqrt, delete, factorial);

        // Auto‑hook every non‑reserved button
        for (Node node : buttonPane.getChildren()) {
            if (node instanceof Button btn) {
                btn.setFocusTraversable(false);
            }
            if (node instanceof Button btn && !reserved.contains(btn)) {
                btn.setOnAction(e -> appendCharacter(btn.getText()));
            }
        }
    }

    private void appendCharacter(String ch) {

        if (clearInput)
        {
            resetForNextEntry();
        }
        else
        {
            if (!isDigit(ch))
            {
                // exit superscript mode when a non-numeric key is pressed
                superscriptMode = false;
            }
            if (superscriptMode)
            {
                resultText.insertText(resultText.getCaretPosition(), toSuperscript(ch));
            }
            else
            {
                resultText.insertText(resultText.getCaretPosition(), ch);
            }
            update();
        }



    }

    /*------------------------------------------------------------------
     *  button handlers
     *------------------------------------------------------------------*/

    @FXML private void setPower() {
        if (clearInput)
        {
            resetForNextEntry();
        }
        else
        {
            superscriptMode = !superscriptMode;
            update();
        }

    }

    @FXML private void setSqrt() {
        superscriptMode = false;
        if (clearInput)
        {
            resetForNextEntry();
        }
        else
        {
            sqrtMode = !sqrtMode;
            if (sqrtMode)
            {
                resultText.insertText(resultText.getCaretPosition(), "√()");
                resultText.positionCaret(resultText.getText().lastIndexOf("√") + 2);
            }
            else
            {
                resultText.positionCaret(resultText.getText().length());
            }
            update();
        }
    }

    @FXML private void setFactorial() {
        superscriptMode = false;
        if (clearInput) resetForNextEntry();
        resultText.insertText(resultText.getCaretPosition(), "!");
        update();
    }

    @FXML private void insertLeftBracket()  {
        insertBracket("(");
        update();
    }
    @FXML private void insertRightBracket() {
        insertBracket(")");
        update();
    }

    private void insertBracket(String b) {
        superscriptMode = false;
        if (clearInput) resetForNextEntry();

        resultText.insertText(resultText.getCaretPosition(), b);
    }

    @FXML
    private void deleteOne() {
        if (!isError(resultText.getText()) && resultText.getCaretPosition() > 0)
        {
            resultText.deleteText(resultText.getCaretPosition() - 1, resultText.getCaretPosition());
            update();
        }

    }

    @FXML
    private void clear() {
        resultText.clear();
        operationText.setText("");
        superscriptMode = false;
        sqrtMode = false;
        update();
    }

    /*------------------------------------------------------------------
     *  Main evaluation
     *------------------------------------------------------------------*/
    @FXML
    private void calculate() throws MathOperations.CalcException {
        superscriptMode = false;
        sqrtMode = false;
        String expr = resultText.getText();
        resultText.clear();
        operationText.setText(expr);

        // 1) tidy input – negatives, implicit multiplications, superscripts, square roots
        expr = preprocess(expr);
        if (isError(expr)) return;

        // 2) resolve parentheses recursively
        expr = evaluateParentheses(expr);
        if (isError(expr)) return;

        // 3) final power sweep (e.g. nested parentheses produced new superscripts)
        expr = handlePowers(expr);
        if (isError(expr)) return;

        // 4) straightforward left‑to‑right evaluation honouring precedence
        expr = calculateFormatted(expr);
        if (isError(expr)) return;

        // 5) round & display
        expr = round(expr);
        resultText.setText(expr);
        resultText.positionCaret(resultText.getText().length());
        update();
    }

    /*------------------------------------------------------------------
     *  Pre‑processing helpers
     *------------------------------------------------------------------*/

    private String preprocess(String expr) throws MathOperations.CalcException {
        expr = checkForNegativeNumbers(expr);
        expr = insertImplicitMultiplication(expr);
        expr = handlePowers(expr);
        expr = handleSqrt(expr);
        if (isValidFormula(expr)) return setError();
        expr = checkForNegativeNumbers(expr); // re‑run after new ‘x’ insertions
        return expr;
    }

    /*--------------------------------------------------------------
     *  Superscript handling
     *--------------------------------------------------------------*/

    private String handlePowers(String expr) throws MathOperations.CalcException {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (charContains(SUPERSCRIPT_DIGITS, c)) {
                /* Collect exponent */
                int expStart = i;
                while (i < expr.length() && charContains(SUPERSCRIPT_DIGITS, expr.charAt(i))) i++;
                String exponentSup = expr.substring(expStart, i);
                String exponent    = convertSuperscriptToNormal(exponentSup);

                /* Locate & evaluate base */
                int baseEnd = expStart; // exclusive
                int baseStart;
                String baseValue; // numeric string we will feed to MathOperations

                if (expr.charAt(baseEnd - 1) == ')') {
                    // Case:  ( ... )² → find matching '('
                    int depth = 0;
                    baseStart = baseEnd - 1;
                    while (baseStart >= 0) {
                        char ch = expr.charAt(baseStart);
                        if (ch == ')') depth++;
                        else if (ch == '(') {
                            depth--;
                            if (depth == 0) break;
                        }
                        baseStart--;
                    }
                    if (baseStart < 0) return setError(); // unmatched bracket

                    String inside = expr.substring(baseStart + 1, baseEnd - 1);
                    inside = calculateFormatted(inside);
                    if (isError(inside)) return inside;
                    baseValue = inside;

                    // remove entire ( ... ) from output buffer
                    out.delete(out.length() - (baseEnd - baseStart), out.length());
                } else {
                    // Case: plain number right before exponent
                    baseStart = baseEnd - 1;
                    while (baseStart >= 0 && !charContains(OPERATORS, expr.charAt(baseStart)) && expr.charAt(baseStart) != '(') {
                        baseStart--;
                    }
                    baseStart++;
                    baseValue = expr.substring(baseStart, baseEnd);
                    out.delete(out.length() - (baseEnd - baseStart), out.length());
                }

                /* Compute and append */
                String computed = String.valueOf(MathOperations.power(baseValue, exponent));
                out.append(computed);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    public  String toSuperscript(String input) {
        return input
                .replace('0', '⁰')
                .replace('1', '¹')
                .replace('2', '²')
                .replace('3', '³')
                .replace('4', '⁴')
                .replace('5', '⁵')
                .replace('6', '⁶')
                .replace('7', '⁷')
                .replace('8', '⁸')
                .replace('9', '⁹');
    }

    private String convertSuperscriptToNormal(String input) {
        return input
                .replace('⁰', '0')
                .replace('¹', '1')
                .replace('²', '2')
                .replace('³', '3')
                .replace('⁴', '4')
                .replace('⁵', '5')
                .replace('⁶', '6')
                .replace('⁷', '7')
                .replace('⁸', '8')
                .replace('⁹', '9');
    }


    /*--------------------------------------------------------------
     *  Square Root Handling
     *--------------------------------------------------------------*/

    private String handleSqrt(String expr) {
        int idx;
        while ((idx = expr.indexOf('√')) != -1) {
            // square root with no opening bracket
            if (idx + 1 >= expr.length() || expr.charAt(idx + 1) != '(')
                return setError();

            // finds the depth
            int start = idx + 2; // skip √(
            int depth = 1;
            int end = start;

            while (end < expr.length() && depth > 0) {
                char c = expr.charAt(end);
                if (c == '(') depth++;
                else if (c == ')') depth--;
                end++;
            }

            // brackets are wrong
            if (depth != 0) return setError();

            String inside = expr.substring(start, end - 1);
            String evaluatedInside;
            try {
                evaluatedInside = calculateFormatted(preprocess(inside));
                if (isError(evaluatedInside)) return setError();
            } catch (MathOperations.CalcException e) {
                return setError();
            }

            double result;
            try {
                result = MathOperations.sqrt(evaluatedInside);
            } catch (MathOperations.CalcException e) {
                return setError();
            }

            // replace with result
            String before = expr.substring(0, idx);
            String after = expr.substring(end);
            expr = before + result + after;
        }

        return expr;
    }

    /*--------------------------------------------------------------
     *  Factorial Handling
     *--------------------------------------------------------------*/



    /*--------------------------------------------------------------
     *  Parsing / validation utilities
     *--------------------------------------------------------------*/

    private boolean isDigit(String s) {
        return s.length() == 1 && Character.isDigit(s.charAt(0));
    }

    private boolean charContains(char[] arr, char c) {
        for (char x : arr) {
            if (x == c) return true;
        }
        return false;
    }

    private boolean stringContains(char[] arr, String str) {
        int count = 0;
        for (char x : arr) {
            if (charContains(arr, str.charAt(count)))
            {
                count++;
                return true;
            }
        }
        return false;
    }

    private String checkForNegativeNumbers(String expr) {
        if (expr.startsWith("-")) expr = "0" + expr;
        return expr.replace("(-", "(0-"); // handle negatives immediately after ‘(’
    }

    private String insertImplicitMultiplication(String expr) {
        for (int i = 0; i < expr.length() - 1; i++) {
            char cur = expr.charAt(i);
            char nxt = expr.charAt(i + 1);
            // number)(number, number(, )number, )(
            if ((cur == ')' && (Character.isDigit(nxt) || nxt == '(')) ||
                    (nxt == '(' && Character.isDigit(cur))) {
                expr = expr.substring(0, i + 1) + 'x' + expr.substring(i + 1);
                i++; // skip inserted char
            }
        }
        return expr;
    }

    /*--------------------------------------------------------------
     *  Formula validation
     *--------------------------------------------------------------*/

    private boolean isValidFormula(String s) {
        return s.isEmpty() || !hasBalancedParentheses(s) || !hasOnlyAllowedChars(s)
                || hasConsecutiveOperators(s) || startsOrEndsWithOperator(s);
    }

    private boolean hasBalancedParentheses(String s) {
        int depth = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') depth++; else if (c == ')') depth--;
            if (depth < 0) return false;
        }
        return depth == 0;
    }

    private boolean hasOnlyAllowedChars(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c) && "x-+÷().!".indexOf(c) == -1 && !charContains(SUPERSCRIPT_DIGITS, c)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasConsecutiveOperators(String s) {
        String ops = "x-+÷";
        for (int i = 1; i < s.length(); i++) {
            if (ops.indexOf(s.charAt(i)) != -1 && ops.indexOf(s.charAt(i - 1)) != -1) return true;
        }
        return false;
    }

    private boolean startsOrEndsWithOperator(String s) {
        String ops = "x+÷";
        return ops.indexOf(s.charAt(0)) != -1 || "x-+÷".indexOf(s.charAt(s.length() - 1)) != -1;
    }

    /*--------------------------------------------------------------
     *  Parentheses evaluation & expression calculation
     *--------------------------------------------------------------*/

    private String evaluateParentheses(String expr) {
        int open;
        while ((open = expr.lastIndexOf('(')) != -1) {
            int close = expr.indexOf(')', open);
            if (close == -1) return setError();

            String inside = expr.substring(open + 1, close);
            if (isValidFormula(inside)) return setError();


            String value = calculateFormatted(inside);
            if (isError(value)) return value;

            expr = expr.substring(0, open) + value + expr.substring(close + 1);
        }
        return expr;
    }

    private String calculateFormatted(String expr) {

        int opIndex = findOperator(PRIMARY_OPERATORS, expr);

        if (opIndex != -1) {
            char op = expr.charAt(opIndex);
            String left  = findLeft(expr.substring(0, opIndex));
            String right = findRight(expr.substring(opIndex + 1));
            if (isError(left) || isError(right)) return setError();

            try {
                double val = switch (op) {
                    case '÷' -> MathOperations.divide(left, right);
                    case 'x' -> MathOperations.multiply(left, right);
                    default   -> throw new AssertionError();
                    };
                if (findOperator(PRIMARY_OPERATORS, expr) == -1) {
                    return String.valueOf(val);
                }
                else
                {
                    // update the expression, and re-run the calculation
                    String quoted = Pattern.quote(left + op + right);
                    String nextExpr = expr.replaceFirst(quoted, String.valueOf(val));
                    return calculateFormatted(nextExpr);
                }
            }
            catch (MathOperations.CalcException ex) {
                return setError();
            }
        }
        else
        {
            // check for secondary operators
            opIndex = findOperator(SECONDARY_OPERATORS, expr);

            if (opIndex != -1) {
                char op = expr.charAt(opIndex);
                String left  = findLeft(expr.substring(0, opIndex));
                String right = findRight(expr.substring(opIndex + 1));
                if (isError(left) || isError(right)) return setError();

                try {
                    double val = switch (op) {
                        case '+' -> MathOperations.add(left, right);
                        case '-' -> MathOperations.subtract(left, right);
                        default   -> throw new AssertionError();
                    };
                    if (findOperator(SECONDARY_OPERATORS, expr) == -1) {
                        return String.valueOf(val);
                    }
                    else
                    {
                        // update the expression, and re-run the calculation
                        String quoted = Pattern.quote(left + op + right);
                        String nextExpr = expr.replaceFirst(quoted, String.valueOf(val));
                        return calculateFormatted(nextExpr);
                    }
                }
                catch (MathOperations.CalcException ex) {
                    return setError();
                }
            }
            else
            {
                // contains no primary or secondary operators
                return expr;
            }
        }

    }

    private int findOperator(char[] operators, String expr) {
        for (int i = 0; i < expr.length(); i++) {
            if (charContains(operators, expr.charAt(i)))
            {
                return i;
            }
        }
        return -1;
    }

    private String findLeft(String expr)
    {
        StringBuilder result = new StringBuilder();
        for (int i = expr.length() - 1; i >= 0; i--) {
            if (charContains(OPERATORS, expr.charAt(i))) {
                return result.toString();
            }
            else
            {
                result.insert(0, expr.charAt(i));
            }
        }
        return result.toString();
    }

    private String findRight(String expr)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            if (charContains(OPERATORS, expr.charAt(i))) {
                return result.toString();
            }
            else {
                result.append(expr.charAt(i));
            }
        }
        return result.toString();
    }

    /*--------------------------------------------------------------
     *  Misc helpers
     *--------------------------------------------------------------*/

    private String round(String expr) {
        try {
            double num = Double.parseDouble(expr);
            num = new BigDecimal(num).setScale(4, RoundingMode.HALF_UP).doubleValue();
            return String.valueOf(num);
        } catch (NumberFormatException e) {
            return setError();
        }
    }

    private String setError() {
        resultText.setText("Error");
        clearInput = true;
        superscriptMode = false;
        return "Error";
    }

    private boolean isError(String s) { return "Error".equals(s); }

    private void resetForNextEntry() {
        operationText.setText(resultText.getText());
        resultText.clear();
        clearInput = false;
        superscriptMode = false;
    }

    private void update() {
        // superscript glow
        if (superscriptMode) {
            if (!power.getStyleClass().contains("borderGlow"))
                power.getStyleClass().add("borderGlow");
        } else {
            power.getStyleClass().removeAll("borderGlow");
        }

        // sqrt glow + caret move
        if (sqrtMode) {
            if (!sqrt.getStyleClass().contains("borderGlow"))
                sqrt.getStyleClass().add("borderGlow");
        } else {
            sqrt.getStyleClass().removeAll("borderGlow");
        }
    }


}