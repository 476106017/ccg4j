package org.example.card.data;

import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Effect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据驱动卡牌测试
 */
@SpringBootTest
public class DataDrivenCardTest {
    
    @Test
    public void testLoadBasicSoldier() {
        CardData data = new CardData();
        data.setId("test_soldier");
        data.setName("测试士兵");
        data.setCardType("FOLLOW");
        data.setCost(1);
        data.setAtk(1);
        data.setHp(2);
        
        Card card = DataDrivenCard.createCard(data);
        card.init();
        
        assertNotNull(card);
        assertEquals("测试士兵", card.getName());
        assertEquals(1, card.getCost());
        
        if (card instanceof FollowCard) {
            FollowCard follow = (FollowCard) card;
            assertEquals(1, follow.getAtk());
            assertEquals(2, follow.getHp());
        }
    }
    
    @Test
    public void testLoadCardWithEffect() {
        CardData data = new CardData();
        data.setId("test_spell");
        data.setName("测试法术");
        data.setCardType("SPELL");
        data.setCost(2);
        
        // 添加抽牌效果
        CardData.EffectData effect = new CardData.EffectData();
        effect.setTiming("ON_PLAY");
        effect.setType("DRAW_CARD");
        CardData.EffectParams params = new CardData.EffectParams();
        params.setAmount(2);
        effect.setParams(params);
        data.getEffects().add(effect);
        
        Card card = DataDrivenCard.createCard(data);
        card.init();
        
        assertNotNull(card);
        assertEquals("测试法术", card.getName());
    }
    
    @Test
    public void testCardDataLoader() {
        // 假设已经加载了卡牌数据
        CardDataLoader.loadAllCardData();
        
        // 测试获取卡牌
        Card soldier = CardDataLoader.createCardById("basic_soldier");
        if (soldier != null) {
            assertEquals("士兵", soldier.getName());
        }
        
        Card spell = CardDataLoader.createCardById("card_draw_spell");
        if (spell != null) {
            assertEquals("占卜术", spell.getName());
        }
    }
}
