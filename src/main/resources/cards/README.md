# 卡牌数据配置说明

## 目录结构
```
cards/
  ├── neutral/          # 中立卡牌
  ├── priest/           # 牧师职业卡
  ├── warrior/          # 战士职业卡
  ├── mage/             # 法师职业卡
  └── ...               # 其他职业
```

## 配置格式 (JSON)

### 基础卡牌属性
```json
{
  "id": "unique_card_id",
  "name": "卡牌名称",
  "cardType": "FOLLOW|SPELL|AMULET|EQUIP",
  "cost": 3,
  "rarity": "BRONZE|SILVER|GOLD|RAINBOW|LEGENDARY",
  "race": ["种族1", "种族2"],
  "job": "职业",
  "keywords": ["关键词1", "关键词2"],
  "description": "卡牌描述",
  
  // 随从卡特有
  "atk": 3,
  "hp": 4,
  
  // 护符卡特有
  "countdown": 2,
  
  // 装备卡特有
  "addAtk": 2,
  "durability": 3,
  
  // 效果配置
  "effects": [
    {
      "timing": "BATTLECRY|DEATHRATTLE|TURN_START|TURN_END|ON_PLAY|ON_ATTACK|ON_DAMAGED",
      "type": "DRAW_CARD|DEAL_DAMAGE|SUMMON|BUFF|HEAL|DISCOVER",
      "target": "SELF|ENEMY_LEADER|ALLY_FOLLOW|ENEMY_FOLLOW|ALL",
      "params": {
        "amount": 2,
        "cardFilter": "race:龙",
        "summonCard": "card_id"
      },
      "condition": "ifAllyControlsRace:龙",
      "script": "custom_script_name"
    }
  ]
}
```

## 迁移策略

1. **第一阶段**：建立数据加载系统，Java类卡牌继续工作
2. **第二阶段**：简单卡牌迁移到JSON（如白板随从）
3. **第三阶段**：复杂效果卡牌使用脚本系统
4. **第四阶段**：完全数据驱动
