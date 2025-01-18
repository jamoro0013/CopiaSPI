module com.spaceinvaders.spaceinvaders {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.spaceinvaders.spaceinvaders to javafx.fxml;
    exports com.spaceinvaders.spaceinvaders;
}