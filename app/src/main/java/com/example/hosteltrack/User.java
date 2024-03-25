package com.example.hosteltrack;

public class User {
    private String username;
    private String email;
    private String phone;
    private String role;
    private String enrnumber; // New field for the user's enrollment number

    public User() {
        // Default constructor required for Firestore
    }



    public User(String username, String email,String enrnumber, String phone, String role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.enrnumber = enrnumber;
    }

    // Getter and setter for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter and setter for role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getter and setter for enrollment number
    public String getEnrnumber() {
        return enrnumber;
    }

    public void setEnrnumber(String enrnumber) {
        this.enrnumber = enrnumber;
    }
}
