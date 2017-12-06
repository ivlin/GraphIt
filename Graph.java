import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.event.EventHandler;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import javafx.collections.ObservableList;

public class Graph{
    
    /*
      CONSTANTS
     */
    private final double DEFAULT_INTERVAL_SIZE=100,//pixel distance between coordinates
	MIN_INTERVAL_SIZE=20,
	MAX_INTERVAL_SIZE=200,
	DEFAULT_INTERVAL=1;//coordinate distance between intervals

    /*
      PROPERTIES
     */
    private ObservableList<DataPoint> dataset;
    private StatisticsGroup statistics;
    private Canvas canvas;
    private double width, height, minX, minY;//minX and minY are the coordinate of the minX*intervalSize
    private double intervalSize;
    private double interval;
    private boolean isSnapToGrid;
    private boolean isDisplayingBarGraph;
    /*
      EVENTLISTENER
     */
    private boolean deleteOnClick;
    private DataPoint mouseFocus;//used for optimization purposes

    /*
      CONVENIENCE FEATURES
     */
    private int magnification=0;
    
    public Graph(double width, double height, ObservableList<DataPoint> dataset, StatisticsGroup statistics){
	this.width=width; this.height=height; this.dataset=dataset;  this.statistics=statistics;
	minX=-width/2; minY=-height/2;
	canvas = new Canvas();
	canvas.setWidth(width); canvas.setHeight(height);
	intervalSize=DEFAULT_INTERVAL_SIZE;
	interval=DEFAULT_INTERVAL;
	initListeners(canvas,canvas.getGraphicsContext2D());
	mouseFocus=null;
	isSnapToGrid=false;
	deleteOnClick=false;
	isDisplayingBarGraph=false;
    }

    public Canvas getCanvas(){
	return canvas;
    }

    public void reload(){
	canvas.setWidth(width);
	canvas.setHeight(height);
	drawGraph(canvas,canvas.getGraphicsContext2D());
    }

    public void setViewFrame(double minX, double minY,
			     double maxX, double maxY){
	double maxint = maxX-minX > maxY-minY ? maxX-minX : maxY-minY;
	double intSize = width / maxint;
	if (intSize > MAX_INTERVAL_SIZE){
	    intervalSize = DEFAULT_INTERVAL_SIZE;
	    interval = maxint*intervalSize/width;
	    System.out.println("zoom out");
	}
	else if (intSize < MIN_INTERVAL_SIZE) {
	    intervalSize = DEFAULT_INTERVAL_SIZE;
	    interval = maxint*intervalSize/width;
	    System.out.println("zoom in");
	}
	this.minX = minX*intervalSize;//corToPixel(minX,'x');
	this.minY = minY*intervalSize;//corToPixel(minY,'y');;
    }

    public void toggleSnapToGrid(){
	isSnapToGrid=!isSnapToGrid;
    }

    public void toggleDeleteOnClick(){
	deleteOnClick=!deleteOnClick;
    }

    public void toggleBarGraphView(){
	isDisplayingBarGraph=!isDisplayingBarGraph;
    }
    
    /*
      PRIVATE METHODS
     */    
    private void initListeners(Canvas canvas, GraphicsContext ctx){
	canvas.addEventHandler(ScrollEvent.ANY, new CanvasScrollEventListener());
	canvas.addEventHandler(MouseEvent.ANY, new CanvasMouseEventListener());
    }

    private void drawGraph(Canvas c, GraphicsContext ctx){	
	//Draw graph
	drawPlane(ctx);
	//Draw points
	drawPoints(ctx);
	//Regression Line
	if (statistics.get("regression line").isValid){
	    drawRegressionLine(ctx);
	}
	//Standard deviation lines
	if (statistics.get("mean").value!=Double.NaN && statistics.get("mean").isValid){
	    drawStdDeviation(ctx);
	}
	//Histogram
	if (isDisplayingBarGraph){
	    drawBarGraph(ctx);
	}
    }

    /*
      HANDLERS
    */
    
