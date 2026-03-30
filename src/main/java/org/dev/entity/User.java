package org.dev.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false, unique = true)
    private String username;

    @Column(length = 45, nullable = false, unique = true)
    private String email;

    @Column(length = 200, nullable = false, unique = true)
    private String password;

    @Column(length = 10, nullable = false, unique = true)
    private String contact;

    @Column(name = "wallet_balance", columnDefinition = "DOUBLE DEFAULT 0.0")
    private double walletBalance = 0.0;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcm_token;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getFcm_token() { return fcm_token; }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public double getWalletBalance() {return walletBalance; }

    public void setWalletBalance(double walletBalance) {this.walletBalance = walletBalance; }
}
