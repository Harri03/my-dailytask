package com.queue.queuemanagement.repository;

import com.queue.queuemanagement.entity.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    QueueEntry findTopByOrderByTokenNumberDesc();

    java.util.List<QueueEntry> findByStatus(String status);

    QueueEntry findFirstByStatus(String status);
}