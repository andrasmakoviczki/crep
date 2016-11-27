package hu.elte.computernetworks;

import hu.elte.computernetworks.util.Load;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Andras Makoviczki on 2016. 11. 16..
 */
public class AppController implements Initializable{
    @FXML
    public Button addClusterButton;
    @FXML
    public MenuItem newMenuItem;
    private Network network;
    private GraphicsContext gc;

    @FXML
    private MenuItem loadMenuItem;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private Stage stage;
    @FXML
    private Canvas canvas;
    @FXML
    private StatusBar statusBar;
    @FXML
    private Button startButton;

    private Double fieldHeight;
    private Double fieldWidth;

    public void initialize(URL location, ResourceBundle resources) {
        initial(4);
    }

    private void initial(Integer factor) {
        gc = canvas.getGraphicsContext2D();
        drawBase(factor);
        drawCluster(0.0,0.0,fieldWidth,fieldHeight,2,5);
    }

    public AppController() {
        test();
    }

    public void test(){
        this.network = new Network();
        network.testSave();
        //network.testLoad();
    }

    private Integer factorization(Integer num){
        Double root = Math.ceil(Math.sqrt(Integer.valueOf(num).doubleValue()));
        return root.intValue();
    }

    public void resetHandler(ActionEvent actionEvent) {
    }

    public void handleRequest(ActionEvent actionEvent) {
    }

    public void handleCloseButton(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    private void drawBase(Integer factor){
        Integer size = factorization(factor);
        fieldWidth = canvas.getWidth() / new Double(size);
        fieldHeight = canvas.getHeight() / new Double(size);

        for (Integer i = 0; i < new Double(canvas.getWidth()).intValue(); i++){
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.setLineDashes(5, 5);
            gc.strokeLine(0,i * fieldHeight,canvas.getWidth(),i * fieldHeight);
        }

        for (Integer j = 0; j < new Double(canvas.getHeight()).intValue(); j++){
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.setLineDashes(5, 5);
            gc.strokeLine(j * fieldWidth,0,j * fieldWidth,canvas.getHeight());
        }
    }


    public void drawCluster(Double x, Double y,Double fieldWidth,Double fieldHeight, Integer nodeSize, Integer clusterCapacity){
        Double sizeRate = 0.75;
        Double clusterWidth = fieldWidth * sizeRate;
        Double clusterHeight = fieldWidth * sizeRate;

        Double xShift = x + (fieldWidth - clusterWidth) / 2;
        Double yShift = y + (fieldHeight - clusterHeight) / 2;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.setLineDashes(1,1);
        gc.strokeRect(xShift,yShift,clusterWidth,clusterHeight);

        Double nodeShiftX = clusterWidth / clusterCapacity;
        Double nodeShiftY = clusterHeight / clusterCapacity;

        Integer processed = 0;

        for (Integer i = 0; i < clusterCapacity; i++) {
            if(processed < nodeSize) {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / clusterCapacity,true);
                processed = processed + 1;
            } else {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / clusterCapacity);
            }
        }
    }

    public void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight){
        drawNode(x,y,clusterWidth,clusterHeight,false);
    }

    public void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight,Boolean set){
        Double sizeRate = 0.75;
        Double nodeWidth = clusterWidth * sizeRate;
        Double nodeHeight = clusterHeight * sizeRate;

        Double xShift = x + (clusterWidth - nodeWidth) / 2;
        Double yShift = y + (clusterHeight - nodeHeight) / 2;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.setLineDashes(1,1);
        gc.strokeRect(xShift,yShift,clusterWidth,clusterHeight * sizeRate);
        if (set){
            gc.fillRect(xShift,yShift,clusterWidth,clusterHeight * sizeRate);
        }
    }

    public void handleLoadButton(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        FileChooser.ExtensionFilter txtExtension = new FileChooser.ExtensionFilter("JSON", "*.json");
        fileChooser.getExtensionFilters().add(txtExtension);

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Load load = new Load(file.getAbsolutePath());
            try {
                Network network = load.read();
                drawCanvas(network);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //statusBar.setText("Loaded: " + loadFile.getTotalLoaded().toString() + " objects");
        }
    }

    public void drawCanvas(Network network){
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        Integer actClusterSize = network.getClusters().size();

        if(actClusterSize != 0) {
            drawBase(actClusterSize);

            Integer size = factorization(actClusterSize);
            Integer processed = 0;

            for (Integer j = 0; j < size; j++) {
                for (Integer i = 0; i < size; i++) {
                    if (processed < actClusterSize) {
                        Integer nodeSize = network.getClusters().get(processed).getSize();
                        drawCluster(0 + i * fieldWidth, 0 + j * fieldHeight, fieldWidth, fieldHeight, nodeSize, network.getClusterSize());
                        processed = processed + 1;
                    } else {
                        break;
                    }
                }
            }
        }

    }

    public void handleSaveButton(ActionEvent actionEvent) {
    }

    public void handleAddCluster(ActionEvent actionEvent) {
        network.addCluster();
        drawCanvas(network);
    }

    public void handleNewButton(ActionEvent actionEvent) {
        network = new Network();
        drawCanvas(network);
    }
}
