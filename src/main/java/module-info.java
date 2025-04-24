module hm.app.calculatorapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens hm.app.calculatorapp to javafx.fxml;
    exports hm.app.calculatorapp;
}