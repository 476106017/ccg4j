# ccg4j

可以看做影之诗（シャドバ）的fan made作品。以后可能会变成别的玩法。

## 开始
1. 配置好jdk17、gradle
2. 不需要数据库、不需要中间件，直接启动Ccg4jApplication.main()
3. 在idea或者vscode里面选择用浏览器打开test.html。在localhost域名上跑起来。
4. 拿这个域名，换个浏览器，再开一个页面（相同浏览器只能玩一个号）
5. 两个页面左右互搏吧！
6. 或者把局域网ip发给局域网里的小伙伴，用局域网ip替换localhost，和小伙伴来场pk吧！

## 最新动态
总之先做出一套“炉石vs影之诗”的玩法吧,名字就叫shadowStone影之石传说怎么样。

交战时/攻击时/受伤时/击杀时 这套逻辑做的头大。任何一个节点都可能重新指定对象，或者对象已经消失了。

感觉系统已经做的差不多了，再就是做i18n、还有开始印卡了。

前路漫漫。。。

实验室招募员跟爆牌鱼已经印好了，这下该开始有趣起来了吧

## 玩法
`(SV：shadowverse HS：heartstone STS：SlayTheSpire)`
- [x] 匹配/准备
- [x] 开局换牌
- [x] 回合计时
- [x] 预置聊天语句
- [x] 特殊胜利/投降
- [x] 随从/法术/护符
- [x] 入场回合无法攻击/突进
- [x] 入场时/离场时
- [x] 战吼/亡语
- [x] 瞬念召唤/揭示(SV)
- [x] 腐蚀/注能（HS）->增幅/注能
- [x] 轮回(从墓地移到牌堆，重置属性)
- [x] 比赛计数器
- [x] 主战者效果（SV）
- [x] 主战者技能（HS）
- [x] 激励（HS）
- [x] 交战时/攻击时/受伤时/击杀时
- [x] 无法被破坏
- [x] 疾驰/守护/必杀/吸血
- [ ] 主战者底牌（不同主战者底牌不同：死神、胜利、疲劳等）
- [ ] 自定义牌组
- [ ] ~~ep/进化（存在感太强了）~~
- [ ] ~~奥秘/陷阱（比较恶心）~~
- [ ] 英雄牌/任务牌（HS）
- [ ] 发现（HS）
- [ ] 返回手牌
- [ ] 激奏/结晶(SV)
- [ ] 葬送/弃牌(SV)
- [ ] 死灵术/召还(SV)(召还会从墓地移除，防止反复召还，没有真实感)

## 职业/卡牌
`（希望以后能有各种离谱的职业。要做ccg游戏中的mugen）`
### 中立
- 巴哈姆特
- 哥布林旅行家
- 宏愿哥布林法师
- 寒光智者
- 暗黑陷阱（自创牌、测试腐蚀效果）
- 英雄：玩家（进化(0)：使一个己方随从获得+2/+2、突进）

### 妖精
- 森林交响乐（SV怀旧牌）
- 妖之轻语者
- 森林模式（自创牌、测试轮回效果）

### 复仇者
- 永恒之盾·席翁
- 灾祸模式（SV双职业模式中被禁了，怨念ing）
- 英雄：伊昂（虚空解析(2)：将1张手牌加入牌堆，召唤1个解析的造物）

### 潜行者
- 实验室招募员

## 游戏截图（早期版本）
![img.png](imgs/snapshot1.png)
![img.png](imgs/snapshot2.png)

## 设计相关
![img.png](imgs/uml.png)
![img.png](imgs/apis.png)

