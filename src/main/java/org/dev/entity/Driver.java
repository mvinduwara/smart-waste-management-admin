package org.dev.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "drivers")
public class Driver implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false, unique = true)
    private String driver_username;

    @Column(length = 45, nullable = false, unique = true)
    private String password;

    @Column(length = 45, nullable = false, unique = true)
    private String contact;

    @Column(length = 45, nullable = false, unique = true)
    private String liscense_no;

    @Column(length = 45, nullable = false, unique = true)
    private String vehicle_type;

    @Column(length = 45, nullable = false, unique = true)
    private String vehicle_reg_no;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private String verification_status;
}
