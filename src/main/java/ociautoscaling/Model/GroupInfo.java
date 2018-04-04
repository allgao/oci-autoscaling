package ociautoscaling.Model;

import java.io.Serializable;

public class GroupInfo implements Serializable {
    private String groupName;
    private int regularRunning;
    private int auxiliaryRunning;
    private int auxiliaryStaring;
    private int auxiliaryStopping;
    private int auxiliaryStopped;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getRegularRunning() {
        return regularRunning;
    }

    public void setRegularRunning(int regularRunning) {
        this.regularRunning = regularRunning;
    }

    public int getAuxiliaryRunning() {
        return auxiliaryRunning;
    }

    public void setAuxiliaryRunning(int auxiliaryRunning) {
        this.auxiliaryRunning = auxiliaryRunning;
    }

    public int getAuxiliaryStaring() {
        return auxiliaryStaring;
    }

    public void setAuxiliaryStaring(int auxiliaryStaring) {
        this.auxiliaryStaring = auxiliaryStaring;
    }

    public int getAuxiliaryStopping() {
        return auxiliaryStopping;
    }

    public void setAuxiliaryStopping(int auxiliaryStopping) {
        this.auxiliaryStopping = auxiliaryStopping;
    }

    public int getAuxiliaryStopped() {
        return auxiliaryStopped;
    }

    public void setAuxiliaryStopped(int auxiliaryStopped) {
        this.auxiliaryStopped = auxiliaryStopped;
    }

    public void addRegular() {
        this.regularRunning += 1;
    }

    public void addAuxiliaryRunning() {
        this.auxiliaryRunning += 1;
    }

    public void addAuxiliaryStarting() {
        this.auxiliaryStaring += 1;
    }

    public void addAuxiliaryStopping() {
        this.auxiliaryStopping += 1;
    }

    public void addAuxiliaryStopped() {
        this.auxiliaryStopped += 1;
    }

}
