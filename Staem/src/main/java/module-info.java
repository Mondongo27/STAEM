module com.mycompany {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany to javafx.fxml;
    exports com.mycompany;
}