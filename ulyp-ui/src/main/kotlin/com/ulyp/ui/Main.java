package com.ulyp.ui;

import com.ulyp.ui.looknfeel.Theme;
import com.ulyp.ui.looknfeel.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;

public class Main extends Application {

    private ApplicationContext context;

    @Override
    public void start(Stage stage) throws Exception {
        context = new AnnotationConfigApplicationContext(Configuration.class);

        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("PrimaryView.fxml"));
        loader.setControllerFactory(cl -> {
            Object bean = context.getBean(cl);
            System.out.println(cl + " -> " + bean);
            return bean;
        });

        Parent root = loader.load();

        PrimaryViewController viewController = loader.getController();

        FileChooser fileChooser = new FileChooser();

        viewController.fileChooser = () -> fileChooser.showOpenDialog(stage);

        Scene scene = new Scene(root);
        context.getBean(ThemeManager.class).applyTheme(Theme.DARK, scene);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setTitle("ULYP");
        InputStream iconStream = Main.class.getClassLoader().getResourceAsStream("icon.png");
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
