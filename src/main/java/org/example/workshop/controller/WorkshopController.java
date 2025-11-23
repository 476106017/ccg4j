package org.example.workshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.workshop.entity.WorkshopCard;
import org.example.workshop.service.WorkshopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import org.example.auth.SessionConstants;

@RestController
@RequestMapping("/api/workshop")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<Map<String, Object>>> getCards(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(workshopService.getCards(status, sortBy));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<Map<String, Object>> getCardDetail(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        return ResponseEntity.ok(workshopService.getCardDetail(id, userId));
    }

    @PostMapping("/cards")
    public ResponseEntity<String> createCard(@RequestBody WorkshopCard card, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        workshopService.createCard(card, userId);
        return ResponseEntity.ok("提交成功");
    }

    @PostMapping("/cards/{id}/vote")
    public ResponseEntity<String> voteCard(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        workshopService.voteCard(id, userId);
        return ResponseEntity.ok("操作成功");
    }

    @PostMapping("/cards/{id}/comment")
    public ResponseEntity<String> addComment(@PathVariable Long id, @RequestBody Map<String, String> body,
            HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("评论内容不能为空");
        }
        workshopService.addComment(id, content, userId);
        return ResponseEntity.ok("评论成功");
    }

    @PostMapping("/cards/{id}/implement")
    public ResponseEntity<String> implementCard(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        workshopService.implementCard(id, userId);
        return ResponseEntity.ok("实装成功");
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long id, HttpSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        workshopService.deleteCard(id, userId);
        return ResponseEntity.ok("撤回成功");
    }
}
