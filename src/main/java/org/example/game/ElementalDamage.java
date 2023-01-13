package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.genshin.Elemental;

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
        Elemental cling = to.getElementalCling();
        List<ElementalDamage> moreDamage = new ArrayList<>();
        if (element == Elemental.Anemo) {
            if (cling.isActive()) {
                to.getInfo().msg("扩散！");
                to.ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != to)
                    .forEach(followCard ->
                        moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, cling)));
                setDamage(getDamage() + 1);
            }
        } else if (element == Elemental.Geo) {
            if (cling.isActive()) {
                to.getInfo().msg("结晶！");
                if (getFrom() instanceof FollowCard fromFollow) {
                    fromFollow.addKeywordN("格挡", 2);
                }
            }

        } else {
            switch (cling) {
                case Electro -> {// 雷附着
                    switch (element) {
                        case Hydro -> {
                            to.getInfo().msg("感电！");
                            to.ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != to)
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Electro)));
                        }
                        case Pydro -> {
                            to.getInfo().msg("超载！");
                            new Damage(from, to, 2);
                            if (to instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("守护");
                        }
                        case Cryo -> {
                            to.getInfo().msg("超导！");
                            to.ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != to)
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Void)));
                            if (to instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("护甲");
                        }
                    }
                }
                case Hydro -> {
                    switch (element) {
                        case Electro -> {
                            to.getInfo().msg("感电！");
                            to.ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != to)
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Electro)));
                        }
                        case Pydro -> {
                            to.getInfo().msg("蒸发！");
                            setDamage(getDamage() * 2);
                        }
                        case Cryo -> {
                            to.getInfo().msg("冻结！");
                            if (to instanceof FollowCard toFollow)
                                toFollow.addKeywordN("眩晕", 2);
                        }
                    }
                }
                case Pydro -> {
                    switch (element) {
                        case Electro -> {
                            to.getInfo().msg("超载！");
                            new Damage(from, to, 2);
                            if (to instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("守护");
                        }
                        case Hydro -> {
                            to.getInfo().msg("蒸发（反向）！");
                            setDamage((int)(getDamage()*1.5));
                        }
                        case Cryo -> {
                            to.getInfo().msg("融化！");
                            setDamage(getDamage() * 2);
                        }
                    }
                }
                case Cryo -> {
                    switch (element) {
                        case Electro -> {
                            to.getInfo().msg("超导！");
                            to.ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard != to)
                                .forEach(followCard ->
                                    moreDamage.add(new ElementalDamage(getFrom(), followCard, 1, Elemental.Void)));
                            if (to instanceof FollowCard toFollow)
                                toFollow.removeKeywordAll("护甲");
                        }
                        case Hydro -> {
                            to.getInfo().msg("冻结！");
                            if (to instanceof FollowCard toFollow)
                                toFollow.addKeywordN("眩晕", 2);
                        }
                        case Pydro -> {
                            to.getInfo().msg("融化（反向）！");
                            setDamage((int)(getDamage()*1.5));
                        }
                    }
                }
                case Void -> {
                    to.setElementalCling(element);
                }
            }
        }
        to.setElementalCling(Elemental.Void);

        new DamageMulti(to.getInfo(), List.of(this)).apply();

        moreDamage.forEach(Damage::apply);
    }
}
