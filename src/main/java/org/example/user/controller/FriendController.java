package org.example.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.system.util.Result;
import org.example.user.dto.FriendDto;
import org.example.user.service.FriendService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public Result<String> sendRequest(@RequestParam String username, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.sendFriendRequest(userId, username);
            return Result.success("Request sent");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/accept")
    public Result<String> acceptRequest(@RequestParam Long friendId, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.acceptFriendRequest(userId, friendId);
            return Result.success("Accepted");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reject")
    public Result<String> rejectRequest(@RequestParam Long friendId, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.rejectFriendRequest(userId, friendId);
            return Result.success("Rejected");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public Result<String> removeFriend(@RequestParam Long friendId, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.removeFriend(userId, friendId);
            return Result.success("Removed");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<FriendDto>> getList(HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        return Result.success(friendService.getFriendList(userId));
    }

    @PostMapping("/block")
    public Result<String> blockUser(@RequestParam Long blockId, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.blockUser(userId, blockId);
            return Result.success("Blocked");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/unblock")
    public Result<String> unblockUser(@RequestParam Long blockId, HttpSession session) {
        Long userId = (Long) session.getAttribute(org.example.auth.SessionConstants.SESSION_USER_ID);
        if (userId == null)
            return Result.error("Not logged in");

        try {
            friendService.unblockUser(userId, blockId);
            return Result.success("Unblocked");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<org.example.user.entity.UserAccount> getPlayerInfo(@RequestParam Long userId) {
        org.example.user.service.UserAccountService userService = org.example.system.ApplicationContextHelper
                .getBean(org.example.user.service.UserAccountService.class);
        org.example.user.entity.UserAccount user = userService.getPublicInfo(userId);
        if (user == null) {
            return Result.error("User not found");
        }
        return Result.success(user);
    }
}
