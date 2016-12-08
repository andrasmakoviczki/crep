package hu.elte.computernetworks;

import hu.elte.computernetworks.model.Network;
import hu.elte.computernetworks.model.Node;
import hu.elte.computernetworks.model.Request;
import hu.elte.computernetworks.util.Load;
import hu.elte.computernetworks.util.Save;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andras Makoviczki on 2016. 11. 16..
 */
public class AppController implements Initializable {
    @FXML
    private Button stopButton;
    //region FXML objects
    @FXML
    private Button addClusterButton;
    @FXML
    private Button autoCrep;
    @FXML
    private CheckBox showRequestsBox;
    @FXML
    private Spinner migrationCostSpinner;
    @FXML
    private MenuItem newMenuItem;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button addRequestsButton;
    @FXML
    private ContextMenu contextMenu;
    @FXML
    private MenuItem addNodeMenuItem;
    @FXML
    private MenuItem removeNodeMenuItem;
    @FXML
    private MenuItem removeClusterMenuItem;
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
    @FXML
    private Spinner clusterSizeSpinner;
    private ScheduledExecutorService scheduledExecutorService;
    //endregion

    //region fields
    private Double fieldHeight;
    private Double fieldWidth;

    private GraphicsContext gc;
    private Network network;
    //endregion

    //region calculate
    private Boolean isCluster(Double x, Double y) {
        Integer factor = factorization(network.getClusters().size());

        Integer processed = 0;
        for (Integer j = 0; j < factor; j++) {
            for (Integer i = 0; i < factor; i++) {
                if (processed < network.getClusters().size()) {
                    Double minX = i * fieldWidth;
                    Double maxX = minX + fieldWidth;
                    Double minY = j * fieldHeight;
                    Double maxY = minY + fieldHeight;
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        return true;
                    }

                    processed = processed + 1;
                } else {
                    break;
                }
            }
        }

