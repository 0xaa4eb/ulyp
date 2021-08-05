package com.ulyp.ui;

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

public class UIMain extends Application {

    private ApplicationContext context;

    @Override
    public void start(Stage stage) throws Exception {
        context = new AnnotationConfigApplicationContext(Configuration.class);

        FXMLLoader loader = new FXMLLoader(UIMain.class.getClassLoader().getResource("PrimaryView.fxml"));
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
        scene.getStylesheets().add("modena.css");

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setTitle("ULYP");
        InputStream iconStream = UIMain.class.getClassLoader().getResourceAsStream("icon.png");
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
