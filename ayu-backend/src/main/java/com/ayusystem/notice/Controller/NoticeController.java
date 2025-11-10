package com.ayusystem.notice.Controller;

import com.ayusystem.notice.Model.Notice;
import com.ayusystem.notice.Service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    // Dependency Injection of the NoticeService
    public NoticeController(NoticeService ns) {
        this.noticeService = ns;
    }

    /**
     * Handles POST requests to create a new notice.
     * Submits the task to the multithreaded pool and waits for completion.
     */
    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody Notice notice) throws Exception {
        // 1. Submit the notice creation task asynchronously [cite: 426-427]
        Future<Notice> future = noticeService.postNoticeAsync(notice);

        // 2. Wait for the asynchronous task to complete (blocking for a max of 10 seconds)
        Notice saved = future.get(10, TimeUnit.SECONDS); // Return success when task completes [cite: 385, 428-429]

        return ResponseEntity.ok(saved); // Return the saved notice
    }

    /**
     * Handles GET requests to list all notices.
     */
    @GetMapping
    public ResponseEntity<?> list() {
        // Retrieve all notices synchronously
        return ResponseEntity.ok(noticeService.getAll());
    }

    /**
     * Handles PUT requests to update an existing notice.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        // Ensure the ID in the path matches the body, and set the ID
        if (!id.equals(notice.getId())) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "ID mismatch."));
        }
        try {
            Notice updated = noticeService.updateNotice(notice);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            // Handles if ID is not found or null
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles DELETE requests to remove a notice.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        if (!noticeService.existsById(id)) {
            // Check existence before attempting to delete
            return ResponseEntity.notFound().build();
        }
        noticeService.deleteNotice(id);
        return ResponseEntity.ok().build(); // 200 OK with no content (or 204 No Content)
    }
}
