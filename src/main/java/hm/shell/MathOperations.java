package hm.shell;

import hm.app.calculatorapp.CalculatorController;

import static hm.app.calculatorapp.CalculatorController.setError;

public class MathOperations {

    /**
     * Preforms division on two numbers
     * @param numerator the numerator
     * @param denominator the denominator
     * @return the quotient
     */
    public static String divide(String numerator, String denominator) {
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
    public static String multiply(String a, String b) {
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
    public static String add(String a, String b) {
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
    public static String subtract(String a, String b) {
        try {
            return String.valueOf(Double.parseDouble(a) - Double.parseDouble(b));
        }
        catch (NumberFormatException e) {
            return setError();
        }
    }
}
