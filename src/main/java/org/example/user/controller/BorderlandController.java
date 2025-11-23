package org.example.user.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.SessionConstants;
import org.example.user.entity.BorderlandVisa;
import org.example.user.entity.UserAccount;
import org.example.user.service.BorderlandService;
import org.example.user.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/borderland")
@RequiredArgsConstructor
public class BorderlandController {

    private final BorderlandService borderlandService;
    private final UserAccountService userAccountService;

    /**
     * 获取签证状态
     */
    @GetMapping("/visa/status")
    public ResponseEntity<?> getVisaStatus(HttpSession session) {
        UserAccount user = requireUser(session);
        BorderlandVisa visa = borderlandService.getVisaStatus(user.getId());

        if (visa == null) {
            return ResponseEntity.ok(Map.of());
        }

        return ResponseEntity.ok(visa);
    }

    /**
     * 办理签证
     */
    @PostMapping("/visa/apply")
    public ResponseEntity<?> applyVisa(HttpSession session) {
        try {
            UserAccount user = requireUser(session);
            BorderlandVisa visa = borderlandService.applyVisa(user.getId());
            return ResponseEntity.ok(visa);
        } catch (Exception e) {
            log.error("办理签证失败", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 丢弃卡牌
     */
    @PostMapping("/deck/discard")
    public ResponseEntity<?> discardCard(
            HttpSession session,
            @RequestBody Map<String, String> request) {
        try {
            UserAccount user = requireUser(session);
            String cardCode = request.get("cardCode");
            BorderlandVisa visa = borderlandService.discardCard(user.getId(), cardCode);
            return ResponseEntity.ok(visa);
        } catch (Exception e) {
            log.error("丢弃卡牌失败", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 带出卡牌
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportCard(
            HttpSession session,
            @RequestBody Map<String, String> request) {
        try {
            UserAccount user = requireUser(session);
            String cardCode = request.get("cardCode");
            borderlandService.exportCard(user.getId(), cardCode);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("带出卡牌失败", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private UserAccount requireUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userAccountService.getById(userId);
    }
}
