package org.example.card.paripi.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
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
public class OutOfThinAir extends AmuletCard {

    public Integer cost = 1;

    public String name = "无中生有";
    public String job = "派对咖";
    private List<String> race = Lists.ofStr();

    public String mark = """
        亡语：随机获得3张卡
        """;
    public String subMark = "";
    public transient int countDown = 3;


    public OutOfThinAir() {

        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->{
            Set<Class<? extends Card>> subTypesOf =
                new Reflections(new ConfigurationBuilder()
                    .filterInputsBy(s -> !s.contains("genshin"))
                    .forPackage("org.example.card"))
                    .getSubTypesOf(Card.class);
            // 移除不符合的卡牌类型
            subTypesOf.removeIf(aClass ->{
                int modifiers = aClass.getModifiers();
                return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
            });
            if(subTypesOf.size()<3)
                info.msg("无法找到随机牌！");

            List<Class<? extends Card>> classes = new ArrayList<>(subTypesOf.stream().toList());
            Collections.shuffle(classes);

            ownerPlayer().addHand(List.of(createCard(classes.get(0)),createCard(classes.get(1)),createCard(classes.get(2))));
        }
        )));
    }

}
