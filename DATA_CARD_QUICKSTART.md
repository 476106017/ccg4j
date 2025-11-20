# 数据驱动卡牌系统 - 快速开始

## 概述

这个系统允许你通过JSON文件定义卡牌，而不需要编写Java代码。已有的Java类卡牌继续正常工作，两种方式可以共存。

## 文件结构

```
src/main/resources/cards/
├── neutral/          # 中立卡牌
│   ├── basic_soldier.json
│   ├── card_draw_spell.json
│   └── dragon_knight.json
├── priest/           # 牧师卡牌
│   ├── healing_amulet.json
│   └── angel_blessing.json
├── warrior/          # 战士卡牌
│   ├── battle_standard.json
│   └── iron_sword.json
└── mage/             # 法师卡牌
    └── flame_mage.json
```

## 示例1：基础随从（无效果）

```json
{
  "id": "basic_soldier",
  "name": "士兵",
  "cardType": "FOLLOW",
  "cost": 1,
  "rarity": "BRONZE",
  "race": ["人类", "士兵"],
  "job": "中立",
  "keywords": [],
  "description": "基础随从单位",
  "atk": 1,
  "hp": 2,
  "effects": []
}
```

**说明**：
- `id`: 唯一标识符，用于程序内部引用
- `cardType`: FOLLOW(随从) | SPELL(法术) | AMULET(护符) | EQUIP(装备)
- `rarity`: BRONZE | SILVER | GOLD | RAINBOW | LEGENDARY
- `atk/hp`: 随从的攻击力和生命值

## 示例2：带战吼效果的随从

```json
{
  "id": "flame_mage",
  "name": "火焰法师",
  "cardType": "FOLLOW",
  "cost": 3,
  "rarity": "SILVER",
  "race": ["人类", "法师"],
  "job": "法师",
  "keywords": [],
  "description": "战吼：对敌方主战者造成2点伤害",
  "atk": 2,
  "hp": 3,
  "effects": [
    {
      "timing": "BATTLECRY",
      "type": "DEAL_DAMAGE",
      "target": "ENEMY_LEADER",
      "params": {
        "amount": 2
      }
    }
  ]
}
```

**效果说明**：
- `timing`: 触发时机
  - `BATTLECRY` - 战吼
  - `DEATHRATTLE` - 亡语
  - `TURN_START` - 回合开始
  - `TURN_END` - 回合结束
  - `ON_PLAY` - 使用时
  - `ON_ATTACK` - 攻击时
  - `ON_DAMAGED` - 受伤时

- `type`: 效果类型
  - `DRAW_CARD` - 抽牌
  - `DEAL_DAMAGE` - 造成伤害
  - `HEAL` - 治疗
  - `BUFF` - 增益
  - `SUMMON` - 召唤
  - `ADD_KEYWORD` - 添加关键词
  - `DISCOVER` - 发现

- `target`: 目标选择
  - `SELF` - 自己
  - `ENEMY_LEADER` - 敌方主战者
  - `MY_LEADER` - 我方主战者
  - `ALLY_FOLLOW` - 我方随从
  - `ENEMY_FOLLOW` - 敌方随从

## 示例3：多效果法术卡

```json
{
  "id": "angel_blessing",
  "name": "天使祝福",
  "cardType": "SPELL",
  "cost": 2,
  "rarity": "BRONZE",
  "job": "牧师",
  "description": "恢复我方主战者5点生命，并抽1张牌",
  "effects": [
    {
      "timing": "ON_PLAY",
      "type": "HEAL",
      "target": "MY_LEADER",
      "params": { "amount": 5 }
    },
    {
      "timing": "ON_PLAY",
      "type": "DRAW_CARD",
      "params": { "amount": 1 }
    }
  ]
}
```

## 示例4：护符卡

