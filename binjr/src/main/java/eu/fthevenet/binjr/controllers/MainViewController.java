package eu.fthevenet.binjr.controllers;

import eu.fthevenet.binjr.controls.ContextMenuTreeViewCell;
import eu.fthevenet.binjr.controls.EditableTab;
import eu.fthevenet.binjr.data.adapters.DataAdapter;
import eu.fthevenet.binjr.data.adapters.DataAdapterException;
import eu.fthevenet.binjr.data.adapters.TimeSeriesBinding;
import eu.fthevenet.binjr.data.workspace.ChartType;
import eu.fthevenet.binjr.data.workspace.Worksheet;
import eu.fthevenet.binjr.dialogs.Dialogs;
import eu.fthevenet.binjr.dialogs.EditWorksheetDialog;
import eu.fthevenet.binjr.sources.jrds.adapters.JRDSDataAdapter;
import eu.fthevenet.binjr.dialogs.GetDataAdapterDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The controller class for the main view
 *
 * @author Frederic Thevenet
 */
public class MainViewController implements Initializable {
    private static final Logger logger = LogManager.getLogger(MainViewController.class);
    @FXML
    public VBox root;
    @FXML
    private Menu viewMenu;
    @FXML
    private Menu helpMenu;
    @FXML
    private MenuItem editRefresh;
    @FXML
    private TabPane sourcesTabPane;
    @FXML
    private TabPane seriesTabPane;
    @FXML
    private MenuItem newTab;
    @FXML
    private CheckMenuItem showXmarkerMenuItem;
    @FXML
    private CheckMenuItem showYmarkerMenuItem;
    private SimpleBooleanProperty showVerticalMarker = new SimpleBooleanProperty();

    public boolean isShowVerticalMarker() {
        return showVerticalMarker.getValue();
    }

    public SimpleBooleanProperty showVerticalMarkerProperty() {
        return showVerticalMarker;
    }

    public boolean isShowHorizontalMarker() {
        return showHorizontalMarker.getValue();
    }

    public SimpleBooleanProperty showHorizontalMarkerProperty() {
        return showHorizontalMarker;
    }

    private SimpleBooleanProperty showHorizontalMarker = new SimpleBooleanProperty();
    //private final ContextMenu treeContextMenu;

    private final Map<TimeSeriesController, Worksheet> worksheetMap;

    public MainViewController() {
        super();
        worksheetMap = new HashMap<>();
    }

    @FXML
    protected void handleAboutAction(ActionEvent event) throws IOException {
        Dialog<String> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("About binjr");
        dialog.setDialogPane(FXMLLoader.load(getClass().getResource("/views/AboutBoxView.fxml")));
        dialog.initOwner(Dialogs.getStage(root));
        dialog.showAndWait();
    }

    @FXML
    protected void handleQuitAction(ActionEvent event) {
        Platform.exit();
    }

    private AtomicInteger nbSeries = new AtomicInteger(0);

    @FXML
    protected void handleNewTabAction(ActionEvent actionEvent) {
        //seriesTabPane.getTabs().add(new Tab("New series (" + nbSeries.incrementAndGet() + ")"));
    }

    private Map<Tab, TimeSeriesController> seriesControllers = new HashMap<>();

    private TreeView<TimeSeriesBinding<Double>> buildTreeViewForTarget(DataAdapter dp) {
        TreeView<TimeSeriesBinding<Double>> treeView = new TreeView<>();

        treeView.setCellFactory(ContextMenuTreeViewCell.forTreeView(getTreeViewContextMenu(treeView)));
        try {
            TreeItem<TimeSeriesBinding<Double>> root = dp.getBindingTree();

            root.setExpanded(true);

            treeView.setRoot(root);
//            treeView.setOnMouseClicked(event -> {
//                if (event.getClickCount() == 2) {
//                    TreeItem<TimeSeriesBinding<Double>> item = treeView.getSelectionModel().getSelectedItem();
//                    if (selectedTabController != null && item !=null) {
//                        List<TimeSeriesBinding> bindings = new ArrayList<>();
//                        getAllBindingsFromBranch(item, bindings);
//
//                        selectedTabController.addBinding(item.getValue());
//                    }
//                }
//            });
        } catch (DataAdapterException e) {
            Dialogs.displayException("An error occurred while building the tree from " + (dp != null ? dp.getSourceName() : "null"), e, root);
        }
        return treeView;
    }

    private <T> void getAllBindingsFromBranch(TreeItem<T> branch, List<T> bindings) {
        if (branch.getChildren().size() > 0) {
            for (TreeItem<T> t : branch.getChildren()) {
                getAllBindingsFromBranch(t, bindings);
            }
        } else {
            bindings.add(branch.getValue());
        }
    }

    private TimeSeriesController selectedTabController;

