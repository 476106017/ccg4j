package org.example.card.ccg.festival.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class tbcz extends SpellCard {

   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 5;
    public String name = "跳吧，虫子！";
    public String job = "萨满";
    private List<String> race = Lists.ofStr("火焰");
    public String mark = """
        使一个随从变成炎魔之王，下回合开始时PP-2
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            ()->{
                // 创建一个新的可变列表，避免UnsupportedOperationException
                List<GameObj> _return = new ArrayList<>(enemyPlayer().getAreaFollowsAsGameObj());
                _return.addAll(ownerPlayer().getAreaFollowsAsGameObj());
                return _return;
            },true,
            obj -> {
                if(obj instanceof FollowCard followCard){
                    if(followCard.getOwner() == getOwner()){
                        getInfo().transform(followCard,createCard(ymzw.class));
                    }else {
                        getInfo().transform(followCard,createEnemyCard(ymzw.class));
                    }

                    ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.BeginTurn,3,()->{
                        int ppNum = ownerPlayer().getPpNum();
                        ownerPlayer().setPpNum(Math.max(ppNum-2,0));
                    }), false);
                }
            }));
    }

    @Getter
    @Setter
    public static class ymzw extends FollowCard {

        private CardRarity rarity = CardRarity.SILVER;
        public Integer cost = 8;
        public String name = "炎魔之王拉格纳罗斯";
        public String job = "中立";
        private List<String> race = Lists.ofStr("火焰");
        public String mark = """
            回合结束时：随机对一个敌人造成8点伤害
            """;

        public String subMark = "";

        public int atk = 8;
        public int hp = 8;

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("缴械");
            addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->{
                List<GameObj> list = new ArrayList<>();
                list.add(enemyLeader());
                list.addAll(enemyPlayer().getAreaFollows());
                info.damageEffect(this,Lists.randOf(list),8);
            })));
        }
    }
}
