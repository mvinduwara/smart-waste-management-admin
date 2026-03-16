package org.dev.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "verification_status")
public class VerificationStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus_type() {
        return status_type;
    }

    public void setStatus_type(String status_type) {
        this.status_type = status_type;
    }

    @Column(name = "status_type", length = 45)
    private String status_type;
}
