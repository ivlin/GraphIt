import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;

import javafx.geometry.Insets;
import javafx.util.StringConverter;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphIt extends Application {
    private StatisticsGroup statistics;
    private Graph graph;
    private ArrayList<DataPoint> data;
    private ObservableList<DataPoint> dataset;
    private int width, height;
    //private HashMap<String, DisplayValue> statistics;
    
    @Override
    public void start(Stage primaryStage){
	data = new ArrayList<DataPoint>();
	dataset = FXCollections.observableList(data);
	
	width=1450; height=833;
	
	TabPane tabPane = new TabPane();
	Tab graphTab = new Tab();
	graphTab.setText("Graph");
	graphTab.setContent(getGraphTab(800,800));
	graphTab.setClosable(false);
	Tab optionsTab = new Tab();
	optionsTab.setText("Options");
	optionsTab.setContent(getOptionsTab(primaryStage));
	optionsTab.setClosable(false);

	tabPane.getTabs().add(graphTab);
	tabPane.getTabs().add(optionsTab);
	
	Scene scene = new Scene(tabPane, width, height);
	scene.getStylesheets().add(getClass().getResource("stylesheets/stylesheet.css").toExternalForm());
	primaryStage.setTitle("GraphIt");
	primaryStage.setScene(scene);
	primaryStage.show();
    }

    public Pane getGraphTab(int width, int height){
	Pane buttonPane = getButtonPane();

	graph = new Graph(width,height,dataset,statistics);

	Pane graphPane = new Pane();
	graphPane.getChildren().add(graph.getCanvas());
	graph.reload();
	
	BorderPane pane = new BorderPane();
	pane.setCenter(graphPane);
	pane.setRight(buttonPane);
	buttonPane.setPrefWidth(650);

	return pane;
    }
    
    public Pane getButtonPane(){
	statistics = new StatisticsGroup();

	GridPane buttonGrid = new GridPane();
	buttonGrid.setHgap(10);
	buttonGrid.setVgap(10);
	buttonGrid.setPadding(new Insets(10,10,10,10));
	buttonGrid.getStyleClass().addAll("gridpane");
	Label viewStatistics = new Label("Show/Hide Statistics");
	viewStatistics.getStyleClass().addAll("heading");
	ToggleButton meanBtn = statistics.get("mean").button;
	ToggleButton medianBtn = statistics.get("median").button;
	Label meanLbl = statistics.get("mean").label;
	Label medianLbl = statistics.get("median").label;	
	ToggleButton stdDevBtn = statistics.get("standard deviation").button;
	Label stdDevLbl = statistics.get("standard deviation").label;
	ToggleButton regrLineBtn = statistics.get("regression line").button;
	Label regrSlopeLbl = statistics.get("slope").label;
	Label regrIntcptLbl = statistics.get("intercept").label;
	ToggleButton coeffDetermBtn = statistics.get("coefficient of determination").button;
	Label coeffDetermLbl = statistics.get("coefficient of determination").label;
	ToggleButton covarianceBtn = statistics.get("covariance").button;
	Label covarianceLbl = statistics.get("covariance").label;

	ToggleButton partitionBtn = new ToggleButton("Toggle Graph");
	partitionBtn.setOnAction(e->{
	        graph.toggleBarGraphView();
		graph.reload();
	    });
	
	buttonGrid.add(viewStatistics,0,0,2,1);
	buttonGrid.add(meanBtn,0,1); buttonGrid.add(meanLbl,1,1);
	buttonGrid.add(medianBtn,0,2); buttonGrid.add(medianLbl,1,2);
	buttonGrid.add(stdDevBtn,0,3); buttonGrid.add(stdDevLbl,1,3);
	buttonGrid.add(regrLineBtn,0,4); buttonGrid.add(statistics.get("regression line").label,1,4);
	buttonGrid.add(coeffDetermBtn,0,5); buttonGrid.add(coeffDetermLbl,1,5);
	buttonGrid.add(covarianceBtn,0,6); buttonGrid.add(covarianceLbl,1,6);
	buttonGrid.add(partitionBtn,0,7);
	
	meanBtn.setOnAction(e->{
		statistics.get("mean").toggleValid(); graph.reload();
	    });
	medianBtn.setOnAction(e->{
		statistics.get("median").toggleValid(); graph.reload();
	    });
	stdDevBtn.setOnAction(e->{
		statistics.get("standard deviation").toggleValid(); graph.reload();
	    });
	regrLineBtn.setOnAction(e->{
		statistics.get("regression line").toggleValid(); graph.reload();
	    });

	VBox buttonPane = new VBox();

	buttonPane.getChildren().addAll(getCoordinateControlsPane(),buttonGrid);
	buttonPane.setStyle("-fx-border-color:black");
	return buttonPane;
    }

    public Pane getCoordinateControlsPane(){
	GridPane setCoorFields = new GridPane();
	setCoorFields.getStyleClass().addAll("gridpane");
	setCoorFields.setPadding(new Insets(10,10,10,10));
	Label setCoordinatesLbl = new Label("Set Coordinates");
	setCoordinatesLbl.getStyleClass().addAll("heading");
	TextField minXTxBox = new TextField("minX"); TextField minYTxBox = new TextField("minY");
	TextField maxXTxBox = new TextField("maxX"); TextField maxYTxBox = new TextField("maxY");
	Button changeViewBtn = new Button("Set");
	ToggleButton snapToGridBtn = new ToggleButton("Toggle Snap to Grid");
	Button clearGridBtn = new Button("Clear");
	ToggleButton deleteOnClickBtn = new ToggleButton("Toggle Delete on Click");
	setCoorFields.add(setCoordinatesLbl,0,0);
	setCoorFields.add(minXTxBox,0,1);
	setCoorFields.add(minYTxBox,1,1);
	setCoorFields.add(maxXTxBox,0,2);
	setCoorFields.add(maxYTxBox,1,2);
	setCoorFields.setHgap(10);  setCoorFields.setVgap(10);
	setCoorFields.add(changeViewBtn,0,3);
	setCoorFields.add(clearGridBtn,1,3);
	setCoorFields.add(snapToGridBtn,0,4);
	setCoorFields.add(deleteOnClickBtn,1,4);
	changeViewBtn.setOnAction(e->{
		try{
		    graph.setViewFrame(Double.parseDouble(minXTxBox.getText()),
				       Double.parseDouble(minYTxBox.getText()),
				       Double.parseDouble(maxXTxBox.getText()),
				       Double.parseDouble(maxYTxBox.getText()));
		    graph.reload();
		    minXTxBox.setText("minX");
		    minYTxBox.setText("minY");
		    maxXTxBox.setText("maxX");
		    maxYTxBox.setText("maxY");
		} catch (NumberFormatException ex){
		    Alert alert = new Alert(Alert.AlertType.INFORMATION);
		    alert.setTitle("GraphIt");
		    alert.setHeaderText("Improperly formatted input");
		    alert.setContentText("The input for changing the viewframe has been imporperly formatted. Please make sure the input is a floating point decimal number and consists of only numbers and at most one decimal.");
		    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		    alert.showAndWait();
		}
	    });
	deleteOnClickBtn.setOnAction(e->{
		graph.toggleDeleteOnClick();
	    });
	snapToGridBtn.setOnAction(e->{
		graph.toggleSnapToGrid();
	    });
	clearGridBtn.setOnAction(e-> {
		dataset.clear();
		statistics.updateStats(dataset);
		graph.reload();
	    });
	return setCoorFields;
    }


    public Pane getOptionsTab(Stage stage){
	BorderPane optionsPane = new BorderPane();
	VBox optionsContent = new VBox();
		
	TableView<DataPoint> dataTable = getTable();	
       
	HBox addDataPoint = new HBox();
	addDataPoint.setPadding(new Insets(10,10,0,10));
	Label newXCorLbl = new Label("X:");
	newXCorLbl.setPrefWidth(100);
	TextField newXCorBox = new TextField();
	newXCorBox.setPrefHeight(50);
	newXCorBox.setPrefWidth(250);
	Label newYCorLbl = new Label("Y:");
	newYCorLbl.setPrefWidth(100);
	TextField newYCorBox = new TextField();
	newYCorBox.setPrefHeight(50);
	newYCorBox.setPrefWidth(250);
	Button addNewDataPointBtn = new Button("Add");
	addNewDataPointBtn.setOnAction(e->{
		try{
		    dataset.add(new DataPoint(Double.parseDouble(newXCorBox.getText()),
					      Double.parseDouble(newYCorBox.getText())));
		    statistics.updateStats(dataset);
		    graph.reload();
		    
		} catch (NumberFormatException ex){
		    Alert alert = new Alert(Alert.AlertType.INFORMATION);
		    alert.setTitle("GraphIt");
		    alert.setHeaderText("Improperly formatted input");
		    alert.setContentText("The input for creating a new datapoint has been improperly formatted. Please make sure the input is a floating point decimal number and consists of only numbers and at most one decimal.");
		    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		    alert.showAndWait();
		    System.out.println("Invalid coordinates");
		}
		});
	addDataPoint.getChildren().addAll(newXCorLbl, newXCorBox, newYCorLbl, newYCorBox, addNewDataPointBtn);
	addDataPoint.setSpacing(10);

	optionsContent.getChildren().addAll(dataTable, addDataPoint);

	optionsPane.setCenter(optionsContent);
	optionsPane.setRight(getFunctionsPanel());
	optionsPane.setBottom(getFileControlPane(stage));
	return optionsPane;
    }
    
    public TableView<DataPoint> getTable(){
	StringConverter<Double> converter = (new StringConverter<Double>(){
		@Override
		public Double fromString(String s){
		    return Double.parseDouble(s);
		}
		@Override
		public String toString(Double d){
		    return ""+d;
		}
	    });

	TableView<DataPoint> dataTable = new TableView<DataPoint>();
	dataTable.setPrefHeight(700);
	dataTable.setItems(dataset);
	TableColumn<DataPoint,Double> xCor = new TableColumn<>("x");
	xCor.setCellValueFactory(new PropertyValueFactory<DataPoint,Double>("x"));
	xCor.setPrefWidth(3*xCor.getPrefWidth());
	TableColumn<DataPoint,Double> yCor = new TableColumn<>("y");
	yCor.setCellValueFactory(new PropertyValueFactory<DataPoint,Double>("y"));
	yCor.setPrefWidth(3*yCor.getPrefWidth());
	TableColumn<DataPoint,Integer> orderAdded = new TableColumn<>("Order");
	orderAdded.setPrefWidth(5*orderAdded.getPrefWidth());
	orderAdded.setCellValueFactory(new PropertyValueFactory<DataPoint,Integer>("orderAdded"));
	
	dataTable.setEditable(true);
	
	dataTable.getColumns().add(xCor);
	dataTable.getColumns().add(yCor);
	dataTable.getColumns().add(orderAdded);
	xCor.setCellFactory(TextFieldTableCell.<DataPoint,Double>forTableColumn(converter));
	xCor.setOnEditCommit(new EventHandler<CellEditEvent<DataPoint,Double>>(){
		@Override
		public void handle(CellEditEvent<DataPoint, Double> t) {
		    ((DataPoint)t.getTableView().getItems().get(t.getTablePosition().getRow())).setX(t.getNewValue());
		    statistics.updateStats(dataset);
		    graph.reload();
		}
	    });
	
	yCor.setCellFactory(TextFieldTableCell.<DataPoint,Double>forTableColumn(converter));
	yCor.setOnEditCommit(new EventHandler<CellEditEvent<DataPoint,Double>>(){
		@Override
		public void handle(CellEditEvent<DataPoint, Double> t) {
		    ((DataPoint)t.getTableView().getItems().get(t.getTablePosition().getRow())).setY(t.getNewValue());
		    statistics.updateStats(dataset);
		    graph.reload();
		}
	    });
	return dataTable;
    }
    
    public Pane getFunctionsPanel(){
	Alert invalidIOAlert = new Alert(Alert.AlertType.INFORMATION);
	invalidIOAlert.setTitle("GraphIt");
	invalidIOAlert.setHeaderText("Improperly formatted input");
	invalidIOAlert.setContentText("The input must be a valid javascript-formatted math expression using x or y.");
	invalidIOAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
     
	VBox applyFunctions = new VBox();
	HBox xFunction = new HBox();
	Label xprime = new Label("f'(x)=");
	TextField fx = new TextField();
	Button applyFxBtn = new Button("Apply to X");
	fx.setPrefHeight(50);
	applyFxBtn.setOnAction(e->{
		boolean validIO=true;
		if (validIO = fx.getText().matches("^[\\d\\s\\+\\-\\*/xy]+$")){
		    for (DataPoint d: dataset){
			d.setX(statistics.eval(fx.getText().replaceAll("x",""+d.getX()).replaceAll("y",""+d.getY())));
		    }
		}
		if (!validIO){
		    invalidIOAlert.showAndWait();
		}
	    });
	xFunction.getChildren().addAll(xprime,fx,applyFxBtn);
	HBox yFunction = new HBox();
	Label yprime = new Label("f'(y)=");
	TextField fy = new TextField();
	Button applyFyBtn = new Button("Apply to Y");
	fy.setPrefHeight(50);
	applyFyBtn.setOnAction(e->{
		boolean validIO=true;
		if (validIO = fy.getText().matches("^[\\d\\s\\+\\-\\*/xy]+$")){
		    validIO= statistics.eval(fy.getText()) != Double.NaN;
		    for (DataPoint d: dataset){
			d.setY(statistics.eval(fy.getText().replaceAll("x",""+d.getX()).replaceAll("y",""+d.getY())));
		    }
		}
		if (!validIO){
		    invalidIOAlert.showAndWait();
		}
	    });
	yFunction.getChildren().addAll(yprime,fy,applyFyBtn);

	applyFunctions.getChildren().addAll(xFunction, yFunction);
	return applyFunctions;
    }

    public Pane getFileControlPane(Stage stage){
	HBox fileControlButtons = new HBox();
	fileControlButtons.setPadding(new Insets(10,10,10,10));
	fileControlButtons.setSpacing(10);

	Button openFileBtn = new Button("Open a CSV");
	//openFileBtn.setPrefWidth(800);
	openFileBtn.setOnAction(e ->{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); 
		try{
		    File file = fileChooser.showOpenDialog(stage);
		    if (file != null) {
			Scanner sc = new Scanner(file);
			sc.useDelimiter("[,|\\s*|\\n]*");
			double[] coords=new double[2];
			int coordInd=0;
			while (sc.hasNext()){
			    coords[coordInd]=Double.parseDouble(sc.next());
			    if (coords[coordInd]!=Double.NaN){
				coordInd=(coordInd+1)%coords.length;
				if (coordInd==0){
				    dataset.add(new DataPoint(coords[0],coords[1]));
				}
			    }
			}
		    }
		} catch(FileNotFoundException ex){
		}
	    });
	
	fileControlButtons.getChildren().add(openFileBtn);
	HBox exportFileControls = new HBox();
	exportFileControls.setSpacing(10);
	TextField exportFilename = new TextField("output.csv");
	exportFilename.setPrefWidth(500);
	exportFilename.setPrefHeight(50);
	Button exportDataBtn = new Button("Export Data To:");
	exportDataBtn.setOnAction(e ->{
		if (!exportFilename.getText().isEmpty()){
		    try{
			FileWriter writer = new FileWriter(exportFilename.getText()+".csv");
			for (DataPoint d: dataset){
			    writer.write(d.getX()+","+d.getY()+"\n");
			}
			writer.flush();
		    }catch (IOException ex){}
		}
	    });
	exportFileControls.getChildren().add(exportDataBtn);
	exportFileControls.getChildren().add(exportFilename);
	fileControlButtons.getChildren().addAll(exportFileControls);
	return fileControlButtons;
    }
    
    public static void main(String[]args){
        launch(args);
    }
}
