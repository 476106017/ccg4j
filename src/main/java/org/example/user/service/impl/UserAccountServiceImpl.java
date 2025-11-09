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
}
