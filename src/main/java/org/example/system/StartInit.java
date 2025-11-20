package org.example.system;

import lombok.extern.slf4j.Slf4j;
import org.example.card.Card;
import org.example.card.data.CardDataLoader;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Set;

@Component
@Slf4j
public class StartInit implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        log.info("初始化卡牌数据库...");

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
        log.info("扫描到 {} 个Java类卡牌", subTypesOf.size());
        subTypesOf.forEach(Database::getPrototype);
        
        // 加载数据驱动卡牌
        log.info("开始加载数据驱动卡牌...");
        CardDataLoader.loadAllCardData();
        
        log.info("卡牌数据库初始化完成！");
        log.info("Java类卡牌: {} 张", subTypesOf.size());
        log.info("数据驱动卡牌: {} 张", CardDataLoader.getAllDataCardPrototypes().size());
    }
}
