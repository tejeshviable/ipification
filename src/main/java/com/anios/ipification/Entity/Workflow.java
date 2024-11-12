package com.anios.ipification.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "workflow")
@Data
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "txn_id")
    private String txnId;

    @Column(name = "brand")
    private String brand;

    @Column(name = "final_channel")
    private String finalChannel;

    @Column(name = "status")
    private Boolean status;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Channel> channels;
}
