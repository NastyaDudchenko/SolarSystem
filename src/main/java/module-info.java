module com.example.solar_system {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.solar_system to javafx.fxml;
    exports com.example.solar_system;
}