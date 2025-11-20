/**
 * 生成卡牌HTML
 * @param {Object} card 卡牌数据
 * @returns {String} HTML字符串
 */
var cardHtml = function(card){
    if (!card) return '';

    // 装备图标（如果是被装备的随从）
    var equipmentIcon = '';
    if (card.TYPE === 'FOLLOW' && card.equipment) {
        var equipName = card.equipment.name || '未知装备';
        equipmentIcon = `<div class="equipment-indicator" title="装备: ${equipName}">⚔️</div>`;
    }
    
    // 添加卡牌类型类名（用于全局样式）
    var typeClass = card.TYPE ? 'card-type-' + card.TYPE.toLowerCase() : '';
    var rarityClass = card.rarity || 'BRONZE';
    var canAttackClass = card.canAttack ? 'canAttack' : '';
    var canDashClass = card.canDash ? 'canDash' : '';
    var upgradeClass = card.upgrade ? 'upgrade' : '';
    
    // 关键字处理
    var keywordsHtml = '';
    if (card.keywords && card.keywords.length > 0) {
        keywordsHtml = `<b class="keyword">${distinctArr(card.keywords).join(' ')}</b>\n`;
    }

    // 种族处理
    var raceHtml = '';
    if (card.race && card.race.length > 0) {
        raceHtml = `<p class="race">${card.race.join(' ')}</p>`;
    }

    // 描述文本处理
    var markText = (card.mark || "").replace(/'/g, "\\'");
    var subMarkText = card.subMarkStr || "";
    
    // 数据属性
    var dataKeywords = JSON.stringify(card.keywords || []);
    var dataEquipment = card.equipment ? JSON.stringify(card.equipment).replace(/'/g, "\\'") : "";

    // 各部分显示控制
    var showCountDown = card.TYPE === "AMULET" ? "" : "hidden";
    var showStats = card.TYPE === "FOLLOW" ? "" : "hidden";
    var showEquipStats = card.TYPE === "EQUIP" ? "" : "hidden";

    // 倒数显示
    var countDownText = (card.countDown > 0) ? card.countDown : "∞";
    
    // 血量条计算
    var hpPercent = 0;
    if (card.maxHp > 0) {
        hpPercent = Math.max(0, Math.min(100, (card.hp / card.maxHp * 100)));
    }

    return `
        <div class="card col-sm-6 col-md-4 col-lg-2 id-${card.id} ${card.TYPE} ${typeClass} ${canAttackClass} ${canDashClass} ${rarityClass}" 
             data-keywords='${dataKeywords}' 
             data-mark='${markText}' 
             data-equipment='${dataEquipment}'>
            <div class="card-inner">
                <div class="name ${upgradeClass}">${card.name}${card.upgrade ? " +" : ""}</div>
                <div class="type">${card.TYPE}</div>
                ${raceHtml}
                <div class="cost">${card.cost}</div>
                <div class="description">
                    <p>${keywordsHtml}${card.mark}<i>${subMarkText}</i></p>
                    <div class="job">${card.job}</div>
                </div>
                
                <div ${showCountDown}>
                    <div class="countDown">倒数：${countDownText}</div>
                </div>
                
                <div ${showStats}>
                    <div class="attack">${card.atk}</div>
                    <div class="health" title="${card.hp}/${card.maxHp}">
                        <div class="health-text">${card.hp}</div>
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
        </div>
    `;
}
