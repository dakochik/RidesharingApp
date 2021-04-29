package org.app.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextInputControl;

public class DoubleLatLonValidator extends ValidatorBase {
    private final double minMax;

    public DoubleLatLonValidator(int minMax){
        super();
        this.minMax = minMax;
    }

    @Override
    protected void eval() {
        TextInputControl textField = (TextInputControl)this.srcControl.get();
        if(textField.getText() == null || textField.getText().isEmpty()){
            message = new SimpleStringProperty("Введите значение");
            this.hasErrors.set(true);
            textField.setText("");
            return;
        }

        double val;

        try{
           val = Double.parseDouble(textField.getText());
        }catch (NumberFormatException e){
            message = new SimpleStringProperty("Требуется дробное число");
            textField.setText("");
            this.hasErrors.set(true);
            return;
        }

        if((val > minMax) || (val < -minMax)){
            message = new SimpleStringProperty(String.format("-%s <= %s <= %s", minMax, (minMax == 90 ? "Долгота" : "Широта"), minMax));
            textField.setText("");
            this.hasErrors.set(true);
            return;
        }

        this.hasErrors.set(false);
    }
}
