package com.example.hosteltrack;

public class Student {
    private String uuid;
    private String name;
    private String contactNumber; // New field for the contact number

    // Required empty public constructor for Firestore
    public Student() {
    }

    // Updated constructor to include the contact number
    public Student(String uuid, String name, String contactNumber) {
        this.uuid = uuid;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    // Getter for UUID
    public String getUuid() {
        return uuid;
    }

    // Getter for Name
    public String getName() {
        return name;
    }

    // New getter for the contact number
    public String getContactNumber() {
        return contactNumber;
    }

    // If needed, you can also add setters for these fields.
    // For example, a setter for the contact number:
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Similarly, add setters for other fields if necessary
}