    private void handleControlKey(KeyEvent event, boolean pressed) {
        switch (event.getCode()) {
            case SHIFT:
                showHorizontalMarker.set(pressed);
                event.consume();
                break;
            case CONTROL:
            case META:
            case SHORTCUT: // shortcut does not seem to register as Control on Windows here, so check them all.
                showVerticalMarker.set(pressed);
                event.consume();
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assert viewMenu != null : "fx:id\"editMenu\" was not injected!";
        assert root != null : "fx:id\"root\" was not injected!";
        assert seriesTabPane != null : "fx:id\"seriesTabPane\" was not injected!";
        assert sourcesTabPane != null : "fx:id\"sourceTabPane\" was not injected!";

        root.addEventFilter(KeyEvent.KEY_PRESSED, e -> handleControlKey(e, true));
        root.addEventFilter(KeyEvent.KEY_RELEASED, e -> handleControlKey(e, false));
        showXmarkerMenuItem.selectedProperty().bindBidirectional(showVerticalMarker);
        showYmarkerMenuItem.selectedProperty().bindBidirectional(showHorizontalMarker);

        seriesTabPane.getSelectionModel().clearSelection();
        seriesTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue == null) {
                    return;
                }
                if (newValue.getContent() == null) {

                        EditWorksheetDialog<Double> dlg = new EditWorksheetDialog<>(root);
                        dlg.showAndWait().ifPresent(worksheet -> {
                            try {
                                // Loading content on demand
                                FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("/views/TimeSeriesView.fxml"));
                                TimeSeriesController current = new StackedAreaChartTimeSeriesController(MainViewController.this);
                                fXMLLoader.setController(current);
                                Parent p = fXMLLoader.load();

                                // Store the controllers

                                newValue.setContent(p);
                                selectedTabController = current;
                                worksheetMap.put(current, worksheet);
                                seriesControllers.put(newValue, current);
                                ((EditableTab) newValue).nameProperty().bindBidirectional(worksheet.nameProperty());
                                // add "+" tab
                                seriesTabPane.getTabs().add(new EditableTab("+"));

                            } catch (IOException ex) {
                                logger.error("Error loading time series", ex);
                            }
                        });

                }
            }
        });
        // By default, select 1st tab and load its content.
        seriesTabPane.getSelectionModel().selectFirst();
        seriesTabPane.getTabs().add(new EditableTab("New worksheet"));
        seriesTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.selectedTabController = seriesControllers.get(newValue);
            }
        });

        sourcesTabPane.getSelectionModel().clearSelection();
        sourcesTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            if (newValue.getContent() == null) {
                TreeView<TimeSeriesBinding<Double>> treeView;
                @SuppressWarnings("unchecked")
                DataAdapter<Double> da = (DataAdapter<Double>) newValue.getUserData();
                treeView = buildTreeViewForTarget(da);
                newValue.setContent(treeView);
            }
        });
    }

    public void handleRefreshAction(ActionEvent actionEvent) {
        if (selectedTabController != null) {
            selectedTabController.invalidate(false, true, true);
        }
    }

    private ContextMenu getTreeViewContextMenu(final TreeView<TimeSeriesBinding<Double>> treeView) {
        MenuItem addToCurrent = new MenuItem("Add to current worksheet");
        addToCurrent.setOnAction(event -> {
            TreeItem<TimeSeriesBinding<Double>> item = treeView.getSelectionModel().getSelectedItem();
            if (selectedTabController != null && item != null) {
                List<TimeSeriesBinding<Double>> bindings = new ArrayList<>();
                getAllBindingsFromBranch(item, bindings);

                selectedTabController.addBindings(bindings);
            }
        });
        MenuItem addToNew = new MenuItem("Add to new worksheet");
        addToNew.setOnAction(event -> {

        });
        return new ContextMenu(addToCurrent, addToNew);
    }

    @FXML
    public void handlePreferencesAction(ActionEvent actionEvent) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.initModality(Modality.NONE);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Preferences");
            dialog.setDialogPane(FXMLLoader.load(getClass().getResource("/views/PreferenceDialogView.fxml")));
            dialog.initOwner(Dialogs.getStage(root));
            dialog.show();
        } catch (Exception ex) {
            Dialogs.displayException("Failed to display preference dialog", ex, root);
        }
    }

    public void handleAddJRDSSource(ActionEvent actionEvent) {
        GetDataAdapterDialog dlg = new GetDataAdapterDialog("Add a JRDS source", JRDSDataAdapter::fromUrl, root);
        dlg.showAndWait().ifPresent(da ->
        {
            Tab newTab = new Tab(da.getSourceName());
            newTab.setUserData(da);
            sourcesTabPane.getTabs().add(newTab);
            sourcesTabPane.getSelectionModel().select(newTab);
        });

    }

    public Map<TimeSeriesController, Worksheet> getWorksheetMap() {
        return worksheetMap;
    }
}
