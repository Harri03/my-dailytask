package com.queue.queuemanagement.controller;

import com.queue.queuemanagement.entity.QueueEntry;
import com.queue.queuemanagement.entity.QueueEntryResponse;
import com.queue.queuemanagement.service.QueueEntryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueEntryService queueEntryService;

    public QueueController(QueueEntryService queueEntryService) {
        this.queueEntryService = queueEntryService;
    }

    @PostMapping("/join")
    public Object joinQueue(@RequestParam String customerName) {
        return queueEntryService.addCustomer(customerName);
    }

    @GetMapping
    public java.util.List<QueueEntryResponse> getAllCustomers() {
        return queueEntryService.getAllCustomers();
    }

    @PutMapping("/{id}/call")
    public QueueEntry callCustomer(@PathVariable Long id) {
        return queueEntryService.callCustomer(id);
    }

    @PutMapping("/{id}/start")
    public QueueEntry startService(@PathVariable Long id) {
        return queueEntryService.startService(id);
    }

    @PutMapping("/{id}/complete")
    public QueueEntry completeCustomer(@PathVariable Long id) {
        return queueEntryService.completeCustomer(id);
    }

    @GetMapping("/average-service-time")
    public double getAverageServiceTime() {
        return queueEntryService.calculateAverageServiceTime();
    }

    @PutMapping("/{id}/late")
    public QueueEntry markCustomerLate(@PathVariable Long id) {
        return queueEntryService.markCustomerLate(id);
    }

    @PutMapping("/{id}/arrive")
    public QueueEntry arriveLateCustomer(@PathVariable Long id) {
        return queueEntryService.arriveLateCustomer(id);
    }
}