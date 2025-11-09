package org.example.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.auth.SessionConstants;
import org.example.auth.dto.AuthRequest;
import org.example.auth.dto.AuthResponse;
import org.example.user.entity.UserAccount;
import org.example.user.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int NEW_USER_TICKETS = 10;

    private final UserAccountService userAccountService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request, HttpSession session) {
        userAccountService.findByUsername(request.getUsername())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已被使用");
                });

        OffsetDateTime now = OffsetDateTime.now();
        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setTickets(NEW_USER_TICKETS);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        userAccountService.save(account);

        bindSession(session, account);
        return toResponse(account);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        UserAccount user = userAccountService.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userAccountService.updateById(user);
        bindSession(session, user);
        return toResponse(user);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/status")
    public Map<String, Object> status(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("loggedIn", false);
            return result;
        }

        UserAccount user = userAccountService.getById(userId);
        if (user == null) {
            session.invalidate();
            result.put("loggedIn", false);
            return result;
        }

        result.put("loggedIn", true);
        result.put("user", toResponse(user));
        return result;
    }

    @GetMapping("/session")
    public AuthResponse session(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        String username = (String) session.getAttribute(SessionConstants.SESSION_USERNAME);
        UserAccount user = userAccountService.getById(userId);
        if (user == null) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return toResponse(user);
    }

    private void bindSession(HttpSession session, UserAccount account) {
        // 只在值变更时才设置，避免触发不必要的数据库写入
        Object existingUserId = session.getAttribute(SessionConstants.SESSION_USER_ID);
        Object existingUsername = session.getAttribute(SessionConstants.SESSION_USERNAME);
        
        if (!account.getId().equals(existingUserId)) {
            session.setAttribute(SessionConstants.SESSION_USER_ID, account.getId());
        }
        if (!account.getUsername().equals(existingUsername)) {
            session.setAttribute(SessionConstants.SESSION_USERNAME, account.getUsername());
        }
    }

    private AuthResponse toResponse(UserAccount account) {
        return AuthResponse.builder()
                .userId(account.getId())
                .username(account.getUsername())
                .tickets(account.getTickets())
                .arcaneDust(account.getArcaneDust())
                .matchRating(account.getMatchRating())
                .build();
    }
}