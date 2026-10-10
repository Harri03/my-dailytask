package com.queue.queuemanagement.service;

import com.queue.queuemanagement.entity.QueueEntry;
import com.queue.queuemanagement.entity.QueueEntryResponse;
import com.queue.queuemanagement.repository.QueueEntryRepository;
import org.springframework.stereotype.Service;

@Service
public class QueueEntryService {

    private final QueueEntryRepository queueEntryRepository;

    public QueueEntryService(QueueEntryRepository queueEntryRepository) {
        this.queueEntryRepository = queueEntryRepository;
    }

    // Add a new customer to the queue
    public QueueEntry addCustomer(String customerName) {

        QueueEntry lastEntry =
                queueEntryRepository.findTopByOrderByTokenNumberDesc();

        int nextToken = 1;

        if (lastEntry != null) {
            nextToken = lastEntry.getTokenNumber() + 1;
        }

        QueueEntry queueEntry = new QueueEntry();

        queueEntry.setTokenNumber(nextToken);
        queueEntry.setCustomerName(customerName);
        queueEntry.setStatus("WAITING");

        return queueEntryRepository.save(queueEntry);
    }


    // Get all customers with queue position and estimated waiting time
    public java.util.List<QueueEntryResponse> getAllCustomers() {

        java.util.List<QueueEntry> customers =
                queueEntryRepository.findAll();

        // Sort customers according to queue priority
        customers.sort((customer1, customer2) -> {

            int priority1 = getQueuePriority(customer1.getStatus());
            int priority2 = getQueuePriority(customer2.getStatus());

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            // If same status, keep older token first
            return Integer.compare(
                    customer1.getTokenNumber(),
                    customer2.getTokenNumber()
            );
        });

        java.util.List<QueueEntryResponse> response =
                new java.util.ArrayList<>();

        // Average service time is stored/calculated in minutes
        double averageServiceTimeMinutes =
                calculateAverageServiceTime();

        // Convert minutes to seconds for waiting-time calculation
        double averageServiceTimeSeconds =
                averageServiceTimeMinutes * 60;

        QueueEntry servingCustomer =
                queueEntryRepository.findFirstByStatus("SERVING");

        double remainingServiceTime = 0;

        // Calculate remaining time of current serving customer
        if (servingCustomer != null) {

            long elapsedTime =
                    java.time.Duration.between(
                            servingCustomer.getServiceStartTime(),
                            java.time.LocalDateTime.now()
                    ).getSeconds();

            remainingServiceTime =
                    Math.max(
                            0,
                            averageServiceTimeSeconds - elapsedTime
                    );
        }

        int position = 0;

        for (QueueEntry customer : customers) {

            long estimatedWaitingTime = 0;

            // Completed customers are not part of the active queue
            if (customer.getStatus().equals("COMPLETED")) {

                estimatedWaitingTime = 0;
            }

            // Current serving customer
            else if (customer.getStatus().equals("SERVING")) {

                position = 1;

                estimatedWaitingTime =
                        (long) remainingServiceTime;
            }

            // Late customer gets the next turn
            else if (customer.getStatus().equals("LATE")) {

                position++;

                if (servingCustomer != null) {

                    estimatedWaitingTime =
                            (long) remainingServiceTime;

                } else {

                    estimatedWaitingTime = 0;
                }
            }

            // Normal waiting customer
            else {

                position++;

                if (servingCustomer != null) {

                    estimatedWaitingTime =
                            (long) (
                                    remainingServiceTime
                                            + (position - 1)
                                            * averageServiceTimeSeconds
                            );

                } else {

                    estimatedWaitingTime =
                            (long) (
                                    (position - 1)
                                            * averageServiceTimeSeconds
                            );
                }
            }

            // Prevent negative waiting time
            estimatedWaitingTime =
                    Math.max(0, estimatedWaitingTime);

            long minutes =
                    estimatedWaitingTime / 60;

            long seconds =
                    estimatedWaitingTime % 60;

            String estimatedWaitingTimeText =
                    minutes + " min " + seconds + " sec";

            int customerPosition = position;

            // Completed customer should have position 0
            if (customer.getStatus().equals("COMPLETED")) {

                customerPosition = 0;
            }

            QueueEntryResponse customerResponse =
                    new QueueEntryResponse(
                            customer.getId(),
                            customer.getTokenNumber(),
                            customer.getCustomerName(),
                            customer.getStatus(),
                            customerPosition,
                            estimatedWaitingTimeText
                    );

            response.add(customerResponse);
        }

        return response;
    }


    // Define the priority of each queue status
    private int getQueuePriority(String status) {

        if (status.equals("SERVING")) {
            return 1;
        }

        if (status.equals("LATE")) {
            return 2;
        }

        if (status.equals("WAITING")) {
            return 3;
        }

        return 4;
    }


    // Calculate average service time in minutes
    public double calculateAverageServiceTime() {

        java.util.List<QueueEntry> completedCustomers =
                queueEntryRepository.findByStatus("COMPLETED");

        // If there are no completed customers,
        // assume 10 minutes initially
        if (completedCustomers.isEmpty()) {
            return 10;
        }

        long totalDuration = 0;

        for (QueueEntry customer : completedCustomers) {

            totalDuration +=
                    customer.getServiceDurationMinutes();
        }

        return (double) totalDuration
                / completedCustomers.size();
    }


    // Call a customer
    public QueueEntry callCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            customer.setStatus("CALLED");

            return queueEntryRepository.save(customer);
        }

        return null;
    }


    // Mark a customer as late
    public QueueEntry markCustomerLate(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            customer.setStatus("LATE");

            return queueEntryRepository.save(customer);
        }

        return null;
    }


    // Mark a late customer as ready to be served
    public QueueEntry arriveLateCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            // Check if another customer is currently being served
            QueueEntry servingCustomer =
                    queueEntryRepository.findFirstByStatus("SERVING");

            if (servingCustomer != null) {

                // Keep late customer waiting
                customer.setStatus("LATE");

                // Remember that the late customer has arrived
                customer.setLateCustomerArrived(true);

            } else {

                // No customer is currently being served,
                // so the late customer can start serving
                customer.setStatus("SERVING");

                customer.setServiceStartTime(
                        java.time.LocalDateTime.now()
                );
            }

            return queueEntryRepository.save(customer);
        }

        return null;
    }


    // Start serving a customer
    public QueueEntry startService(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            customer.setStatus("SERVING");

            customer.setServiceStartTime(
                    java.time.LocalDateTime.now()
            );

            return queueEntryRepository.save(customer);
        }

        return null;
    }


    // Complete a customer's service
    public QueueEntry completeCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            java.time.LocalDateTime endTime =
                    java.time.LocalDateTime.now();

            // Calculate actual service duration in minutes
            long duration =
                    java.time.Duration.between(
                            customer.getServiceStartTime(),
                            endTime
                    ).toMinutes();

            // Store service duration in minutes
            customer.setServiceDurationMinutes(duration);

            customer.setStatus("COMPLETED");


            // Find a late customer who has already arrived
            java.util.List<QueueEntry> lateCustomers =
                    queueEntryRepository.findByStatus("LATE");

            for (QueueEntry lateCustomer : lateCustomers) {

                if (lateCustomer.isLateCustomerArrived()) {

                    // Automatically give the next turn
                    lateCustomer.setStatus("SERVING");

                    lateCustomer.setServiceStartTime(
                            java.time.LocalDateTime.now()
                    );

                    lateCustomer.setLateCustomerArrived(false);

                    queueEntryRepository.save(lateCustomer);

                    break;
                }
            }

            return queueEntryRepository.save(customer);
        }

        return null;
    }
}