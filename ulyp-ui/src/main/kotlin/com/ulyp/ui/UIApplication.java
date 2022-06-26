package com.ulyp.ui;

import com.ulyp.ui.looknfeel.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;

public class UIApplication extends Application {

    // Guaranteed to be non-null and initialized
    public static Stage stage;

    private AnnotationConfigApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        UIApplication.stage = stage;

        context = new AnnotationConfigApplicationContext(Configuration.class);

        FXMLLoader loader = new FXMLLoader(UIApplication.class.getClassLoader().getResource("PrimaryView.fxml"));
        loader.setControllerFactory(cl -> context.getBean(cl));

        SceneRegistry sceneRegistry = context.getBean(SceneRegistry.class);

        ThemeManager themeManager = context.getBean(ThemeManager.class);
        themeManager.setSceneRegistry(sceneRegistry);

        Parent root = loader.load();

        Scene scene = sceneRegistry.newScene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setTitle("Ulyp");
        InputStream iconStream = UIApplication.class.getClassLoader().getResourceAsStream("icons/ulyp-logo-icon.png");
        if (iconStream == null) {
            throw new RuntimeException("Icon not found");
        }
        stage.getIcons().add(new Image(iconStream));

        stage.show();
    }
}
