package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class StoreApp extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        showLogin();
    }

    public static void showLogin() throws Exception {
        URL fxml = StoreApp.class.getResource("/org/example/login.fxml");
        URL css = StoreApp.class.getResource("/org/example/style.css");
        if (fxml == null) throw new IllegalStateException("Missing resource: /org/example/login.fxml");
        if (css == null) throw new IllegalStateException("Missing resource: /org/example/style.css");
        Parent root = new FXMLLoader(fxml).load();
        Scene scene = new Scene(root, 960, 680);
        scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("Online Bookstore | Sign In");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        stage.show();
    }

    public static void showStore(Customer customer) throws Exception {
        if (customer == null) throw new IllegalArgumentException("A signed-in customer is required.");
        URL fxml = StoreApp.class.getResource("/org/example/store.fxml");
        URL css = StoreApp.class.getResource("/org/example/style.css");
        if (fxml == null) throw new IllegalStateException("Missing resource: /org/example/store.fxml");
        if (css == null) throw new IllegalStateException("Missing resource: /org/example/style.css");
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        StoreController controller = loader.getController();
        controller.setCustomer(customer);
        Scene scene = new Scene(root, 1320, 820);
        scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("Online Bookstore");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1080);
        stage.setMinHeight(720);
        stage.show();
    }
}
