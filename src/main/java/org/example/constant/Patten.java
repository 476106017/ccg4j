package org.example.constant;

import org.example.game.GameInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public enum Patten {
    Ones(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 1) sum++;
        }
        return sum;
    }),
    Twos(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 2) sum++;
        }
        return sum;
    }),
    Threes(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 3) sum++;
        }
        return sum;
    }),
    Fours(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 4) sum++;
        }
        return sum;
    }),
    Fives(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 5) sum++;
        }
        return sum;
    }),
    Sixes(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 6) sum++;
        }
        return sum;
    }),
    OnePair(p-> {
        Set<Integer> set = new HashSet<>();
        int num = 0;
        for (int i = 0; i < 5; i++) {
            if(!set.add(p[i])){
                // 找到重复的，第一次重复就记在num中，否则清零
                if(num == 0)
                    num = p[i];
                else num = 0;
            }
        }
        return num*2;
    }),
    TwoPairs(p-> {
        Set<Integer> set = new HashSet<>();
        int num1 = 0;
        int num2 = 0;
        for (int i = 0; i < 5; i++) {
            if(!set.add(p[i])){
                // 找到重复的
                if(num1 == 0) {
                    num1 = p[i];
                } else if(num1!=p[i] && num2 == 0) {
                    num2 = p[i];
                } else{
                    num1=0;num2=0;
                }
            }
        }
        return num1*2 + num2*2;
    }),
    Yatzy(p-> {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(p[i]);
        }
        if(set.size()==1)
            return 50;
        else
            return 0;
    }),
    Random(p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += p[i];
        }
        return sum;
    }),
    ;

    private Function<int[],Integer> score;

    Patten(Function<int[],Integer> score) {
        this.score = score;
    }

    public int getScore(GameInfo info) {
        return score.apply(info.thisPlayer().getDices());
    }
}
