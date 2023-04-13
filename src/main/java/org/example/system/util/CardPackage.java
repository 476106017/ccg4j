package org.example.system.util;

import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CardPackage {

    public static Class<? extends Card> randCard(String keyword){
        final List<Class<? extends Card>> classes = randCard(keyword, 1);
        if(classes.isEmpty())
            return null;
        return classes.get(0);
    }
    public static List<Class<? extends Card>> randCard(String keyword,int num){
        Set<Class<? extends Card>> subTypesOf =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(s -> s.contains(keyword))
                .forPackage("org.example.card"))
                .getSubTypesOf(Card.class);
        // 移除不符合的卡牌类型
        subTypesOf.removeIf(aClass ->{
            int modifiers = aClass.getModifiers();
            return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
        });

        if(subTypesOf.isEmpty())return new ArrayList<>();

        List<Class<? extends Card>> classes = new ArrayList<>(subTypesOf.stream().toList());
        Collections.shuffle(classes);
        return Lists.randOf(classes,num);
    }
}
