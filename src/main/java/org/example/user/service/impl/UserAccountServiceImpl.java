package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.user.entity.UserAccount;
import org.example.user.mapper.UserAccountMapper;
import org.example.user.service.UserAccountService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
        implements UserAccountService {

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return lambdaQuery().eq(UserAccount::getUsername, username).oneOpt();
    }

    @Override
    public void recordLogin(Long userId) {
        UserAccount user = getById(userId);
        if (user == null)
            return;

        java.time.LocalDate today = java.time.LocalDate.now();
        if (user.getLastLoginDate() == null || !user.getLastLoginDate().equals(today)) {
            user.setLastLoginDate(today);
            user.setActivityScore((user.getActivityScore() == null ? 0 : user.getActivityScore()) + 10); // Daily Login
                                                                                                         // +10
            updateById(user);
        }
    }

    @Override
    public void recordGameResult(Long userId, boolean isWin) {
        UserAccount user = getById(userId);
        if (user == null)
            return;

        int scoreGain = 5; // Play game +5
        java.time.LocalDate today = java.time.LocalDate.now();

        if (isWin) {
            scoreGain += 5; // Win game +5
            if (user.getLastDailyWinDate() == null || !user.getLastDailyWinDate().equals(today)) {
                user.setLastDailyWinDate(today);
                scoreGain += 20; // First win of day +20
            }
        }

        user.setActivityScore((user.getActivityScore() == null ? 0 : user.getActivityScore()) + scoreGain);
        updateById(user);
    }

    @Override
    public UserAccount getPublicInfo(Long userId) {
        UserAccount user = getById(userId);
        if (user == null)
            return null;

        UserAccount publicInfo = new UserAccount();
        publicInfo.setId(user.getId());
        publicInfo.setUsername(user.getUsername());
        publicInfo.setMatchRating(user.getMatchRating());
        publicInfo.setActivityScore(user.getActivityScore());
        publicInfo.setLastLoginDate(user.getLastLoginDate());
        // Don't expose password or other sensitive info

        return publicInfo;
    }
}
