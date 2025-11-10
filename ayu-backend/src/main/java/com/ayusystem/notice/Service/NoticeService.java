package com.ayusystem.notice.Service;

import com.ayusystem.notice.Model.Notice;
import com.ayusystem.notice.Repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class NoticeService {

    private final NoticeRepository repo;

    // Fixed thread pool for concurrent posting requests (Multithreading concept)
    private final ExecutorService pool =
            Executors.newFixedThreadPool(10); // Using a pool size of 10 [cite: 396-397]

    public NoticeService(NoticeRepository repo) {
        this.repo = repo;
    }

    /**
     * Submits a notice creation task to the thread pool asynchronously.
     * @param n The Notice object to be saved.
     * @return A Future object representing the pending completion of the task.
     */
    public Future<Notice> postNoticeAsync(Notice n) {
        // Task submitted to the thread pool [cite: 375, 402]
        return pool.submit(() -> {
            // Log the thread ID to demonstrate concurrency [cite: 117, 376, 384-385]
            System.out.println("Posting notice in thread: "
                    + Thread.currentThread().getId()
                    + " for notice titled: " + n.getTitle());

            // Ensure timestamp is set before saving
            if (n.getPostedAt() == null) {
                n.setPostedAt(LocalDateTime.now());
            }

            // Save the notice to the database via JPA
            return repo.save(n);
        });
    }

    /**
     * Retrieves all notices from the database.
     * @return A list of all Notice objects.
     */
    public List<Notice> getAll() {
        return repo.findAll(); // Simple JPA retrieval [cite: 410-411]
    }

    /**
     * Updates an existing notice. Uses save() which handles both insert and update.
     * @param n The Notice object with an existing ID to update.
     * @return The updated Notice object.
     */
    public Notice updateNotice(Notice n) {
        // Find the existing notice by ID (optional for robust check)
        if (n.getId() == null || !repo.existsById(n.getId())) {
            throw new IllegalArgumentException("Notice ID required for update.");
        }
        // Save performs the update if an ID is present
        return repo.save(n);
    }

    /**
     * Checks if a notice exists by ID.
     * @param id The ID to check.
     * @return true if the notice exists, false otherwise.
     */
    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    /**
     * Deletes a notice by its ID.
     * @param id The ID of the notice to delete.
     */
    public void deleteNotice(Long id) {
        repo.deleteById(id);
    }

    // Optional: Add a shutdown method for graceful termination
    public void shutdown() {
        pool.shutdown();
    }
}
