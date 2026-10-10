package com.queue.queuemanagement.entity;

public class QueueEntryResponse {

    private Long id;
    private int tokenNumber;
    private String customerName;
    private String status;
    private int queuePosition;
    private String estimatedWaitingTime;

    public QueueEntryResponse(
            Long id,
            int tokenNumber,
            String customerName,
            String status,
            int queuePosition,
            String estimatedWaitingTime) {

        this.id = id;
        this.tokenNumber = tokenNumber;
        this.customerName = customerName;
        this.status = status;
        this.queuePosition = queuePosition;
        this.estimatedWaitingTime = estimatedWaitingTime;
    }
    public Long getId() {
        return id;
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getStatus() {
        return status;
    }

    public int getQueuePosition() {
        return queuePosition;
    }
    public String getEstimatedWaitingTime() {
        return estimatedWaitingTime;
    }
}
