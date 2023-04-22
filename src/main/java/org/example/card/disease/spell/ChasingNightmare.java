package org.example.card.disease.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class ChasingNightmare extends SpellCard {
    public Integer cost = 2;
    public String name = "追逐噩梦";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();
    public String mark = """
        如果是对方回合，且回合剩余时间在2分钟内，则对敌方主战者造成10点伤害
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()->{
            if(info.thisPlayer() == enemyPlayer() && info.getRope().getDelay(TimeUnit.SECONDS)>120){
                info.damageEffect(this,enemyLeader(),10);
            }
        }));
    }

}
