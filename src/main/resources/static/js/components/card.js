console.log('Loading card.js...');
/**
 * 生成卡牌HTML
 * @param {Object} card 卡牌数据
 * @returns {String} HTML字符串
 */
function cardHtml(card){
    if (!card) return '';

    // Data Adaptation for Global Reuse
    var typeMap = {
        '随从': 'FOLLOW',
        '法术': 'SPELL',
        '护符': 'AMULET',
        '装备': 'EQUIP'
    };
    // Use existing TYPE or map from type/cardType
    card.TYPE = card.TYPE || typeMap[card.type] || typeMap[card.cardType] || 'SPELL';
    card.rarity = card.rarity || 'BRONZE';
    
    // Better adaptation for health/attack with defaults
    card.maxHp = card.maxHp ?? card.hp ?? card.health ?? card.maxHealth ?? 0;
    card.hp = card.hp ?? card.health ?? card.currentHealth ?? card.maxHp ?? 0;
    card.atk = card.atk ?? card.attack ?? card.power ?? 0;
    
    // Ensure keywords/race are arrays
    card.keywords = Array.isArray(card.keywords) ? card.keywords : [];
    card.race = Array.isArray(card.race) ? card.race : (card.race ? [card.race] : []);

    // 装备图标（如果是被装备的随从）
    var equipmentIcon = '';
    if (card.TYPE === 'FOLLOW' && card.equipment) {
        var equipName = card.equipment.name || '未知装备';
        equipmentIcon = `<div class="equipment-indicator" title="装备: ${equipName}">⚔️</div>`;
    }
    
    // 添加卡牌类型类名（用于全局样式）
    var typeClass = card.TYPE ? 'card-type-' + card.TYPE.toLowerCase() : '';
    var rarityClass = card.rarity;
    var canAttackClass = card.canAttack ? 'canAttack' : '';
    var canDashClass = card.canDash ? 'canDash' : '';
    var upgradeClass = card.upgrade ? 'upgrade' : '';
    
    // 关键字处理 - 单独一行
    var keywordsHtml = '';
    var tooltipHtml = '';
    
    if (card.keywords && card.keywords.length > 0) {
        // Add tooltips for keywords
        var keywordsList = distinctArr(card.keywords).map(k => `<span class="keyword-item">${k}</span>`).join('');
        keywordsHtml = `<div class="keywords-row">${keywordsList}</div>`;
        
    // Keyword Definitions
        const keywordDefinitions = {
            "速攻": "可以在敌方回合使用",
            "疾驰": "入场回合可以立即攻击敌方单位",
            "突进": "入场回合可以立即攻击敌方随从",
            "复生": "死亡时，以1点生命值重新召唤该随从",
            "守护": "敌方随从必须优先攻击此随从",
            "无视守护": "可以无视守护攻击目标",
            "缴械": "无法攻击",
            "冻结": "无法攻击，敌方回合开始时解除",
            "眩晕": "无法攻击和反击，敌方回合开始时解除",
            "远程": "伤害无法被反击",
            "游魂": "死亡时，不会进入墓地",
            "虚无": "回合结束时：除外此卡",
            "剧毒": "此卡的伤害可以秒杀随从",
            "吸血": "此卡的伤害可以治疗主战者",
            "自愈": "此卡的伤害可以治疗自身",
            "重伤": "此卡的伤害可以给予随从【无法回复】",
            "无法回复": "无法被治疗",
            "格挡": "消耗后,抵挡等量的伤害",
            "护甲": "抵挡等量的攻击伤害",
            "魔抗": "抵挡等量的效果伤害",
            "效果伤害免疫": "使效果伤害变为0",
            "穿透": "无视伤害减免效果",
            "圣盾": "免疫一次伤害",
            "魔法护盾": "消耗后，免疫效果伤害、破坏、变身、除外、返回手牌",
            "无法破坏": "免疫破坏",
            "魔法免疫": "免疫效果伤害、破坏、变身、除外、返回手牌",
            "灵魂绑定": "此卡被打出后会留在手中",
            "恶魔转生": "被除外时，使游戏中任意一张非【恶魔转生】卡变成该卡",
            "死亡掉落": "装备在破坏后进入敌方手牌",
            "压轴": "如果使用后pp为0，则发动效果",
            "死灵术": "消耗等额的墓地数发动效果",
            "自搜": "除外牌堆里同名卡片发动效果",
            "瞬念召唤": "当此卡符合指定条件时，从牌堆召唤并触发效果",
            "注能": "战场随从被破坏时发动效果",
            "招募": "从牌堆召唤特定卡片",
            "轮回": "使墓地卡片重回牌堆",
            "增幅": "打出其他卡片时触发效果",
            "搜索": "抽到符合要求的卡片",
            "战吼": "在此随从进入战场/使用时触发的效果",
            "亡语": "在此随从离场时触发的效果",
            "回合结束": "在你的回合结束时触发的效果",
            "回合开始": "在你的回合开始时触发的效果",
            "使用时": "在使用此卡片时触发的效果",
            "受到伤害时": "当此随从受到伤害时触发的效果",
            "攻击时": "当此随从发起攻击时触发的效果",
            "揭示": "当此卡符合指定条件时，从牌堆抽到手中并触发效果",
            "腐蚀": "当使用费用高于此卡片的其他卡片时触发的效果",
            "离场时": "当该随从离开战场（返回手牌/被破坏等）时触发的效果",
            "超杀": "当造成的伤害超过目标剩余生命时触发的额外效果"
        };

        const leftKeywords = [
            "速攻", "疾驰", "突进", "复生", "守护", "无视守护", "缴械", "冻结", "眩晕", "远程", 
            "游魂", "虚无", "剧毒", "吸血", "自愈", "重伤", "无法回复", "格挡", "护甲", "魔抗", 
            "效果伤害免疫", "穿透", "圣盾", "魔法护盾", "无法破坏", "魔法免疫", "灵魂绑定", 
            "恶魔转生", "死亡掉落"
        ];
        
        const uniqueKeywords = distinctArr(card.keywords);
        const leftList = uniqueKeywords.filter(k => leftKeywords.includes(k));
        
        // Start with keywords that are NOT in leftList
        let rightList = uniqueKeywords.filter(k => !leftKeywords.includes(k));

        // Scan description (card.mark or card.description) for additional triggers
        const mark = (card.mark || card.description || "").toString();
        const extraTriggers = [];
        
        if (/战吼/.test(mark)) extraTriggers.push('战吼');
        if (/亡语|亡語/.test(mark)) extraTriggers.push('亡语');
        if (/回合结束时/.test(mark)) extraTriggers.push('回合结束'); // Map to simplified key
        if (/回合开始时/.test(mark)) extraTriggers.push('回合开始');
        if (/进化时/.test(mark)) extraTriggers.push('进化时');
        if (/攻击时/.test(mark)) extraTriggers.push('攻击时');
        if (/交战时/.test(mark)) extraTriggers.push('交战时');
        if (/连击/.test(mark)) extraTriggers.push('连击');
        if (/爆能强化/.test(mark)) extraTriggers.push('爆能强化');
        if (/土之秘术/.test(mark)) extraTriggers.push('土之秘术');
        if (/觉醒/.test(mark)) extraTriggers.push('觉醒');
        if (/复仇/.test(mark)) extraTriggers.push('复仇');
        if (/共鸣/.test(mark)) extraTriggers.push('共鸣');
        
        // Extra triggers from game.js
        if (/受伤时|受到伤害时|受伤/.test(mark)) extraTriggers.push('受伤时');
        if (/增幅|魔力增幅|增幅效果/.test(mark)) extraTriggers.push('增幅');
        if (/瞬念召唤|瞬念/.test(mark)) extraTriggers.push('瞬念召唤');
        if (/揭示/.test(mark)) extraTriggers.push('揭示');
        if (/腐蚀/.test(mark)) extraTriggers.push('腐蚀');
        if (/离场时|不在场时/.test(mark)) extraTriggers.push('离场时');
        if (/超杀|超额杀死|超杀时/.test(mark)) extraTriggers.push('超杀');
        if (/出牌时/.test(mark) && !/战吼/.test(mark)) extraTriggers.push('出牌时');

        // Merge and deduplicate rightList
        rightList = [...new Set([...rightList, ...extraTriggers])];

        // Generate tooltip content
        tooltipHtml = `
            <div class="card-tooltips">
                ${leftList.length > 0 ? `
                <div class="keyword-tooltips-left">
                    ${leftList.map(k => `<div class="tooltip-item"><b>${k}</b>: ${keywordDefinitions[k] || '特殊效果'}</div>`).join('')}
                </div>` : ''}
                
                ${rightList.length > 0 ? `
                <div class="keyword-tooltips-right">
                    ${rightList.map(k => `<div class="tooltip-item"><b>${k}</b>: ${keywordDefinitions[k] || '触发效果'}</div>`).join('')}
                </div>` : ''}
            </div>
        `;
    } else {
        // Even if no explicit keywords, we might still have triggers in the description
        const mark = (card.mark || card.description || "").toString();
        const extraTriggers = [];
        
        if (/战吼/.test(mark)) extraTriggers.push('战吼');
        if (/亡语|亡語/.test(mark)) extraTriggers.push('亡语');
        if (/回合结束时/.test(mark)) extraTriggers.push('回合结束');
        if (/回合开始时/.test(mark)) extraTriggers.push('回合开始');
        if (/进化时/.test(mark)) extraTriggers.push('进化时');
        if (/攻击时/.test(mark)) extraTriggers.push('攻击时');
        if (/交战时/.test(mark)) extraTriggers.push('交战时');
        if (/连击/.test(mark)) extraTriggers.push('连击');
        if (/爆能强化/.test(mark)) extraTriggers.push('爆能强化');
        if (/土之秘术/.test(mark)) extraTriggers.push('土之秘术');
        if (/觉醒/.test(mark)) extraTriggers.push('觉醒');
        if (/复仇/.test(mark)) extraTriggers.push('复仇');
        if (/共鸣/.test(mark)) extraTriggers.push('共鸣');
        if (/受伤时|受到伤害时|受伤/.test(mark)) extraTriggers.push('受伤时');
        if (/增幅|魔力增幅|增幅效果/.test(mark)) extraTriggers.push('增幅');
        if (/瞬念召唤|瞬念/.test(mark)) extraTriggers.push('瞬念召唤');
        if (/揭示/.test(mark)) extraTriggers.push('揭示');
        if (/腐蚀/.test(mark)) extraTriggers.push('腐蚀');
        if (/离场时|不在场时/.test(mark)) extraTriggers.push('离场时');
        if (/超杀|超额杀死|超杀时/.test(mark)) extraTriggers.push('超杀');
        if (/出牌时/.test(mark) && !/战吼/.test(mark)) extraTriggers.push('出牌时');
        
        if (extraTriggers.length > 0) {
             const keywordDefinitions = {
                '战吼': '从手牌使用这张卡牌时发动的效果。',
                '亡语': '该随从被破坏时发动的效果。',
                '谢幕曲': '该随从被破坏时发动的效果。',
                '回合结束': '在你的回合结束时发动的效果。',
                '回合开始': '在你的回合开始时发动的效果。',
                '进化时': '该随从进化时发动的效果。',
                '攻击时': '该随从攻击时发动的效果。',
                '交战时': '该随从与敌方随从进行战斗时发动的效果。',
                '连击': '本回合中如果已经使用过其他卡牌，则发动的效果。',
                '爆能强化': '如果剩余费用足够，消耗额外费用发动的效果。',
                '土之秘术': '如果场上有土之印，破坏土之印并发动的效果。',
                '觉醒': 'PP最大值达到7以上时发动的效果。',
                '复仇': '主战者生命值为10以下时发动的效果。',
                '共鸣': '卡组剩余张数为偶数时发动的效果。',
                '受伤时': '该单位受到伤害时触发的效果（通常在伤害结算后）。',
                '增幅': '打出其他卡片时触发效果。',
                '瞬念召唤': '当此卡符合指定条件时，从牌堆召唤并触发效果。',
                '揭示': '当此卡符合指定条件时，从牌堆抽到手中并触发效果。',
                '腐蚀': '当使用费用高于此卡片的其他卡片时触发的效果。',
                '离场时': '当该随从离开战场（返回手牌、被除外、被破坏等）时触发的效果。',
                '超杀': '当对手单位被造成的伤害超过其剩余生命时触发的额外效果。',
                '出牌时': '打出该卡时触发的效果。'
            };
            
            tooltipHtml = `
            <div class="card-tooltips">
                <div class="keyword-tooltips-right">
                    ${extraTriggers.map(k => `<div class="tooltip-item"><b>${k}</b>: ${keywordDefinitions[k] || '触发效果'}</div>`).join('')}
                </div>
            </div>
            `;
        }
    }

    // 种族处理 - 改为徽章样式
    var raceHtml = '';
    if (card.race && card.race.length > 0) {
        raceHtml = `<div class="race-badge">${card.race.join(' ')}</div>`;
    }

    // 描述文本处理
    var markText = String(card.mark || "").replace(/'/g, "\\'");
    var subMarkText = card.subMarkStr || "";
    
    // 数据属性
    var dataKeywords = JSON.stringify(card.keywords || []);
    var dataEquipment = card.equipment ? JSON.stringify(card.equipment).replace(/'/g, "\\'") : "";

    // 各部分显示控制
    var showCountDown = (card.TYPE === "AMULET" || card.TYPE === "EQUIP") ? "" : "hidden"; // Equip also shows countdown as durability
    var showStats = card.TYPE === "FOLLOW" ? "" : "hidden";
    var showEquipStats = card.TYPE === "EQUIP" ? "" : "hidden";

    // 倒数显示 (For Amulet)
    var countDownText = (card.countDown > 0 || card.countdown > 0) ? (card.countDown || card.countdown) : "∞";
    if (card.TYPE === 'EQUIP') showCountDown = "hidden"; // Equip uses specific stats

    // 血量条计算
    var hpPercent = 0;
    if (card.maxHp > 0) {
        hpPercent = Math.max(0, Math.min(100, (card.hp / card.maxHp * 100)));
    }

    // Calculate font size based on name length
    var nameLength = (card.name + (card.upgrade ? " +" : "")).length;
    var nameFontSize = nameLength > 12 ? '0.85rem' : (nameLength > 10 ? '0.95rem' : '1.1rem');

    return `
        <div class="card col-sm-6 col-md-4 col-lg-2 id-${card.id} ${card.TYPE} ${typeClass} ${canAttackClass} ${canDashClass} ${rarityClass}" 
             data-keywords='${dataKeywords}' 
             data-mark='${markText}' 
             data-equipment='${dataEquipment}'>
            <div class="card-inner">
                <div class="name ${upgradeClass}" style="font-size: ${nameFontSize};">${card.name}${card.upgrade ? " +" : ""}</div>
                <div class="type">${card.TYPE}</div>
                ${raceHtml}
                <div class="description">
                    ${keywordsHtml}
                    <p>${card.mark}<i>${subMarkText}</i></p>
                </div>
            </div>
            ${tooltipHtml}
            
            <!-- Job badge at card level, top right -->
            ${card.job ? `<div class="job-badge">${card.job}</div>` : ''}
            
            <div class="cost">${card.cost}</div>

            <div ${showCountDown}>
                <div class="countDown">${countDownText}</div>
            </div>
            
            
            <div ${showStats}>
                <div class="attack">${card.atk !== undefined ? card.atk : 0}</div>
                <div class="health" title="${card.hp !== undefined ? card.hp : 0}/${card.maxHp !== undefined ? card.maxHp : 0}">
                    <div class="health-text">${card.hp !== undefined ? card.hp : 0}</div>
                </div>
                <div class="health-bar-small">
                    <div class="health-bar-inner-small" style="width: ${hpPercent}%;"></div>
                </div>
                ${equipmentIcon}
            </div>
            
            <div ${showEquipStats}>
                <div class="equipment-atk" title="攻击力：${card.addAtk || 0}">${card.addAtk || 0}</div>
                <div class="equipment-durability" title="耐久度：${card.countdown >= 0 ? card.countdown : '∞'}">${card.countdown >= 0 ? card.countdown : "∞"}</div>
            </div>
        </div>
    `;
}

// Ensure cardHtml is globally available
window.cardHtml = cardHtml;
console.log('card.js loaded successfully, window.cardHtml =', typeof window.cardHtml);
