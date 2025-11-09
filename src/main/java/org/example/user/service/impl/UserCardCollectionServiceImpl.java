package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.user.entity.UserCardCollection;
import org.example.user.mapper.UserCardCollectionMapper;
import org.example.user.service.UserCardCollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserCardCollectionServiceImpl extends ServiceImpl<UserCardCollectionMapper, UserCardCollection>
    implements UserCardCollectionService {

    @Override
    public List<UserCardCollection> listByUserId(Long userId) {
        return lambdaQuery().eq(UserCardCollection::getUserId, userId).list();
    }

    @Override
    @Transactional
    public void addCards(Long userId, Map<String, Integer> cardCounts) {
        if (cardCounts == null || cardCounts.isEmpty()) {
            return;
        }
        List<UserCardCollection> existing = lambdaQuery()
            .eq(UserCardCollection::getUserId, userId)
            .in(UserCardCollection::getCardCode, cardCounts.keySet())
            .list();

        Map<String, UserCardCollection> existingByCode = existing.stream()
            .collect(Collectors.toMap(UserCardCollection::getCardCode, c -> c));

        Map<String, Integer> remaining = new HashMap<>(cardCounts);

        existingByCode.forEach((code, entity) -> {
            Integer add = remaining.remove(code);
            if (add != null && add > 0) {
                entity.setQuantity(entity.getQuantity() + add);
                updateById(entity);
            }
        });

        remaining.forEach((code, quantity) -> {
            if (quantity != null && quantity > 0) {
                UserCardCollection newRecord = new UserCardCollection();
                newRecord.setUserId(userId);
                newRecord.setCardCode(code);
                newRecord.setQuantity(quantity);
                save(newRecord);
            }
        });
    }
    
    @Override
    @Transactional
    public void removeCards(Long userId, Map<String, Integer> cardCounts) {
        if (cardCounts == null || cardCounts.isEmpty()) {
            return;
        }
        List<UserCardCollection> existing = lambdaQuery()
            .eq(UserCardCollection::getUserId, userId)
            .in(UserCardCollection::getCardCode, cardCounts.keySet())
            .list();
        
        existing.forEach(entity -> {
            Integer removeCount = cardCounts.get(entity.getCardCode());
            if (removeCount != null && removeCount > 0) {
                int newQuantity = entity.getQuantity() - removeCount;
                if (newQuantity <= 0) {
                    removeById(entity.getId());
                } else {
                    entity.setQuantity(newQuantity);
                    updateById(entity);
                }
            }
        });
    }
}
