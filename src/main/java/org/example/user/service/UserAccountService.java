package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.entity.UserAccount;

import java.util.Optional;

public interface UserAccountService extends IService<UserAccount> {
    Optional<UserAccount> findByUsername(String username);

    void recordLogin(Long userId);

    void recordGameResult(Long userId, boolean isWin);

    UserAccount getPublicInfo(Long userId);
}
