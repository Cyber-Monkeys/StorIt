package com.example.storit;

import android.os.Parcelable;

import java.util.Date;

public class User {
    String username;
    String email;
    String name;
    Date dateOfBirth;
    String region;
    Plan plan;
    String privateKey;
    String publicKey;

    public User(String username, String email, String name, Date dateOfBirth, String region) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.region = region;
    }
    public User(String username, String email, String name, Date dateOfBirth, String region, Plan plan) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.region = region;
        this.plan = plan;
    }


    public User(String username, String email, String name, Date dateOfBirth, String region,
                String privateKey, String publicKey) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.region = region;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public User(String username, String email, String name, Date dateOfBirth, String region, Plan plan,
                String privateKey, String publicKey) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.region = region;
        this.plan = plan;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
