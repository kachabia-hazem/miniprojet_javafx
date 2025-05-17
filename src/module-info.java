module mini_projet {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;
	requires javafx.graphics;

    opens application to javafx.graphics, javafx.fxml;
    opens application.controllers to javafx.fxml;

    exports application;  // Si besoin
    exports application.controllers;  // Si besoin
    exports application.models;  // Si besoin
}
