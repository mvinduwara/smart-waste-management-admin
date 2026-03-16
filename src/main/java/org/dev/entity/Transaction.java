package org.dev.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "pickup_requests_id")
    private PickupRequest pickupRequest;

    @Column(name = "payment_token", length = 45)
    private String payment_token;

    private Double amount_paid;

    private Timestamp timestamp;
}
