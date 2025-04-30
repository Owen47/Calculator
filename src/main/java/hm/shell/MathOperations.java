package hm.shell;


public class MathOperations {



    /** Thrown when a calculation error occurs*/
    public static class CalcException extends Exception {
        public CalcException(String msg) { super(msg); }
    }


    // Math Operations used in calculator

    public static double divide(String a, String b) throws CalcException {
        double x = format(a), y = format(b);
        if (y == 0) throw new CalcException("division by zero");
        return x / y;
    }

    public static double multiply(String a, String b) throws CalcException {
        return format(a) * format(b);
    }

    public static double add(String a, String b) throws CalcException {
        return format(a) + format(b);
    }

    public static double subtract(String a, String b) throws CalcException {
        return format(a) - format(b);
    }

    public static double power(String a, String b) throws CalcException {
        return Math.pow(format(a), format(b));
    }

    public static double sqrt(String a) throws CalcException {
        return Math.sqrt(format(a));
    }

    public static double nlog(String a) throws CalcException {
        return Math.log(format(a));
    }

    public static double factorial(String a) throws CalcException {
            int num = Integer.parseInt(a);
            for (int i = 1; i < num; i++) {
                num *= i;
            }
            return num;
    }


    // converts strings into doubles

    private static double format(String s) throws CalcException {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            throw new CalcException("bad number: " + s);
        }
    }

}
