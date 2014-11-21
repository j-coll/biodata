package org.opencb.biodata.models.variant.annotation;

/**
 * Created by fjlopez on 20/11/14.
 */
public class ExpressionValue {

    private String tissueName;
    private String experiment;
    private float value;

    public ExpressionValue(String tissueName, String experiment, Float value) {
        this.tissueName = tissueName;
        this.experiment = experiment;
        this.value = value;
    }

    public String getTissueName() {
        return tissueName;
    }

    public void setTissueName(String tissueName) {
        this.tissueName = tissueName;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
