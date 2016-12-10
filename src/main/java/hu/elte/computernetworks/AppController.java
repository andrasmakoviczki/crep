package hu.elte.computernetworks;

import hu.elte.computernetworks.model.Network;
import hu.elte.computernetworks.model.Node;
import hu.elte.computernetworks.model.Request;
import hu.elte.computernetworks.util.Load;
import hu.elte.computernetworks.util.Save;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import static java.lang.Integer.MAX_VALUE;

/**
 * Created by Andras Makoviczki on 2016. 11. 16..
 */
public class AppController implements Initializable {
    private final IntegerProperty clusterSize;
    private final IntegerProperty migrationCost;
    //region FXML objects
    @FXML
    private BorderPane borderPane;
    @FXML
    private CheckBox showRequestsBox;
    @FXML
    private Spinner<Integer> migrationCostSpinner;
    @FXML
    private Spinner<Integer> clusterSizeSpinner;
    @FXML
    private ContextMenu contextMenu;
    @FXML
    private Stage stage;
    //endregion
    @FXML
    private Canvas canvas;
    @FXML
    private StatusBar statusBar;
    //region fields
    private GraphicsContext gc;
    private Network network;
    private Double fieldHeight;
    private Double fieldWidth;
    //endregion

    public AppController() {
        this.network = new Network();
        this.clusterSize = new SimpleIntegerProperty(network.getClusterSize());
        this.migrationCost = new SimpleIntegerProperty(network.getMigrationCost());
    }

    //region constructor
    public void initialize(URL location, ResourceBundle resources) {
        initial();
    }

    private void initial() {
        gc = canvas.getGraphicsContext2D();
        drawBase(1);
        setContextMenu();
        setSpinner();
    }