        return false;
    }

    private Integer findCluster(Double x, Double y) {
        Integer factor = factorization(network.getClusters().size());
        Integer processed = 0;

        for (Integer j = 0; j < factor; j++) {
            for (Integer i = 0; i < factor; i++) {
                if (processed < network.getClusters().size()) {
                    Double minX = i * fieldWidth;
                    Double maxX = minX + fieldWidth;
                    Double minY = j * fieldHeight;
                    Double maxY = minY + fieldHeight;
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                        return i + j * factor;
                        //return network.getClusters().get(i * factor + j * factor);
                    }

                    processed = processed + 1;
                } else {
                    break;
                }
            }
        }

        return null;
    }

    private Integer factorization(Integer num) {
        Double root = Math.ceil(Math.sqrt(Integer.valueOf(num).doubleValue()));
        return root.intValue();
    }
    //endregion

    //region initialize
    public void initialize(URL location, ResourceBundle resources) {
        initial(1);
    }

    private void initial(Integer factor) {
        gc = canvas.getGraphicsContext2D();
        drawBase(factor);
        setContextMenu();
        setSpinner();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }


    public AppController() {
        this.network = new Network();
        test();
    }

    private void setSpinner(){
        SpinnerValueFactory spinnerFactoryClusterSize = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,network.getClusterSize());
        clusterSizeSpinner.setValueFactory(spinnerFactoryClusterSize);
        clusterSizeSpinner.setEditable(true);
        clusterSizeSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                network.setClusterSize(newValue);
                drawCanvas(network);
            }
        });

        SpinnerValueFactory spinnerFactoryMigrationCost = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,network.getMigrationCost());
        migrationCostSpinner.setValueFactory(spinnerFactoryMigrationCost);
        migrationCostSpinner.setEditable(true);
        migrationCostSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                network.setMigrationCost(newValue);
            }
        });
    }

    private void setContextMenu() {
        this.contextMenu = new ContextMenu();
        this.addNodeMenuItem = new MenuItem("Add node");
        this.removeNodeMenuItem = new MenuItem("Remove node");
        this.removeClusterMenuItem = new MenuItem("Remove cluster");

        contextMenu.getItems().add(addNodeMenuItem);
        contextMenu.getItems().add(removeNodeMenuItem);
        contextMenu.getItems().add(removeClusterMenuItem);

        //ContextMenu
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    //Canvas pozíciója a scene-en
                    Bounds boundsCanvas = canvas.getBoundsInLocal();
                    Bounds screenBoundsCanvas = canvas.localToScene(boundsCanvas);
                    final Double canvasX = screenBoundsCanvas.getMinX();
                    final Double canvasY = screenBoundsCanvas.getMinY();

                    //ContextMenu pozíciója a scene-en
                    Double x = event.getSceneX() - canvasX;
                    Double y = event.getSceneY() - canvasY;

                    if (isCluster(x, y)) {
                        contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
                    }
                }
            }
        });

        //Contex Menu eltüntetése
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    contextMenu.hide();
                }
            }
        });

        //Node hozzáadása
        addNodeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                //Pane pozíciója a screen-en
                Bounds boundsPane = borderPane.getBoundsInLocal();
                Bounds screenBoundsPane = borderPane.localToScreen(boundsPane);
                Double paneX = screenBoundsPane.getMinX();
                Double paneY = screenBoundsPane.getMinY();

                //Canvas pozíciója a scene-en
                Bounds boundsCanvas = canvas.getBoundsInLocal();
                Bounds screenBoundsCanvas = canvas.localToScene(boundsCanvas);
                final Double canvasX = screenBoundsCanvas.getMinX();
                final Double canvasY = screenBoundsCanvas.getMinY();

                //ContextMenu pozíciója a scene-en
                MenuItem thisItem = (MenuItem) event.getSource();
                ContextMenu parentMenu = (ContextMenu) thisItem.getParentPopup();
                Double x = parentMenu.getAnchorX() - paneX - canvasX;
                Double y = parentMenu.getAnchorY() - paneY - canvasY;

                try {
                    network.addNode(findCluster(x, y));
                    drawCanvas(network);
                } catch (Exception e) {
                    errorDialog("No more place for a new node!");
                }
            }
        });

        //Node eltávolítása
        removeNodeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                //Pane pozíciója a screen-en
                Bounds boundsPane = borderPane.getBoundsInLocal();
                Bounds screenBoundsPane = borderPane.localToScreen(boundsPane);
                Double paneX = screenBoundsPane.getMinX();
                Double paneY = screenBoundsPane.getMinY();

                //Canvas pozíciója a scene-en
                Bounds boundsCanvas = canvas.getBoundsInLocal();
                Bounds screenBoundsCanvas = canvas.localToScene(boundsCanvas);
                final Double canvasX = screenBoundsCanvas.getMinX();
                final Double canvasY = screenBoundsCanvas.getMinY();

                //ContextMenu pozíciója a scene-en
                MenuItem thisItem = (MenuItem) event.getSource();
                ContextMenu parentMenu = (ContextMenu) thisItem.getParentPopup();
                Double x = parentMenu.getAnchorX() - paneX - canvasX;
                Double y = parentMenu.getAnchorY() - paneY - canvasY;

                try {
                    network.removeNode(findCluster(x, y));
                    drawCanvas(network);
                } catch (Exception e) {
                    errorDialog("The cluster is empty!");
                }
            }
        });

        //Cluster eltávolítása
        removeClusterMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                //Pane pozíciója a screen-en
                Bounds boundsPane = borderPane.getBoundsInLocal();
                Bounds screenBoundsPane = borderPane.localToScreen(boundsPane);
                Double paneX = screenBoundsPane.getMinX();
                Double paneY = screenBoundsPane.getMinY();

                //Canvas pozíciója a scene-en
                Bounds boundsCanvas = canvas.getBoundsInLocal();
                Bounds screenBoundsCanvas = canvas.localToScene(boundsCanvas);
                final Double canvasX = screenBoundsCanvas.getMinX();
                final Double canvasY = screenBoundsCanvas.getMinY();

                //ContextMenu pozíciója a scene-en
                MenuItem thisItem = (MenuItem) event.getSource();
                ContextMenu parentMenu = (ContextMenu) thisItem.getParentPopup();
                Double x = parentMenu.getAnchorX() - paneX - canvasX;
                Double y = parentMenu.getAnchorY() - paneY - canvasY;

                try {
                    network.removeCluster(findCluster(x, y));
                    drawCanvas(network);
                } catch (Exception e) {
                    errorDialog("No more cluster!");
                }
            }
        });
    }

    //endregion

    //region test
    public void test() {
        this.network = new Network();
        //network.testStress();
        network.testSave();
        network.testLoad();
    }

    public void errorDialog(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.showAndWait();
    }
    //endregion

    //region draw
    private void drawBase(Integer factor) {
        Integer size = factorization(factor);
        fieldWidth = canvas.getWidth() / new Double(size);
        fieldHeight = canvas.getHeight() / new Double(size);

        for (Integer i = 0; i < new Double(canvas.getWidth()).intValue(); i++) {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.setLineDashes(5, 5);
            gc.strokeLine(0, i * fieldHeight, canvas.getWidth(), i * fieldHeight);
        }

        for (Integer j = 0; j < new Double(canvas.getHeight()).intValue(); j++) {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(0.5);
            gc.setLineDashes(5, 5);
            gc.strokeLine(j * fieldWidth, 0, j * fieldWidth, canvas.getHeight());
        }
    }

    public void drawCanvas(Network network){
        drawCanvas(network,false);
    }
    
    public void drawCanvas(Network network,Boolean showRequests) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        Integer actClusterSize = network.getClusters().size();

        if (actClusterSize != 0) {
            drawBase(actClusterSize);

            Integer size = factorization(actClusterSize);
            Integer processed = 0;

            for (Integer j = 0; j < size; j++) {
                for (Integer i = 0; i < size; i++) {
                    if (processed < actClusterSize) {
                        drawCluster(0 + i * fieldWidth, 0 + j * fieldHeight, fieldWidth, fieldHeight,processed,network.getClusters().get(j*size + i).getSize());
                        processed = processed + 1;
                    } else {
                        break;
                    }
                }
            }
        }
        
        if(showRequests){
            List<RequestItem> rList = new ArrayList<>();
            for (Request request: network.getRequests()) {
                List<Node> nList = new ArrayList<>();
                network.getClusters().stream()
                        .forEach(cluster -> cluster.getNodes()
                                .forEach(node -> nList.add(node)));
                
                Integer iterSrc = 0;
                Boolean lSrc = false;
                for (; !lSrc && iterSrc < nList.size(); iterSrc++) {
                    if(nList.get(iterSrc).getId().equals(request.getSrc())){
                        lSrc = true;
                    }
                }
                if(!lSrc){
                    iterSrc = -1;
                }

                Integer iterDst = 0;
                Boolean lDst = false;
                for (; !lDst && iterDst < nList.size(); iterDst++) {
                    if(nList.get(iterDst).getId().equals(request.getDst())){
                        lDst = true;
                    }
                }
                if(!lDst){
                    iterDst = -1;
                }
                
                Integer iterCSrc = 0;
                Boolean lCSrc = false;
                for(;!lCSrc && iterCSrc < network.getClusters().size();iterCSrc++){
                    if(iterSrc > network.getClusters().get(iterCSrc).getNodes().size()){
                        iterSrc = iterSrc - network.getClusters().get(iterCSrc).getNodes().size();
                    } else {
                        lCSrc = true;
                    }
                }
                if(!lCSrc){
                    iterCSrc = -1;
                }

                Integer iterCDst = 0;
                Boolean lCDst = false;
                for(;!lCDst && iterCDst < network.getClusters().size();iterCDst++){
                    if(iterDst > network.getClusters().get(iterCDst).getNodes().size()){
                        iterDst = iterDst - network.getClusters().get(iterCDst).getNodes().size();
                    } else {
                        lCDst = true;
                    }
                }
                if(!lCDst){
                    iterCDst = -1;
                }
                rList.add(new RequestItem(iterSrc-1,iterDst-1,iterCSrc-1,iterCDst-1));
            }

            Integer size = factorization(actClusterSize);

            Double sizeRateCluster = 0.60;
            Double clusterWidth = fieldWidth * sizeRateCluster;
            Double clusterHeight = fieldWidth * sizeRateCluster;

            Double sizeRateNode = 0.8;
            Double nodeWidth = clusterWidth * sizeRateNode;
            Double nodeHeight = clusterHeight * sizeRateNode;


            for (RequestItem rItem: rList) {
                Double addSrcClusterX = new Double(rItem.getClusterSrc() % size);
                Double addSrcClusterY = new Double(rItem.getClusterSrc() / size);
                Double addDstClusterX = new Double(rItem.getClusterDst() % size);
                Double addDstClusterY = new Double(rItem.getClusterDst() / size);

                Double addSrcNodeX = new Double(rItem.getNodeSrc() % size);
                Double addSrcNodeY = new Double(rItem.getNodeSrc() / size);
                Double addDstNodeX = new Double(rItem.getNodeDst() % size);
                Double addDstNodeY = new Double(rItem.getNodeDst() / size);

                Double nodeSize = nodeHeight / network.getClusterSize();
                
                            //mezők eltolása
                double x1 = addSrcClusterX * fieldWidth +
                        //eltolás a canvas szélétől
                        ((fieldWidth - clusterWidth) / 2)
                        //+ addSrcNodeX * nodeWidth
                        //eltolás a klaszter szélétől
                        + ((clusterWidth - nodeWidth) / 2);
                double y1 = addSrcClusterY * fieldHeight +
                        ((fieldHeight - clusterHeight) / 2)
                        + rItem.getNodeSrc() * nodeSize
                        + ((clusterHeight - nodeHeight) / 2);
                double x2 = addDstClusterX * fieldWidth
                        + ((fieldWidth - clusterWidth) / 2)
                        //+ addDstNodeX * nodeWidth
                       + ((clusterWidth - nodeWidth) / 2);
                double y2 = addDstClusterY * fieldHeight
                        + ((fieldHeight - clusterHeight) / 2)
                        + rItem.getNodeDst() * nodeSize
                        + ((clusterHeight - nodeHeight) / 2);

                drawArrow(x1,y1,x2,y2);
            }
        }

    }

    public void handleAuto(ActionEvent actionEvent) {
        autoCrep();
    }

    public void autoCrep() {
        Platform.runLater(() -> {
                scheduledExecutorService.schedule(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          network.generateRequests();
                                                          network.CREP();
                                                          drawCanvas(network,showRequestsBox.isSelected());
                                                          statusBar.setText("AutoCrep started!");
                                                      }
                                                  },
                        2,
                        TimeUnit.SECONDS);
    });
    }

    public void handleStopbutton(ActionEvent actionEvent) {
        scheduledExecutorService.shutdown();
        statusBar.setText("AutoCrep stopped!");
    }


    private class RequestItem{
        Integer nodeSrc;
        Integer nodeDst;
        Integer clusterSrc;
        Integer clusterDst;

        public RequestItem(Integer nodeSrc, Integer nodeDst, Integer clusterSrc, Integer clusterDst) {
            this.nodeSrc = nodeSrc;
            this.nodeDst = nodeDst;
            this.clusterSrc = clusterSrc;
            this.clusterDst = clusterDst;
        }

        public Integer getNodeSrc() {
            return nodeSrc;
        }

        public Integer getNodeDst() {
            return nodeDst;
        }

        public Integer getClusterSrc() {
            return clusterSrc;
        }

        public Integer getClusterDst() {
            return clusterDst;
        }
    }

    public void drawCluster(Double x, Double y, Double fieldWidth, Double fieldHeight, Integer cId, Integer nodeSize) {

        Double sizeRate = 0.60;
        Double clusterWidth = fieldWidth * sizeRate;
        Double clusterHeight = fieldWidth * sizeRate;

        Double xShift = x + (fieldWidth - clusterWidth) / 2;
        Double yShift = y + (fieldHeight - clusterHeight) / 2;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        gc.setLineDashes(1, 1);
        gc.strokeRect(xShift, yShift, clusterWidth, clusterHeight);

        gc.fillText("clusterId: " + cId.toString(),x + fieldWidth * 0.05,y + fieldHeight * 0.95);

        Double nodeShiftX = clusterWidth / network.getClusterSize();
        Double nodeShiftY = clusterHeight / network.getClusterSize();

        Integer processed = 0;

        for (Integer i = 0; i < network.getClusterSize(); i++) {
            if (processed < nodeSize) {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / network.getClusterSize(),processed,true);
                Node actNode = network.getClusters().get(cId).getNodes().get(i);
                /*if (network.getGraph() != null && network.getGraph().findComponent(actNode.getId()) != null){

                    //Komponens kirajzolás
                    drawComponents(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / network.getClusterSize(),
                            network.getGraph().findComponent(actNode.getId()).getId().toString());
                }*/
                processed = processed + 1;
            } else {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / network.getClusterSize());
            }
        }
    }

    public void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight) {
        drawNode(x, y, clusterWidth, clusterHeight,null,false);
    }

    public void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight, Integer nodeId, Boolean set) {
        Double sizeRate = 0.8;
        Double nodeWidth = clusterWidth * sizeRate;
        Double nodeHeight = clusterHeight * sizeRate;

        Double xShift = x + (clusterWidth - nodeWidth) / 2;
        Double yShift = y + (clusterHeight - nodeHeight) / 2;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.setLineDashes(1, 1);
        gc.strokeRect(xShift, yShift, clusterWidth, clusterHeight * sizeRate);
        if (set) {
            gc.setGlobalAlpha(0.3);
            gc.fillRect(xShift, yShift, clusterWidth, clusterHeight * sizeRate);
            gc.setGlobalAlpha(1);
            gc.fillText("nId: " + nodeId.toString(),xShift + 10, yShift + (clusterHeight / 2));
        }
    }

    void drawComponents(Double x, Double y, Double clusterWidth, Double clusterHeight,String componentId){
        Double sizeRate = 0.8;
        Double nodeWidth = clusterWidth * sizeRate;
        Double nodeHeight = clusterHeight * sizeRate;

        Double xShift = x + (clusterWidth - nodeWidth) / 2;
        Double yShift = y + (clusterHeight - nodeHeight) / 2;

        gc.setStroke(Color.RED);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(2);
        gc.setLineDashes(1, 1);
        gc.strokeRect(xShift, yShift, clusterWidth, clusterHeight * sizeRate);
        gc.fillText("cId: " + componentId,xShift + (clusterWidth - 10), yShift + (clusterHeight / 2));
    }

    void drawArrow(double x1, double y1, double x2, double y2) {
        Integer arrowSize = 8;
        Double dx = new Double(x2 - x1);
        Double dy = new Double(y2 - y1);
        double angle = Math.atan2(dy, dx);
        Integer len = new Double(Math.sqrt(dx * dx + dy * dy)).intValue();

        Transform transform = Transform.translate(x1, y1);
        transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
        gc.setTransform(new Affine(transform));
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        gc.strokeLine(0, 0, len, 0);
        gc.fillPolygon(new double[]{len, len - arrowSize, len - arrowSize, len}, new double[]{0, -arrowSize, arrowSize, 0},
                4);
        gc.setTransform(new Affine());
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
    }
    //endregion

    //region handler
    public void runCrepHandler(ActionEvent actionEvent) {
        network.CREP();
        drawCanvas(network,showRequestsBox.isSelected());
    }

    public void handleCloseButton(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
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
                network = load.read();
                drawCanvas(network);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //statusBar.setText("Loaded: " + loadFile.getTotalLoaded().toString() + " objects");
        }
    }

    public void handleSaveButton(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        FileChooser.ExtensionFilter txtExtension = new FileChooser.ExtensionFilter("JSON", "*.json");
        fileChooser.getExtensionFilters().add(txtExtension);

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Save save = new Save(file.getAbsolutePath());
            try {
                save.write(network);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //statusBar.setText("Loaded: " + loadFile.getTotalLoaded().toString() + " objects");
        }
    }

    public void handleAddCluster(ActionEvent actionEvent) {
        network.addCluster();
        drawCanvas(network);
    }

    public void handleNewButton(ActionEvent actionEvent) {
        network = new Network();
        drawCanvas(network);
    }

    public void handleRequests(ActionEvent actionEvent) {
        network.generateRequests();
        statusBar.setText("New requests: " + network.getRequests().size());
        drawCanvas(network,showRequestsBox.isSelected());
    }

    public void handleRequstsBox(ActionEvent actionEvent) {
        drawCanvas(network,showRequestsBox.isSelected());
    }
    //endregion
}
