package org.openmhealth.data.generator.domain;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.xml.crypto.Data;

/**
 * for sample-data-generator
 *
 * @author just on 2017/12/11.
 */
public class DataTrend {
    private String shape;
    private Double startMoment;
    private Double endMoment;
    private Double startValue;
    private Double endValue;

    public DataTrend(){}

    public DataTrend(Double startValue,Double endValue){
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public DataTrend(String shape, Double startMoment, Double endMoment, Double startValue, Double endValue) {
        this.shape = shape;
        this.startMoment = startMoment;
        this.endMoment = endMoment;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public Double getStartMoment() {
        return startMoment;
    }

    public void setStartMoment(Double startMoment) {
        this.startMoment = startMoment;
    }

    public Double getEndMoment() {
        return endMoment;
    }

    public void setEndMoment(Double endMoment) {
        this.endMoment = endMoment;
    }

    @NotNull
    public Double getStartValue() {
        return startValue;
    }

    public void setStartValue(Double startValue) {
        this.startValue = startValue;
    }

    @NotNull
    public Double getEndValue() {
        return endValue;
    }

    public void setEndValue(Double endValue) {
        this.endValue = endValue;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();

        sb.append("shape : ").append(shape)
                .append("startMoment : ").append(startMoment)
                .append("endMoment : ").append(endMoment)
                .append("startValue : ").append(startValue)
                .append("endValue : ").append(endValue);
        return sb.toString();
    }

}
