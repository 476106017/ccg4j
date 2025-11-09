package org.example.game.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public class BorderlandAiController implements AiController {

    private static final String KEYWORD_FREEZE = "\u51bb\u7ed3";
    private static final String KEYWORD_DISARM = "\u7f34\u68b0";
    private static final String KEYWORD_STUN = "\u7729\u6655";
    private static final String KEYWORD_RUSH = "\u7a81\u8fdb";
    private static final String KEYWORD_STORM = "\u75be\u9a70";
    private static final String KEYWORD_GUARD = "\u5b88\u62a4";
    private static final String KEYWORD_IGNORE_GUARD = "\u65e0\u89c6\u5b88\u62a4";

    private final String aiName;

    @Override
    public void onTurnStart(GameInfo info) {
        PlayerInfo current = info.thisPlayer();
        if (current == null || !current.isAiControlled()) {
            return;
        }
        log.debug("[{}] start turn {}", aiName, info.getTurn());
        takeTurn(current);
    }

    private void takeTurn(PlayerInfo ai) {
        GameInfo info = ai.getInfo();
        ai.autoDiscover();
        info.pushInfo();
        sleep(800);

        int safety = 0;
        while (safety++ < 50) {
            if (ai.getStep() > 0) {
                ai.autoDiscover();
                continue;
            }
            if (playOneCard(ai)) {
                sleep(1000);
                continue;
            }
            if (useLeaderSkill(ai)) {
                sleep(1000);
                continue;
            }
            if (attackOnce(ai)) {
                sleep(800);
                continue;
            }
            break;
        }

        ai.autoDiscover();
        info.pushInfo();
        sleep(500);
        info.endTurnOfCommand();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("AI sleep interrupted", e);
        }
    }

    private boolean playOneCard(PlayerInfo ai) {
        List<Card> hand = new ArrayList<>(ai.getHand());
        for (Card card : hand) {
            if (card.getCost() != null && card.getCost() > ai.getPpNum()) {
                continue;
            }
            if (card instanceof AreaCard areaCard && !(areaCard instanceof EquipmentCard)
                && ai.getArea().size() >= ai.getAreaMax()) {
                continue;
            }
            Play play = card.getPlay();
            List<GameObj> targets = resolveTargets(play);
            if (targets == null) {
                continue;
            }
            int choice = resolveChoice(play);
            try {
                card.play(targets, choice);
                ai.autoDiscover();
                ai.getInfo().pushInfo();
                return true;
            } catch (Exception e) {
                log.debug("AI failed to play {}", card.getName(), e);
            }
        }
        return false;
    }

    private List<GameObj> resolveTargets(Play play) {
        if (play == null || play.targetNum() == 0) {
            return new ArrayList<>();
        }
        List<List<GameObj>> options = play.canTargets().get();
        if (options.isEmpty()) {
            return new ArrayList<>();
        }
        List<GameObj> result = new ArrayList<>();
        for (List<GameObj> group : options) {
            GameObj pick = Lists.randOf(group);
            if (pick == null && play.mustTarget()) {
                return null;
            }
            if (pick != null) {
                result.add(pick);
            }
        }
        if (play.mustTarget() && result.size() < play.targetNum()) {
            return null;
        }
        return result;
    }

    private int resolveChoice(Play play) {
        if (play == null || play.choiceNum() <= 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(play.choiceNum()) + 1;
    }

    private boolean useLeaderSkill(PlayerInfo ai) {
        Leader leader = ai.getLeader();
        if (leader == null || !leader.isCanUseSkill() || leader.getSkillCost() > ai.getPpNum()) {
            return false;
        }
        try {
            if (leader.isNeedTarget()) {
                List<GameObj> targets = leader.targetable();
                if (targets.isEmpty()) {
                    return false;
                }
                GameObj target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
                leader.skill(target);
            } else {
                leader.skill(null);
            }
            ai.autoDiscover();
            ai.getInfo().pushInfo();
            return true;
        } catch (Exception e) {
            log.debug("AI failed to use leader skill", e);
            return false;
        }
    }

    private boolean attackOnce(PlayerInfo ai) {
        List<AreaCard> area = new ArrayList<>(ai.getArea());
        for (AreaCard areaCard : area) {
            if (!(areaCard instanceof FollowCard follow)) {
                continue;
            }
            if (!canAttackNow(follow)) {
                continue;
            }
            GameObj target = chooseAttackTarget(follow);
            if (target == null) {
                continue;
            }
            try {
                follow.attack(target);
                return true;
            } catch (Exception e) {
                log.debug("AI attack failed", e);
            }
        }
        return false;
    }

    private boolean canAttackNow(FollowCard follow) {
        if (follow.hasKeyword(KEYWORD_FREEZE)
            || follow.hasKeyword(KEYWORD_DISARM)
            || follow.hasKeyword(KEYWORD_STUN)) {
            return false;
        }
        if (follow.getTurnAttack() >= follow.getTurnAttackMax()) {
            return false;
        }
        return !(follow.getTurnAge() == 0
            && !follow.hasKeyword(KEYWORD_RUSH)
            && !follow.hasKeyword(KEYWORD_STORM));
    }

    private GameObj chooseAttackTarget(FollowCard follow) {
        PlayerInfo enemy = follow.ownerPlayer().getEnemy();
        if (enemy == null) {
            return null;
        }
        List<FollowCard> guards = enemy.getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (FollowCard) areaCard)
            .filter(f -> f.hasKeyword(KEYWORD_GUARD))
            .toList();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (!guards.isEmpty() && !follow.hasKeyword(KEYWORD_IGNORE_GUARD)) {
            return guards.get(random.nextInt(guards.size()));
        }
        List<FollowCard> others = enemy.getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (FollowCard) areaCard)
            .filter(f -> !f.hasKeyword(KEYWORD_GUARD))
            .toList();
        if (!others.isEmpty()) {
            return others.get(random.nextInt(others.size()));
        }
        if (canStrikeLeader(follow) || follow.hasKeyword(KEYWORD_IGNORE_GUARD)) {
            return enemy.getLeader();
        }
        return null;
    }

    private boolean canStrikeLeader(FollowCard follow) {
        return follow.getTurnAge() > 0 || follow.hasKeyword(KEYWORD_STORM);
    }
}
