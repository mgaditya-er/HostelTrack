package com.example.hosteltrack;

public class Student {
    private String uuid;
    private String name;

    public Student() {
        // Required empty public constructor for Firestore
    }

    public Student(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}

