package com.steplabs.kafkaproducer.model;

public class DataRecord {
    public Long count;

    public DataRecord() {
    }

    public DataRecord(Long count) {
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public String toString() {
        return new com.google.gson.Gson().toJson(this);
    }

}
