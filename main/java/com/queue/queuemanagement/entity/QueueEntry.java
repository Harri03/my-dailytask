package com.queue.queuemanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int tokenNumber;

    private String customerName;

    private String status;

    private java.time.LocalDateTime serviceStartTime;

    private Long serviceDurationMinutes;

    private boolean lateCustomerArrived;

    public QueueEntry() {
    }

    public QueueEntry(int tokenNumber, String customerName, String status) {
        this.tokenNumber = tokenNumber;
        this.customerName = customerName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public java.time.LocalDateTime getServiceStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(java.time.LocalDateTime serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public Long getServiceDurationMinutes() {
        return serviceDurationMinutes;
    }

    public void setServiceDurationMinutes(Long serviceDurationMinutes) {
        this.serviceDurationMinutes = serviceDurationMinutes;
    }

    public boolean isLateCustomerArrived() {
        return lateCustomerArrived;
    }

    public void setLateCustomerArrived(boolean lateCustomerArrived) {
        this.lateCustomerArrived = lateCustomerArrived;
    }
}