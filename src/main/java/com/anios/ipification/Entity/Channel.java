package com.anios.ipification.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "channels")
@Data
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status")
    private String status;

    @Column(name = "number", nullable = false)
    private String number;

    //@ManyToOne(fetch = FetchType.EAGER)
    //@JoinColumn(name = "deviceid")

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workflowId")
    private Workflow workflow;

}
