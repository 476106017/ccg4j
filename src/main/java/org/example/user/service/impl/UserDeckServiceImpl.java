package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.user.entity.UserDeck;
import org.example.user.mapper.UserDeckMapper;
import org.example.user.service.UserDeckService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserDeckServiceImpl extends ServiceImpl<UserDeckMapper, UserDeck>
    implements UserDeckService {

    @Override
    public List<UserDeck> listByUserId(Long userId) {
        return lambdaQuery()
            .eq(UserDeck::getUserId, userId)
            .orderByDesc(UserDeck::getUpdatedAt)
            .list();
    }

    @Override
    @Transactional
    public UserDeck createDeck(Long userId, String deckName, String deckData) {
        UserDeck deck = new UserDeck();
        deck.setUserId(userId);
        deck.setDeckName(deckName);
        deck.setDeckData(deckData == null ? "" : deckData); // 空数据默认为空字符串
        deck.setCreatedAt(OffsetDateTime.now());
        deck.setUpdatedAt(OffsetDateTime.now());
        save(deck);
        return deck;
    }

    @Override
    @Transactional
    public UserDeck updateDeck(Long id, Long userId, String deckName, String deckData) {
        UserDeck deck = lambdaQuery()
            .eq(UserDeck::getId, id)
            .eq(UserDeck::getUserId, userId)
            .one();
        
        if (deck == null) {
            return null;
        }
        
        System.out.println("更新卡组 - ID: " + id + ", 原数据: " + deck.getDeckData());
        
        if (deckName != null) {
            deck.setDeckName(deckName);
        }
        if (deckData != null) {
            deck.setDeckData(deckData);
            System.out.println("新数据: " + deckData);
        }
        deck.setUpdatedAt(OffsetDateTime.now());
        boolean success = updateById(deck);
        
        System.out.println("更新结果: " + success);
        
        return deck;
    }

    @Override
    @Transactional
    public boolean deleteDeck(Long id, Long userId) {
        return lambdaUpdate()
            .eq(UserDeck::getId, id)
            .eq(UserDeck::getUserId, userId)
            .remove();
    }
}
