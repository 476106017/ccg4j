package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.entity.UserCardCollection;

import java.util.List;
import java.util.Map;

public interface UserCardCollectionService extends IService<UserCardCollection> {

    List<UserCardCollection> listByUserId(Long userId);

    void addCards(Long userId, Map<String, Integer> cardCounts);
    
    void removeCards(Long userId, Map<String, Integer> cardCounts);
}
