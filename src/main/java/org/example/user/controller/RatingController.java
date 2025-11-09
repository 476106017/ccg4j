package org.example.user.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.SessionConstants;
import org.example.user.entity.UserAccount;
import org.example.user.service.RatingService;
import org.example.user.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    private final UserAccountService userAccountService;
    
    /**
     * 获取当前用户分数
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentRating(HttpSession session) {
        UserAccount user = requireUser(session);
        Integer rating = ratingService.getRating(user.getId());
        
        return ResponseEntity.ok(Map.of("rating", rating));
    }
    
    private UserAccount requireUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userAccountService.getById(userId);
    }
}
