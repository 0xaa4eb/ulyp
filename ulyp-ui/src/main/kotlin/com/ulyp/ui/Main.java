package com.ulyp.ui;

import com.ulyp.ui.config.Configuration;
import com.ulyp.ui.looknfeel.Theme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;

public class Main extends Application {

    // Guaranteed to be non-null and initialized
    public static Stage stage;

    private AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;

        context = new AnnotationConfigApplicationContext(Configuration.class);

        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("PrimaryView.fxml"));
        loader.setControllerFactory(cl -> context.getBean(cl));

        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Theme.DARK.getUlypCssPath());

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setTitle("ULYP");
        InputStream iconStream = Main.class.getClassLoader().getResourceAsStream("icons/ulyp-logo-icon.png");
        if (iconStream == null) {
            throw new RuntimeException("Icon not found");
        }
        stage.getIcons().add(new Image(iconStream));

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
