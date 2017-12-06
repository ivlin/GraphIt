import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public class StatisticsGroup{

    private HashMap<String, DisplayValue> statistics;
    private boolean isSample=false;
    private ScriptEngine engine;
    
    public StatisticsGroup(){
	statistics=new HashMap<>();    
	initStatistic("mean");
	initStatistic("median");
	initStatistic("median1");//when count is even
	initStatistic("median2");//when count is even
	initStatistic("standard deviation");
	initStatistic("regression line");
	initStatistic("slope");
	initStatistic("intercept");
	initStatistic("covariance");
	initStatistic("coefficient of determination");
	engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
    }

    public DisplayValue get(String statistic){
	return statistics.get(statistic);
    }

    public HashMap<String, DisplayValue> getStatistics(){
	return statistics;
    }

    private void initStatistic(String statistic){
	statistics.put(statistic, new DisplayValue(statistic, new ToggleButton(statistic), new Label("NaN"), Double.NaN, false));
    }

    public void updateStats(List<DataPoint> dataset){
	ArrayList<DataPoint> dataCpy = new ArrayList<>(dataset);
	if (dataset.size()>0){
	    Collections.sort(dataCpy, (x,y)->{
		    return x.getX()>= y.getX() ? 1 : -1;
		});
	
	    double sumX, sumXSq, sumY, sumYSq, sumXY, sumXSqDev, sumYSqDev, sumXYDev;
	    sumX= sumXSq= sumY= sumYSq= sumXY= sumXSqDev= sumYSqDev= sumXYDev= 0;
	
	    for (DataPoint d: dataCpy){
		sumX+=d.getX(); sumXSq+=d.getX()*d.getX();
		sumY+=d.getY(); sumYSq+=d.getY()*d.getY();
		sumXY+=d.getX()*d.getY();
	    }
	    for (DataPoint d: dataCpy){
		sumXSqDev+=(d.getX()-sumX/dataset.size())*(d.getX()-sumX/dataset.size());
		sumYSqDev+=(d.getY()-sumY)*(d.getY()-sumY)/dataset.size();
		sumXYDev+=(d.getX()-sumX/dataset.size())*(d.getY()-sumY/dataset.size());
	    }	
	
	    statistics.get("mean").setValue(sumX/ dataset.size());
	    statistics.get("median").setValue(dataset.size()%2!=0 ?
					      dataCpy.get(dataset.size()/2).getX() :
					      (dataCpy.get(dataset.size()/2-1).getX() + dataCpy.get(dataset.size()/2).getX())/2);
	    if (dataset.size()%2!=0){
		statistics.get("median").setValue(dataCpy.get(dataset.size()/2).getX());
		statistics.get("median1").setValue(Double.NaN);
		statistics.get("median2").setValue(Double.NaN);
	    }
	    else{
		statistics.get("median").setValue((dataCpy.get(dataset.size()/2-1).getX() + dataCpy.get(dataset.size()/2).getX())/2);
		statistics.get("median1").setValue(dataCpy.get(dataset.size()/2-1).getX());
		statistics.get("median2").setValue(dataCpy.get(dataset.size()/2).getX());       
	    }
	    statistics.get("intercept").setValue((sumY*sumXSq-sumX*sumXY)/(dataset.size()*sumXSq-sumX*sumX));
	    statistics.get("slope").setValue((dataset.size()*sumXY-sumX*sumY)/(dataset.size()*sumXSq-sumX*sumX));
	    statistics.get("regression line").setLabel("y="+statistics.get("slope").value+"x+"+statistics.get("slope").value);
	    double r=(dataset.size()*sumXY-sumX*sumY)/Math.sqrt((dataset.size()*sumXSq-sumX*sumX)*(dataset.size()*sumYSq-sumY*sumY));
	    statistics.get("coefficient of determination").setValue(r*r);
	    statistics.get("covariance").setValue(sumXYDev/(dataset.size()-1));	
	    statistics.get("standard deviation").setValue(isSample ? Math.sqrt(sumXSqDev/(dataset.size()-1)) : Math.sqrt(sumXSqDev/(dataset.size())));
	}
	else{
	    for (HashMap.Entry<String,DisplayValue> d : statistics.entrySet()){
		d.getValue().setValue(Double.NaN);;
	    }
	}
    }

    public HashMap<Double, Double> partition(List<DataPoint> dataset, double partitionSize){
	if (dataset.size()<=0){
	    return null;
	}
	HashMap<Double, Double> parts = new HashMap<>();
	DataPoint minX, maxX;
	minX=maxX=dataset.get(0);
	for (DataPoint d: dataset){
	    if (d.getX()<minX.getX()){
		minX=d;
	    }
	    if (d.getX()>maxX.getX()){
		maxX=d;
	    }
	}
	double minStart = Math.floor(minX.getX()/partitionSize)*partitionSize;
	double maxStart = Math.ceil(maxX.getX()/partitionSize)*partitionSize;
	for (double i = minStart; i<maxStart; i+=partitionSize){
	    parts.put(i, 0.0);
	}
	double ind;
	for (DataPoint d: dataset){
	    ind = Math.floor(d.getX()/partitionSize)*partitionSize;
	    parts.put(ind,parts.get(ind)+1);
	}
	return parts;
    }

    public double eval(String expression){
	try{
	    return Double.parseDouble(""+engine.eval(expression));
	} catch (ScriptException|NumberFormatException e){
	    return Double.NaN;
	}
    }
}
