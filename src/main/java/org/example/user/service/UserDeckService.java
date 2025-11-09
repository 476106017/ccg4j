package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.entity.UserDeck;

import java.util.List;

public interface UserDeckService extends IService<UserDeck> {
    
    List<UserDeck> listByUserId(Long userId);
    
    UserDeck createDeck(Long userId, String deckName, String deckData);
    
    UserDeck updateDeck(Long id, Long userId, String deckName, String deckData);
    
    boolean deleteDeck(Long id, Long userId);
}
