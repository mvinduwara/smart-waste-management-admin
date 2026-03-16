package org.dev.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "status")
public class verification_status implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "status_name", length = 45, nullable = false, unique = true)
    private String verification_status;
}
