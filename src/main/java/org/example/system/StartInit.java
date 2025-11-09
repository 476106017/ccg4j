package org.example.system;

import org.example.card.Card;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Set;

@Component
public class StartInit implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {

        Set<Class<? extends Card>> subTypesOf =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(s -> !s.contains("morecard"))
                .forPackage("org.example.card"))
                .getSubTypesOf(Card.class);
        // 移除不符合的卡牌类型
        subTypesOf.removeIf(aClass ->{
            int modifiers = aClass.getModifiers();
            return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
        });
        // 初始化原型
        subTypesOf.forEach(Database::getPrototype);

    }
}
