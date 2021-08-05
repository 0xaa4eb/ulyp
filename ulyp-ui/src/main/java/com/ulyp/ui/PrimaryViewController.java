package com.ulyp.ui;

import com.ulyp.transport.TCallRecordLogUploadRequest;
import com.ulyp.ui.code.SourceCodeView;
import com.ulyp.ui.font.FontSizeChanger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PrimaryViewController implements Initializable {

    public PrimaryViewController() {
    }

    @FXML
    public VBox primaryPane;
    @FXML
    public AnchorPane processTabAnchorPane;
    @FXML
    public AnchorPane sourceCodeViewAnchorPane;
    @Autowired
    public SourceCodeView sourceCodeView;
    @Autowired
    public ProcessTabPane processTabPane;
    @Autowired
    public FontSizeChanger fontSizeChanger;

    private AggregationStrategy aggregationStrategy = new ByRecordingIdAggregationStrategy();

    private final ExecutorService uploaderExecutorService = Executors.newFixedThreadPool(1);
    Supplier<File> fileChooser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.processTabAnchorPane.getChildren().add(processTabPane);

        AnchorPane.setTopAnchor(processTabPane, 0.0);
        AnchorPane.setBottomAnchor(processTabPane, 0.0);
        AnchorPane.setRightAnchor(processTabPane, 0.0);
        AnchorPane.setLeftAnchor(processTabPane, 0.0);

        this.sourceCodeViewAnchorPane.getChildren().add(sourceCodeView);

        AnchorPane.setTopAnchor(sourceCodeView, 0.0);
        AnchorPane.setBottomAnchor(sourceCodeView, 0.0);
        AnchorPane.setRightAnchor(sourceCodeView, 0.0);
        AnchorPane.setLeftAnchor(sourceCodeView, 0.0);
    }

    public void clearAll(Event event) {
        processTabPane.clear();
    }

    public void changeAggregation(Event event) {
        aggregationStrategy = new ByThreadIdAggregationStrategy();
        // TODO maybe popup
    }

    public void openRecordedDump(ActionEvent actionEvent) {
        // Without those calls font style won't be applied until user changes font for the first time
        fontSizeChanger.upscale(primaryPane.getScene());
        fontSizeChanger.downscale(primaryPane.getScene());

        final File file = fileChooser.get();

        uploaderExecutorService.submit(
                () -> {
                    if (file != null) {
                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {

                            while (inputStream.available() > 0) {
                                TCallRecordLogUploadRequest request = TCallRecordLogUploadRequest.parseDelimitedFrom(inputStream);
                                CallRecordTreeChunk chunk = new CallRecordTreeChunk(request);
                                FileRecordingsTab fileRecordingsTab = processTabPane.getOrCreateProcessTab(new FileRecordingsTabName(file, chunk.getProcessInfo()));
                                CallRecordTreeTab recordingTab = fileRecordingsTab.getOrCreateRecordingTab(aggregationStrategy, chunk);
                                recordingTab.uploadChunk(chunk);
                                Platform.runLater(recordingTab::refreshTreeView);
                            }

                        } catch (Exception e) {
                            // TODO show error dialog
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
