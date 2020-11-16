package com.datalogerphone.frank.dataloger_phone;

/**
 * Created by Esther on 10/20/20.
 */

public class SensorRecord {
    public SensorRecord(){
        this.sensor = new float[3];
    }

    public SensorRecord(long timestamp, float[] sensorValues){
        this.sensor = new float[3];
        this.timestamp = new Long(timestamp);
        this.sensor = sensorValues.clone();
    }

    public SensorRecord(long timestamp, float x, float y, float z){
        this.sensor = new float[3];
        this.timestamp = timestamp;
        this.sensor[0] = x;
        this.sensor[1] = y;
        this.sensor[2] = z;
    }

    protected void setSensor(float[] sensorValues){
        this.sensor = sensorValues;
    }

    protected float[] getSensor(){
        return this.sensor;
    }

    protected float getSensor(int index){
        return this.sensor[index];
    }

    protected void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    protected long getTimestamp(){
        return this.timestamp;
    }

	/*
	 * 	Instance variables
	 */

    private float[] sensor;
    private long timestamp;
}

