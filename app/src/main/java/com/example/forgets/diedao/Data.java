package com.example.forgets.diedao;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by m199 on 2017/3/15.
 */

public class Data extends DataSupport{
    private String x;
    private String y;
    private String z;
    private String acceleration;
    private double type;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public String getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(String acceleration) {
        this.acceleration = acceleration;
    }

    public double getType() {
        return type;
    }

    public void setType(double type) {
        this.type = type;
    }
}
