package org.example.morecard.genshin.system;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Damage;
import org.example.game.DamageMulti;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ElementalDamage extends Damage {
    private Elemental element;

    public ElementalDamage(GameObj from, GameObj to, int damage, Elemental element) {
        super(from, to, damage);
        this.element = element;
    }

    public void apply() {
        Elemental cling = getTo().getElementalCling();
        List<ElementalDamage> moreDamage = new ArrayList<>();
        boolean clingToVoid = false;
        if (element == Elemental.Anemo) {
            if (cling.isActive()) {
                getTo().getInfo().msg("扩散！");
                getTo().ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != getTo())
                    .forEach(followCard ->
                        moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, cling)));
                setDamage(getDamage() + 1);
            }
        } else if (element == Elemental.Geo) {
            if (cling.isActive()) {
                getTo().getInfo().msg("结晶！");
                if (getFrom() instanceof FollowCard fromFollow) {
                    fromFollow.addKeywordN("格挡", 2);
                }
            }

        } else {
            switch (cling) {
                case Electro -> {// 雷附着
                    switch (element) {
                        case Hydro -> {
                            getTo().getInfo().msg("感电！");
                            getTo().ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != getTo())
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Electro)));
                        }
                        case Pydro -> {
                            getTo().getInfo().msg("超载！");
                            new Damage(getFrom(), getTo(), 2).apply();
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("守护");
                        }
                        case Cryo -> {
                            getTo().getInfo().msg("超导！");
                            getTo().ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != getTo())
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Void)));
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("护甲");
                        }
                    }
                }
                case Hydro -> {
                    switch (element) {
                        case Electro -> {
                            getTo().getInfo().msg("感电！");
                            getTo().ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != getTo())
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Electro)));
                        }
                        case Pydro -> {
                            getTo().getInfo().msg("蒸发！");
                            setDamage(getDamage() * 2);
                        }
                        case Cryo -> {
                            getTo().getInfo().msg("缴械！");
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.addKeywordN("眩晕", 2);
                        }
                    }
                }
                case Pydro -> {
                    switch (element) {
                        case Electro -> {
                            getTo().getInfo().msg("超载！");
                            new Damage(getFrom(), getTo(), 2).apply();
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("守护");
                        }
                        case Hydro -> {
                            getTo().getInfo().msg("蒸发（反向）！");
                            setDamage((int)(getDamage()*1.5));
                        }
                        case Cryo -> {
                            getTo().getInfo().msg("融化！");
                            setDamage(getDamage() * 2);
                        }
                    }
                }
                case Cryo -> {
                    switch (element) {
                        case Electro -> {
                            getTo().getInfo().msg("超导！");
                            getTo().ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != getTo())
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Void)));
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("护甲");
                        }
                        case Hydro -> {
                            getTo().getInfo().msg("缴械！");
                            if (getTo() instanceof FollowCard toFollow)
                                toFollow.addKeywordN("眩晕", 2);
                        }
                        case Pydro -> {
                            getTo().getInfo().msg("融化（反向）！");
                            setDamage((int)(getDamage()*1.5));
                        }
                    }
                }
                case Void -> {
                    clingToVoid =true;
                    getTo().getInfo().msg("附着"+element.getStr()+"！");
                    getTo().setElementalCling(element);
                }
            }
        }
        if(!clingToVoid)
            getTo().setElementalCling(Elemental.Void);

        new DamageMulti(getTo().getInfo(), List.of(this)).apply();

        moreDamage.forEach(Damage::apply);
    }
}
