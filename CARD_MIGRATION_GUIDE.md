# 卡牌重构迁移指南

## 系统架构

现在支持两种卡牌定义方式共存：

### 1. Java类卡牌（现有系统）
- 位置：`src/main/java/org/example/card/`
- 方式：继承 `Card`、`FollowCard`、`SpellCard` 等基类
- 优点：可以编写复杂的自定义逻辑
- 缺点：需要编译、重启，不够灵活

### 2. 数据驱动卡牌（新系统）
- 位置：`src/main/resources/cards/`
- 方式：JSON配置文件
- 优点：热加载、易于修改、无需编程
- 缺点：复杂效果需要预定义或使用脚本

## 迁移步骤

### 第一阶段：简单卡牌迁移

适合迁移的卡牌类型：
- 白板随从（无效果）
- 简单效果（抽牌、造成伤害、治疗）
- 基础增益效果

示例：
```json
{
  "id": "basic_soldier",
  "name": "士兵",
  "cardType": "FOLLOW",
  "cost": 1,
  "atk": 1,
  "hp": 2
}
```

### 第二阶段：标准效果卡牌

已支持的效果类型：
- `DRAW_CARD` - 抽牌
- `DEAL_DAMAGE` - 造成伤害
- `HEAL` - 治疗
- `BUFF` - 增益
- `SUMMON` - 召唤
- `ADD_KEYWORD` - 添加关键词
- `DISCOVER` - 发现

### 第三阶段：脚本效果卡牌

复杂效果使用脚本系统：
```json
{
  "effects": [
    {
      "timing": "BATTLECRY",
      "script": "custom_effect_name"
    }
  ]
}
```

在 `ScriptEffectLoader.java` 中注册脚本：
```java
registerScript("custom_effect_name", (card, data) -> {
    return Effect.simple(e -> {
        // 自定义逻辑
    });
});
```

## 使用方法

### 1. 创建新卡牌

在 `src/main/resources/cards/` 下创建JSON文件：
```
cards/
  ├── neutral/       # 中立卡
  ├── priest/        # 牧师卡
  ├── warrior/       # 战士卡
  └── ...
```

### 2. 配置示例

**基础随从：**
```json
{
  "id": "soldier",
  "name": "士兵",
  "cardType": "FOLLOW",
  "cost": 1,
  "atk": 2,
  "hp": 2,
  "race": ["人类"],
  "job": "中立"
}
```

**带效果随从：**
```json
{
  "id": "card_drawer",
  "name": "占卜师",
  "cardType": "FOLLOW",
  "cost": 2,
  "atk": 1,
  "hp": 3,
  "effects": [
    {
      "timing": "BATTLECRY",
      "type": "DRAW_CARD",
      "params": { "amount": 1 }
    }
  ]
}
```

**法术卡：**
```json
{
  "id": "fireball",
  "name": "火球术",
  "cardType": "SPELL",
  "cost": 4,
  "effects": [
    {
      "timing": "ON_PLAY",
      "type": "DEAL_DAMAGE",
      "target": "ENEMY_LEADER",
      "params": { "amount": 6 }
    }
  ]
}
```

## 优先迁移列表

建议按以下顺序迁移：

1. ✅ 白板随从（无效果）
2. ✅ 抽牌/造成伤害的简单法术
3. ✅ 基础战吼效果
4. ⏳ 亡语效果
5. ⏳ 持续效果
6. ⏳ 复杂条件触发

## 注意事项

1. **ID唯一性**：每个卡牌的 `id` 必须全局唯一
2. **向后兼容**：Java类卡牌继续正常工作
3. **热加载**：修改JSON后重启即可生效
4. **测试**：迁移后需要测试卡牌效果是否正确

## 扩展系统

### 添加新的效果类型

在 `EffectBuilder.java` 中注册：
```java
EFFECT_BUILDERS.put("NEW_EFFECT", EffectBuilder::buildNewEffect);
```

### 添加新的脚本

在 `ScriptEffectLoader.java` 中注册：
```java
registerScript("new_script", (card, data) -> {
    return Effect.simple(e -> {
        // 实现逻辑
    });
});
```

## 性能考虑

- 数据卡牌和Java类卡牌性能相近
- 所有卡牌在启动时预加载
- 使用原型模式，运行时创建副本

## 下一步计划

1. 迁移常用的基础卡牌
2. 完善效果类型支持
3. 添加条件判断系统
4. 实现真正的脚本引擎（JavaScript/Groovy）
5. 开发可视化卡牌编辑器