    private void setSpinner() {
        SpinnerValueFactory<Integer> spinnerFactoryClusterSize = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_VALUE, network.getClusterSize());
        clusterSizeSpinner.setValueFactory(spinnerFactoryClusterSize);
        clusterSizeSpinner.setEditable(true);
        clusterSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            network.setClusterSize(Integer.parseInt(String.valueOf(newValue)));
            drawCanvas(network);
        });
        clusterSize.addListener((observable, oldValue, newValue) -> spinnerFactoryClusterSize.setValue(Integer.parseInt(String.valueOf(newValue))));

        SpinnerValueFactory<Integer> spinnerFactoryMigrationCost = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_VALUE, network.getMigrationCost());
        migrationCostSpinner.setValueFactory(spinnerFactoryMigrationCost);
        migrationCostSpinner.setEditable(true);
        migrationCostSpinner.valueProperty().addListener((observable, oldValue, newValue) -> network.setMigrationCost(Integer.parseInt(String.valueOf(newValue))));
        migrationCost.addListener((observable, oldValue, newValue) -> spinnerFactoryMigrationCost.setValue(Integer.parseInt(String.valueOf(newValue))));
    }

    private void setContextMenu() {
        this.contextMenu = new ContextMenu();
        MenuItem addNodeMenuItem = new MenuItem("Add node");
        MenuItem removeNodeMenuItem = new MenuItem("Remove node");
        MenuItem removeClusterMenuItem = new MenuItem("Remove cluster");

        contextMenu.getItems().add(addNodeMenuItem);
        contextMenu.getItems().add(removeNodeMenuItem);
        contextMenu.getItems().add(removeClusterMenuItem);

        //ContextMenu
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
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
        });

        //Contex Menu eltüntetése
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });

        //Node hozzáadása
        addNodeMenuItem.setOnAction(event -> {
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
            ContextMenu parentMenu = thisItem.getParentPopup();
            Double x = parentMenu.getAnchorX() - paneX - canvasX;
            Double y = parentMenu.getAnchorY() - paneY - canvasY;

            try {
                network.addNode(findCluster(x, y));
                drawCanvas(network);
                statusBar.setText("Added new node");
            } catch (IndexOutOfBoundsException e) {
                errorDialog("No more place for a new node!");
            } catch (IllegalStateException e) {
                errorDialog("Invariant violation! The spare space have to be more than half of any cluster size!");
            }
        });

        //Node eltávolítása
        removeNodeMenuItem.setOnAction(event -> {
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
            ContextMenu parentMenu = thisItem.getParentPopup();
            Double x = parentMenu.getAnchorX() - paneX - canvasX;
            Double y = parentMenu.getAnchorY() - paneY - canvasY;

            try {
                network.removeNode(findCluster(x, y));
                drawCanvas(network);
                statusBar.setText("Removed node");
            } catch (Exception e) {
                errorDialog("The cluster is empty!");
            }
        });

        //Cluster eltávolítása
        removeClusterMenuItem.setOnAction(event -> {
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
            ContextMenu parentMenu = thisItem.getParentPopup();
            Double x = parentMenu.getAnchorX() - paneX - canvasX;
            Double y = parentMenu.getAnchorY() - paneY - canvasY;

            try {
                network.removeCluster(findCluster(x, y));
                drawCanvas(network);
                statusBar.setText("Removed cluster");
            } catch (IndexOutOfBoundsException e) {
                errorDialog("No more cluster!");
            } catch (IllegalStateException e) {
                errorDialog("Invariant violation! The spare space have to be more than half of any cluster size!");
            }
        });
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

    private void drawCanvas(Network network) {
        drawCanvas(network, false);
    }

    private void drawCanvas(Network network, Boolean showRequests) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        Integer actClusterSize = network.getClusters().size();

        if (actClusterSize != 0) {
            drawBase(actClusterSize);

            Integer size = factorization(actClusterSize);
            Integer processed = 0;

            for (Integer j = 0; j < size; j++) {
                for (Integer i = 0; i < size; i++) {
                    if (processed < actClusterSize) {
                        drawCluster(0 + i * fieldWidth, 0 + j * fieldHeight, fieldWidth, fieldHeight, processed + 1, network.getClusters().get(j * size + i).getSize());
                        processed = processed + 1;
                    } else {
                        break;
                    }
                }
            }
        }

        if (showRequests) {
            List<RequestItem> rList = collectRequestItems();
            drawRequests(rList, actClusterSize);
        }
    }

    private void drawCluster(Double x, Double y, Double fieldWidth, Double fieldHeight, Integer cId, Integer nodeSize) {
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

        gc.fillText("clusterId: " + cId.toString(), x + fieldWidth * 0.05, y + fieldHeight * 0.95);

        Double nodeShiftY = clusterHeight / network.getClusterSize();

        Integer processed = 0;

        for (Integer i = 0; i < network.getClusterSize(); i++) {
            if (processed < nodeSize) {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / network.getClusterSize(), processed + 1, true);
                processed = processed + 1;
            } else {
                drawNode(xShift, yShift + i * nodeShiftY, clusterWidth * sizeRate, clusterHeight / network.getClusterSize());
            }
        }
    }

    private void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight) {
        drawNode(x, y, clusterWidth, clusterHeight, null, false);
    }

    private void drawNode(Double x, Double y, Double clusterWidth, Double clusterHeight, Integer nodeId, Boolean set) {
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
            gc.fillText("nId: " + nodeId.toString(), xShift + 10, yShift + (clusterHeight / 2));
        }
    }

    private void drawRequests(List<RequestItem> rList, Integer clusterSize) {
        Integer size = factorization(clusterSize);

        Double sizeRateCluster = 0.60;
        Double clusterWidth = fieldWidth * sizeRateCluster;
        Double clusterHeight = fieldWidth * sizeRateCluster;

        Double sizeRateNode = 0.8;
        Double nodeWidth = clusterWidth * sizeRateNode;
        Double nodeHeight = clusterHeight * sizeRateNode;

        for (RequestItem rItem : rList) {
            Double addSrcClusterX = (double) (rItem.getClusterSrc() % size);
            Double addSrcClusterY = (double) (rItem.getClusterSrc() / size);
            Double addDstClusterX = (double) (rItem.getClusterDst() % size);
            Double addDstClusterY = (double) (rItem.getClusterDst() / size);

            Double nodeSize = nodeHeight / network.getClusterSize();

            //mezők eltolása
            double x1 = addSrcClusterX * fieldWidth +
                    //eltolás a canvas szélétől
                    ((fieldWidth - clusterWidth) / 2)
                    //eltolás a klaszter szélétől
                    + ((clusterWidth - nodeWidth) / 2);
            double y1 = addSrcClusterY * fieldHeight +
                    ((fieldHeight - clusterHeight) / 2)
                    + rItem.getNodeSrc() * nodeSize
                    + ((clusterHeight - nodeHeight) / 2);
            double x2 = addDstClusterX * fieldWidth
                    + ((fieldWidth - clusterWidth) / 2)
                    + ((clusterWidth - nodeWidth) / 2);
            double y2 = addDstClusterY * fieldHeight
                    + ((fieldHeight - clusterHeight) / 2)
                    + rItem.getNodeDst() * nodeSize
                    + ((clusterHeight - nodeHeight) / 2);

            drawArrow(x1, y1, x2, y2);
        }
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        Integer arrowSize = 8;
        Double dx = x2 - x1;
        Double dy = y2 - y1;
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
    public void handleNewButton() {
        network = new Network();
        drawCanvas(network);
        statusBar.setText("Created new network");
    }

    public void handleCloseButton() {
        Platform.exit();
        System.exit(0);
    }

    public void handleLoadButton() {
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
                clusterSize.setValue(network.getClusterSize());
                migrationCost.setValue(network.getMigrationCost());

            } catch (IOException e) {
                e.printStackTrace();
            }
            statusBar.setText("Loaded from " + file.getName());
        }
    }

    public void handleSaveButton() {
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
            statusBar.setText("Saved to " + file.getName());
        }
    }

    public void handleAddCluster() {
        network.addCluster();
        drawCanvas(network);
        statusBar.setText("Added new cluster");
    }

    public void handleRequests() {
        try {
            network.generateRequests();
            statusBar.setText("Added new " + network.getRequests().size() + " requests");
            drawCanvas(network, showRequestsBox.isSelected());
        } catch (IllegalStateException e) {
            errorDialog("Add a cluster and at least two nodes!");
        }
    }

    public void handleRequstsBox() {
        drawCanvas(network, showRequestsBox.isSelected());
    }

    public void handleRunCrep() {
        try {
            network.CREP();
            drawCanvas(network, showRequestsBox.isSelected());
            statusBar.setText("Ran CREP");
        } catch (IllegalStateException e) {
            errorDialog("Firstly add requests!");
        }
    }
    //endregion

    //region util
    //Hibaüzenet
    private void errorDialog(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.showAndWait();
    }
    //endregion

    //region calculate
    //Klaszter meghatározása a canvas-on
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
                    }
                    processed = processed + 1;
                } else {
                    break;
                }
            }
        }
        return null;
    }

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

    //alaprajz méretének meghatározása
    private Integer factorization(Integer num) {
        return new Double(Math.ceil(Math.sqrt(num))).intValue();
    }

    //segédfüggvény a request kirajzoláshoz
    private List<RequestItem> collectRequestItems() {
        List<RequestItem> rList = new ArrayList<>();
        for (Request request : network.getRequests()) {
            List<Node> nList = new ArrayList<>();
            network.getClusters().forEach(cluster -> cluster.getNodes()
                    .forEach(nList::add));

            Integer iterSrc = 0;
            Boolean lSrc = false;
            for (; !lSrc && iterSrc < nList.size(); iterSrc++) {
                if (nList.get(iterSrc).getId().equals(request.getSrc())) {
                    lSrc = true;
                }
            }

            Integer iterDst = 0;
            Boolean lDst = false;
            for (; !lDst && iterDst < nList.size(); iterDst++) {
                if (nList.get(iterDst).getId().equals(request.getDst())) {
                    lDst = true;
                }
            }

            Integer iterCSrc = 0;
            Boolean lCSrc = false;
            for (; !lCSrc && iterCSrc < network.getClusters().size(); iterCSrc++) {
                if (iterSrc > network.getClusters().get(iterCSrc).getNodes().size()) {
                    iterSrc = iterSrc - network.getClusters().get(iterCSrc).getNodes().size();
                } else {
                    lCSrc = true;
                }
            }

            Integer iterCDst = 0;
            Boolean lCDst = false;
            for (; !lCDst && iterCDst < network.getClusters().size(); iterCDst++) {
                if (iterDst > network.getClusters().get(iterCDst).getNodes().size()) {
                    iterDst = iterDst - network.getClusters().get(iterCDst).getNodes().size();
                } else {
                    lCDst = true;
                }
            }
            rList.add(new RequestItem(iterSrc - 1, iterDst - 1, iterCSrc - 1, iterCDst - 1));
        }
        return rList;
    }

    //segédosztály a request kirajzoláshoz
    private class RequestItem {
        final Integer nodeSrc;
        final Integer nodeDst;
        final Integer clusterSrc;
        final Integer clusterDst;

        private RequestItem(Integer nodeSrc, Integer nodeDst, Integer clusterSrc, Integer clusterDst) {
            this.nodeSrc = nodeSrc;
            this.nodeDst = nodeDst;
            this.clusterSrc = clusterSrc;
            this.clusterDst = clusterDst;
        }

        private Integer getNodeSrc() {
            return nodeSrc;
        }

        private Integer getNodeDst() {
            return nodeDst;
        }

        private Integer getClusterSrc() {
            return clusterSrc;
        }

        private Integer getClusterDst() {
            return clusterDst;
        }
    }
    //endregion
}
