package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class RimefireBolt extends SpellCard {
    public Integer cost = 3;
    public String name = "霜火击";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对敌方场上一名随从造成3点伤害
        超杀：随机获得一张法师法术牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaFollowsAsGameObj(),true,
            gameObjs -> {
                info.damageEffect(this, gameObjs,3);
            }));
        addEffects((new Effect(this,this, EffectTiming.WhenKill,
            obj -> ((FollowCard) obj).getHp() < 0,
            obj -> {

                Set<Class<? extends Card>> subTypesOf =
                    new Reflections(new ConfigurationBuilder()
                        .filterInputsBy(s -> s.contains("mage.spell"))
                        .forPackage("org.example.card"))
                        .getSubTypesOf(Card.class);
                // 移除不符合的卡牌类型
                subTypesOf.removeIf(aClass ->{
                    int modifiers = aClass.getModifiers();
                    return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
                });
                if(subTypesOf.isEmpty())
                    info.msg("无法找到法师法术牌！");
                // 随机取30张
                List<Class<? extends Card>> classes = new ArrayList<>(subTypesOf.stream().toList());

                ownerPlayer().addHand(createCard(Lists.randOf(classes)));

            })));
    }

}
