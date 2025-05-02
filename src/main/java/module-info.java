module hm.app.calculatorapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires commons.math3;


    opens hm.app.calculatorapp to javafx.fxml;
    exports hm.app.calculatorapp;
}