    private void drawPlane(GraphicsContext ctx){
	ctx.clearRect(0,0,width,height);
	
	double startX = intervalSize*Math.ceil(minX/intervalSize);
	double startY = intervalSize*Math.floor(minY/intervalSize);
	
	double axisCoordinates=0;
	
	double value; //used for rounding
	ctx.setLineWidth(1);
	ctx.beginPath();
	ctx.setStroke(Color.LIGHTGREY);	
	ctx.setFill(Color.BLACK);	
	for (double i=startX-minX;i<width;i+=intervalSize){
	    if (Math.round(i+minX)==0){//y-axis
		axisCoordinates=i;
		ctx.stroke();
		ctx.closePath();ctx.beginPath();
	    	ctx.setStroke(Color.BLACK);
		ctx.moveTo(i,0);
	    	ctx.lineTo(i,height);
	    	ctx.stroke();
		ctx.setStroke(Color.LIGHTGREY);
		ctx.closePath();ctx.beginPath();
	    }
	    else{
		axisCoordinates=height+minY;
		if (axisCoordinates<10){
		    axisCoordinates=10;
		}
		else if (axisCoordinates>height-35){
		    axisCoordinates=height-35;
		}
		value = interval>=1?Math.round((i+minX)/intervalSize*interval):(i+minX)/intervalSize*interval;
	    	ctx.fillText(""+value,i,axisCoordinates);
		ctx.moveTo(i,0);
	    	ctx.lineTo(i,height);
	    }	
	}
	for (double i=startY-minY;i<height;i+=intervalSize){
	    if (Math.round(i+minY)==0){//x-axis
		axisCoordinates=i;
	    	ctx.stroke();
		ctx.closePath();ctx.beginPath();
	    	ctx.setStroke(Color.BLACK);
	    	ctx.moveTo(0,height-i);
	    	ctx.lineTo(width,height-i);
	    	ctx.stroke();
		ctx.setStroke(Color.LIGHTGREY);
		ctx.closePath();ctx.beginPath();
	    }
	    else{
		axisCoordinates=-minX;
		if (axisCoordinates<2){
		    axisCoordinates=2;
		}
		else if (axisCoordinates>width-20){
		    axisCoordinates=width-20;
		}
		value = interval>=1?Math.round((i+minY)/intervalSize*interval):(i+minY)/intervalSize*interval;
	    	ctx.fillText(""+value,axisCoordinates,height-i);
		ctx.stroke();
	    	ctx.moveTo(0,height-i);
	    	ctx.lineTo(width,height-i);
	    }
	}
	ctx.stroke();
	ctx.closePath();
    }
    
    private void drawPoints(GraphicsContext ctx){
	ctx.beginPath();
	ctx.setStroke(Color.BLACK);
	double xCor; boolean isMedian;
	for (DataPoint d: dataset){
	    xCor=corToPixel(d.getX(),'x')-d.getRadius()/2;
	    isMedian= d.getX()==statistics.get("median").value || d.getX()==statistics.get("median1").value || d.getX()==statistics.get("median2").value;
	    if (isMedian && statistics.get("median").isValid){
		ctx.stroke();
		ctx.closePath();
		ctx.beginPath();
		ctx.setStroke(Color.RED);
	    }
	    ctx.strokeOval(corToPixel(d.getX(),'x')-d.getRadius()/2,
			   corToPixel(d.getY(),'y')-d.getRadius()/2,
			   d.getRadius(),d.getRadius());
	    if (isMedian && statistics.get("median").isValid){
		ctx.stroke();
		ctx.closePath();
		ctx.beginPath();
		ctx.setStroke(Color.BLACK);
	    }
	}
	ctx.closePath();
    }
    
    private void drawRegressionLine(GraphicsContext ctx){
	double intercept = statistics.get("intercept").value;
	double slope = statistics.get("slope").value;
	if (intercept!=Double.NaN && slope!=Double.NaN){
	    ctx.beginPath();
	    ctx.strokeLine(0, minX*-slope+corToPixel(intercept,'y'),
			   width, (minX+width)*-slope+corToPixel(intercept,'y'));
	    ctx.stroke();
	    ctx.closePath();
	}
    }
    
    private void drawStdDeviation(GraphicsContext ctx){
	int mindev=0; int maxdev=0;
	if (statistics.get("standard deviation").value!=Double.NaN && statistics.get("standard deviation").isValid){
	    mindev=-3; maxdev=3;
	}
	ctx.beginPath();
	for (int curdev=mindev; curdev<=maxdev; curdev++){
	    ctx.setStroke(Color.BLUE);
	    double dev = corToPixel(statistics.get("standard deviation").value*curdev + statistics.get("mean").value,'x');
	    ctx.strokeLine(dev,0,dev,height);
	    ctx.strokeText(""+Math.abs(curdev),dev,height-40);
	}
	ctx.stroke();
	ctx.closePath();
    }

