package org.example.constant;

import org.example.game.GameInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public enum Patten {
    Ones("一点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 1) sum++;
        }
        return sum;
    }),
    Twos("二点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 2) sum++;
        }
        return sum;
    }),
    Threes("三点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 3) sum++;
        }
        return sum;
    }),
    Fours("四点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 4) sum++;
        }
        return sum;
    }),
    Fives("五点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 5) sum++;
        }
        return sum;
    }),
    Sixes("六点", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if(p[i] == 6) sum++;
        }
        return sum;
    }),
    OnePair("一对", p-> {
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
    TwoPairs("二对", p-> {
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
    Yatzy("YATZY", p-> {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            set.add(p[i]);
        }
        if(set.size()==1)
            return 50;
        else
            return 0;
    }),
    Random("自由", p-> {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += p[i];
        }
        return sum;
    }),
    ;

    private String name;
    private Function<int[],Integer> score;

    Patten(String name, Function<int[],Integer> score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore(GameInfo info) {
        return score.apply(info.thisPlayer().getDices());
    }
}
