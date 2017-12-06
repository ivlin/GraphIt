import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
public class DisplayValue{

    public final String property;
    public final ToggleButton button;
    public final Label label;
    public boolean isValid;
    public double value;

    
    public DisplayValue(String property, ToggleButton button, Label label, double value, boolean isValid){
	this.property=property;
	this.button=button;
	this.label=label;
	this.value=value;
	this.isValid=isValid;
    }

    public void toggleValid(){
	isValid=!isValid;
    }

    public void invalidate(){
	isValid=false;
    }

    public void validate(){
	isValid=true;
    }

    public void setValue(double d){
	value = d;
	setLabel(""+value);
    }
    
    public void setLabel(String s){
	label.setText(s);
    }
}
