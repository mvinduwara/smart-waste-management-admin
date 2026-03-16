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
    private String username;

    @Column(length = 45, nullable = false, unique = true)
    private String password;

    @Column(length = 45, nullable = false, unique = true)
    private String contact;

    @Column(name = "license_number", length = 10)
    private String license_number;

    @Column(name = "vehicle_reg_no", length = 45)
    private String vehicle_reg_no;

    @Column(name = "vehicle_type", length = 45)
    private String vehicle_type;

    @ManyToOne
    @JoinColumn(name = "verification_status_id")
    private VerificationStatus verificationStatus;
}
