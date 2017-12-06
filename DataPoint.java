import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DataPoint{
    
    private static int totalPoints = 0;
    private static final double DEFAULT_POINT_RADIUS = 15;
    private double x,y,radius;
    private int orderAdded;
    
    private SimpleDoubleProperty xProperty=new SimpleDoubleProperty();
    private SimpleDoubleProperty yProperty=new SimpleDoubleProperty();
    private SimpleIntegerProperty orderAddedProperty=new SimpleIntegerProperty();

    /*
      CONSTRUCTORS
    */
    public DataPoint(double x, double y){
	this(x,y,DataPoint.DEFAULT_POINT_RADIUS);
    }
    public DataPoint(double x, double y, double radius){
	setX(x); setY(y); setRadius(radius); setOrderAdded(DataPoint.totalPoints);
	DataPoint.totalPoints++;
    }
    
    /*
      MUTATOR METHODS
     */
    public void setX(double x){
	this.x=x;
	xProperty.setValue(x);
    }
    public void setY(double y){
	this.y=y;
	yProperty.setValue(y);
    }
    public void setBirth(int time){
	this.orderAdded=time;
    }
    public void setRadius(double radius){
	this.radius=radius;
    }
    public void setOrderAdded(int orderAdded){
	this.orderAdded=orderAdded;
	orderAddedProperty.setValue(orderAdded);
    }

    /*
      ACCESSOR METHODS
     */
    public double getX(){
	return x;
    }
    public SimpleDoubleProperty xProperty(){
	return xProperty; //d;
    }
    
    public double getY(){
	return y;
    }
    public SimpleDoubleProperty yProperty(){
	return yProperty;
    }
    
    public double getOrderAdded(){
	return orderAdded;
    }
    public SimpleIntegerProperty orderAddedProperty(){
	return orderAddedProperty;
    }
    
    public double getRadius(){
	return radius;
    }
}
