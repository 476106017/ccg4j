package org.example.card.original.disease.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.Msg;

import java.util.List;

@Getter
@Setter
public class Epilepsy extends SpellCard {
    public Integer cost = 0;
    public String name = "癫痫";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();
    public String mark = """
        你的对手失去视野，持续10秒
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()->{
            new Thread(()->{
                Msg.send(enemyPlayer().getSession(), "clearBoard","");
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                info.pushInfo();
            }).start();
        }));
    }

}