```json
{
  "id": "healing_amulet",
  "name": "治疗护符",
  "cardType": "AMULET",
  "cost": 2,
  "rarity": "BRONZE",
  "job": "牧师",
  "countdown": 3,
  "description": "倒数3。回合结束时，恢复我方主战者2点生命",
  "effects": [
    {
      "timing": "TURN_END",
      "type": "HEAL",
      "target": "MY_LEADER",
      "params": { "amount": 2 }
    }
  ]
}
```

**说明**：
- `countdown`: 倒数值，-1表示无限

## 示例5：装备卡

```json
{
  "id": "iron_sword",
  "name": "铁剑",
  "cardType": "EQUIP",
  "cost": 2,
  "rarity": "BRONZE",
  "job": "战士",
  "addAtk": 2,
  "durability": 3,
  "description": "装备后获得+2攻击力"
}
```

**说明**：
- `addAtk`: 提供的攻击力
- `durability`: 耐久度（使用次数）

## 示例6：使用脚本的复杂效果

```json
{
  "id": "dragon_knight",
  "name": "龙骑士",
  "cardType": "FOLLOW",
  "cost": 3,
  "atk": 2,
  "hp": 3,
  "keywords": ["突进"],
  "description": "战吼：如果我方场上有龙族随从，获得+2/+2",
  "effects": [
    {
      "timing": "BATTLECRY",
      "script": "buff_if_dragon",
      "params": {
        "buffAtk": 2,
        "buffHp": 2
      }
    }
  ]
}
```

需要在 `ScriptEffectLoader.java` 中注册脚本：
```java
registerScript("buff_if_dragon", (card, data) -> {
    return Effect.simple(e -> {
        boolean hasDragon = e.getMe().getArea().stream()
            .anyMatch(c -> c.hasRace("龙"));
        if (hasDragon) {
            int buffAtk = data.getParams().getBuffAtk();
            int buffHp = data.getParams().getBuffHp();
            if (e.getSource() instanceof FollowCard) {
                ((FollowCard) e.getSource()).buffAtkHp(buffAtk, buffHp);
            }
        }
    });
});
```

## 常用效果参数

### DRAW_CARD（抽牌）
```json
{
  "type": "DRAW_CARD",
  "params": { "amount": 2 }
}
```

### DEAL_DAMAGE（造成伤害）
```json
{
  "type": "DEAL_DAMAGE",
  "target": "ENEMY_LEADER",
  "params": { "amount": 3 }
}
```

### HEAL（治疗）
```json
{
  "type": "HEAL",
  "target": "MY_LEADER",
  "params": { "amount": 5 }
}
```

### BUFF（增益）
```json
{
  "type": "BUFF",
  "target": "SELF",
  "params": {
    "buffAtk": 2,
    "buffHp": 2
  }
}
```

### SUMMON（召唤）
```json
{
  "type": "SUMMON",
  "params": {
    "summonCard": "basic_soldier"
  }
}
```

### ADD_KEYWORD（添加关键词）
```json
{
  "type": "ADD_KEYWORD",
  "target": "SELF",
  "params": {
    "keywords": ["突进", "守护"]
  }
}
```

## 测试你的卡牌

1. 将JSON文件放在 `src/main/resources/cards/` 对应目录
2. 重启服务器
3. 查看日志确认加载成功：
   ```
   数据驱动卡牌: 8 张
   ```
4. 在游戏中测试卡牌效果

## 常见问题

**Q: 如何在卡组中使用数据卡牌？**
A: 使用卡牌的 `name` 字段，系统会自动识别是Java类还是数据卡牌。

**Q: 修改JSON后需要重启吗？**
A: 是的，目前需要重启服务器。未来可以实现热加载。

**Q: 可以定义复杂的条件判断吗？**
A: 简单条件在 `condition` 字段中定义，复杂逻辑需要使用脚本系统。

**Q: 原有的Java类卡牌还能用吗？**
A: 完全兼容，两种方式可以共存。

## 下一步

1. 尝试创建简单的白板随从
2. 添加基础效果（抽牌、伤害、治疗）
3. 学习使用脚本系统处理复杂效果
4. 查看 `CARD_MIGRATION_GUIDE.md` 了解迁移策略
