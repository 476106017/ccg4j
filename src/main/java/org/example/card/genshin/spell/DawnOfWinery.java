package org.example.card.genshin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.genshin.LittlePrincess;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.card.genshin.system.Elemental;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class DawnOfWinery extends ElementCostSpellCard {
    public List<Elemental> elementCost = List.of(Elemental.Void,Elemental.Void);
    public String name = "晨曦酒庄";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public String mark = """
        召唤同名护符卡
        切换时：生成2个随机元素骰（每回合仅可发动1次）
        """;
    public String subMark = "";

    public DawnOfWinery() {
        setPlay(new Play(()-> ownerPlayer().summon(createCard(DawnOfWineryAmulet.class))));
    }


    @Getter
    @Setter
    public static class DawnOfWineryAmulet extends AmuletCard {

        public Integer cost = 3;

        public String name = "晨曦酒庄";
        public String job = "原神";
        private List<String> race = Lists.ofStr();

        public String mark = """
            切换时：生成2个随机元素骰（每回合仅可发动1次）
            """;
        public String subMark = "";

        public DawnOfWineryAmulet() {
            addEffects(new Effect(this,this, EffectTiming.BeginTurn,()->{
                clearCount();
            }));
            addEffects(new Effect(this,this, EffectTiming.WhenSwapChara,()->{
                if(getCount()==0){
                    count();

                    Leader leader = ownerLeader();
                    if(leader instanceof LittlePrincess littlePrincess){
                        littlePrincess.addDices(2);
                    }
                }
            }));

        }
    }
}