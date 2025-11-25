package com.example.e_commerceapplication;

public class User {
    private String UID;
    private String email;
    private String role;

    public User(){

    }
    public User(String UID, String email, String role) {
        this.UID = UID;
        this.email = email;
        this.role = role;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