    private void drawBarGraph(GraphicsContext ctx){
	ctx.beginPath();
	HashMap<Double,Double> partitions = statistics.partition(dataset,interval);
	if (partitions!=null){
	    Iterator<HashMap.Entry<Double,Double>> it = partitions.entrySet().iterator();
	    ctx.setFill(Color.BLUE);
	    ctx.setGlobalAlpha(0.25);
	    while (it.hasNext()){
		HashMap.Entry<Double,Double> e = it.next();
		double pixCor = corToPixel(e.getKey(),'x');
		if (pixCor >= minX-intervalSize && pixCor<= width+intervalSize){
		    ctx.fillRect(pixCor, corToPixel(e.getValue()*interval,'y'),
				   intervalSize,e.getValue()*intervalSize);
		}
	    } 
	    ctx.setGlobalAlpha(1.0);
	}
	ctx.closePath();
    }
    
    /*
      HELPERS
    */
    private double getSqDistance(double x1, double y1, double x2, double y2){
	return Math.pow(x1-x2,2)+Math.pow(y1-y2,2);
    }
    private double corToPixel(double cor, char axis){
	return axis=='x' ? cor*intervalSize/interval-minX : height-(cor*intervalSize/interval)+minY;
    }
    private double pixelToCor(double cor, char axis){
	return axis=='x' ? (cor+minX)/intervalSize*interval : (height-cor+minY)/intervalSize*interval;
    }

    /*
      HANDLERS
    */
    class CanvasScrollEventListener implements EventHandler<ScrollEvent>{
	@Override
	public void handle(ScrollEvent event){
	    intervalSize*=Math.pow(1.005,event.getDeltaY());
	    magnification+= event.getDeltaY()>0 ? 1 : -1;
	    if (intervalSize < MIN_INTERVAL_SIZE){
		intervalSize*=DEFAULT_INTERVAL_SIZE/MIN_INTERVAL_SIZE;
		interval*=DEFAULT_INTERVAL_SIZE/MIN_INTERVAL_SIZE;
	    }
	    else if (intervalSize > MAX_INTERVAL_SIZE){
		intervalSize*=DEFAULT_INTERVAL_SIZE/MAX_INTERVAL_SIZE;
		interval*=DEFAULT_INTERVAL_SIZE/MAX_INTERVAL_SIZE;
	    }
	    reload();
	}
    }
    
    class CanvasMouseEventListener implements EventHandler<MouseEvent>{
	//needed for race condition of DRAG_DETECTED and MOUSE_DRAGGED events
	private boolean dragchecking=false;
	private boolean dragging = false;
	private double clickX, clickY;
    
	@Override
	public void handle(MouseEvent event) {
	    if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
		dragchecking=true;
		dragging = false;
		mouseFocus = null;
		clickX=event.getX(); clickY=event.getY();
	    }
	    else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
		if (mouseFocus==null ||
		    getSqDistance(event.getX(),event.getY(),corToPixel(mouseFocus.getX(),'x'),corToPixel(mouseFocus.getY(),'y')) > Math.pow(mouseFocus.getRadius(),2)){
		    boolean changedFocus=false;
		    for (DataPoint d: dataset){
			if (!changedFocus && getSqDistance(event.getX(),event.getY(),corToPixel(d.getX(),'x'),corToPixel(d.getY(),'y')) <= Math.pow(d.getRadius(),2)){
			    mouseFocus=d;
			    changedFocus=true;
			}
		    }
		}
		dragchecking=false;
	    }
	    else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
		if (!dragchecking){
		    dragging=true;
		    if (mouseFocus!=null){
			mouseFocus.setX(pixelToCor(event.getX(),'x')); mouseFocus.setY(pixelToCor(event.getY(),'y'));
			reload();
		    }
		    else{
			double deltaX=(clickX-event.getX()); double deltaY=(event.getY()-clickY);
			minX+=deltaX; minY+=deltaY;
			clickX=event.getX(); clickY=event.getY();
			reload();
		    }
		}
	    }
	    else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {//activates on mouse release
		if (!dragging) {
		    if (isSnapToGrid){ 
			dataset.add(new DataPoint(pixelToCor(Math.round(event.getX()/intervalSize)*intervalSize,'x'),
						  pixelToCor(Math.round(event.getY()/intervalSize)*intervalSize,'y')));
		    }
		    else {
			if (deleteOnClick){
			    for (DataPoint d: dataset){
				if (getSqDistance(event.getX(),event.getY(),corToPixel(d.getX(),'x'),corToPixel(d.getY(),'y')) <= Math.pow(d.getRadius(),2)){
				    mouseFocus=d;
				}
			    }
			    if (mouseFocus != null){
				dataset.remove(mouseFocus);
				mouseFocus=null;
			    }
			}
			else{
			    dataset.add(new DataPoint(pixelToCor(event.getX(),'x'),pixelToCor(event.getY(),'y')));
			}
		    }
		}
		statistics.updateStats(dataset);
		reload();
	    }
	}
    }
}
