// 弥留之国页面逻辑
(function() {
    let currentVisa = null;
    let selectedExportCard = null;

    window.startBorderlandAI = function() {
        if (currentVisa === null) {
            alert('签证信息加载中，请稍后再试');
            return;
        }
        if (!currentVisa || currentVisa.status !== 'ACTIVE') {
            alert('请先办理有效的弥留之国签证');
            return;
        }
        localStorage.setItem('pendingMatchMode', 'borderland-ai');
        window.location.href = 'index.html#borderland-ai';
    };

    window.startBorderlandPvP = function() {
        if (currentVisa === null) {
            alert('签证信息加载中，请稍后再试');
            return;
        }
        if (!currentVisa || currentVisa.status !== 'ACTIVE') {
            alert('请先办理有效的弥留之国签证');
            return;
        }
        localStorage.setItem('pendingMatchMode', 'borderland-pvp');
        window.location.href = 'index.html#borderland-pvp';
    };

    // 兼容旧调用
    window.startBorderlandBattle = window.startBorderlandAI;

    // 页面加载时检查签证状态
    $(document).ready(function() {
        // 按钮已在HTML中默认禁用
        loadVisaStatus();
        loadBattleLog();
        // 每30秒检查一次惩罚期状态
        setInterval(checkPunishment, 30000);
        // 每10秒刷新一次战斗记录
        setInterval(loadBattleLog, 10000);
        
        // 绑定退出登录按钮事件
        $('#logout-btn').click(function() {
            $.post('/api/auth/logout', function() {
                window.location.href = 'login.html';
            }).fail(function() {
                alert('退出失败，请重试');
            });
        });
    });

    // 加载签证状态
    window.loadVisaStatus = function() {
        $.get('/api/borderland/visa/status', function(data) {
            currentVisa = data;
            renderVisaStatus(data);
        }).fail(function(xhr) {
            if (xhr.status === 404) {
                // 没有签证
                showNoVisaState();
            } else if (xhr.status === 401) {
                alert('请先登录');
                window.location.href = 'index.html';
            } else {
                // 其他错误也要启用按钮，避免永久禁用
                $('#apply-visa-btn').prop('disabled', false);
            }
        });
    };

    // 渲染签证状态
    function renderVisaStatus(visa) {
        if (!visa || visa.status === 'PUNISHED' || visa.status === 'EXPIRED') {
            showNoVisaState();
            if (visa && visa.status === 'PUNISHED') {
                showPunishment(visa.punishmentEndTime);
            }
        } else {
            showHasVisaState(visa);
        }
    }

    // 显示无签证状态
    function showNoVisaState() {
        $('#no-visa-state').removeClass('d-none');
        $('#has-visa-state').addClass('d-none');
        $('#deck-view').addClass('d-none');
        // 只有在不处于惩罚期时才启用按钮
        if (!$('#punishment-notice').is(':visible')) {
            $('#apply-visa-btn').prop('disabled', false);
        }
    }

    // 显示有签证状态
    function showHasVisaState(visa) {
        $('#no-visa-state').addClass('d-none');
        $('#has-visa-state').removeClass('d-none');
        
        $('#days-remaining').text(visa.daysRemaining || 0);
        
        // 计算过期日期
        const expiryDate = new Date();
        expiryDate.setDate(expiryDate.getDate() + (visa.daysRemaining || 0));
        const expiryStr = `${expiryDate.getFullYear()}年${expiryDate.getMonth() + 1}月${expiryDate.getDate()}日`;
        
        // 显示或更新过期日期
        let expiryElement = $('#visa-expiry-date');
        if (expiryElement.length === 0) {
            $('#days-remaining').parent().append('<div id="visa-expiry-date" class="text-white-50" style="font-size: 0.8rem; margin-top: 0.5rem;"></div>');
            expiryElement = $('#visa-expiry-date');
        }
        expiryElement.text(`于${expiryStr}过期`);
        
        // 解析卡组
        const deckCodes = visa.deckData ? visa.deckData.split(',').filter(c => c.trim()) : [];
        $('#deck-count').text(deckCodes.length);
        
        // 计算不同卡牌数
        const uniqueCards = new Set(deckCodes).size;
        $('#unique-cards').text(uniqueCards);
        
        // 检查是否达成54张不同卡牌
        if (uniqueCards >= 54) {
            $('#achievement-notice').removeClass('d-none');
        } else {
            $('#achievement-notice').addClass('d-none');
        }
    }

    // 显示惩罚期倒计时
    function showPunishment(endTime) {
        const end = new Date(endTime);
        const now = new Date();
        const diff = end - now;
        
        if (diff > 0) {
            $('#punishment-notice').removeClass('d-none');
            $('#apply-visa-btn').prop('disabled', true);
            
            const hours = Math.floor(diff / 3600000);
            const minutes = Math.floor((diff % 3600000) / 60000);
            $('#punishment-time').text(`${hours}小时${minutes}分钟`);
        } else {
            $('#punishment-notice').addClass('d-none');
            $('#apply-visa-btn').prop('disabled', false);
        }
    }

    // 检查惩罚期
    function checkPunishment() {
        if ($('#punishment-notice').is(':visible')) {
            loadVisaStatus();
        }
    }

    // 办理签证
    window.applyVisa = function() {
        // 检查是否已有有效签证
        if (currentVisa && currentVisa.status === 'ACTIVE') {
            alert('您已持有有效签证，无需重复办理');
            return;
        }
        
        if (!confirm('确定要办理新签证吗？将获得10天期限和40张随机卡组。')) {
            return;
        }
        
        // 禁用按钮防止重复点击
        $('#apply-visa-btn').prop('disabled', true);
        
        $.ajax({
            url: '/api/borderland/visa/apply',
            method: 'POST',
            success: function(visa) {
                alert('签证办理成功！');
                loadVisaStatus();
            },
            error: function(xhr) {
                $('#apply-visa-btn').prop('disabled', false);
                if (xhr.status === 400) {
                    alert(xhr.responseJSON?.message || '签证办理失败');
                } else {
                    alert('签证办理失败');
                }
            }
        });
    };

    // 显示卡组视图
    window.showDeckView = function() {
        if (!currentVisa) {
            alert('请先办理签证');
            return;
        }
        
        $('#deck-view').removeClass('d-none');
        renderDeck();
    };

    // 渲染卡组
    function renderDeck() {
        const deckCodes = currentVisa.deckData ? currentVisa.deckData.split(',').filter(c => c.trim()) : [];
        
        $('#total-cards').text(deckCodes.length);
        $('#unique-count').text(new Set(deckCodes).size);
        
        // 加载所有卡牌信息
        $.get('/api/cards/all', function(allCards) {
            const $grid = $('#borderland-deck-grid');
            $grid.empty();
            
            // 统计每张卡的数量
            const cardCount = {};
            deckCodes.forEach(code => {
                cardCount[code] = (cardCount[code] || 0) + 1;
            });
            
            // 获取唯一卡牌并按费用排序
            const uniqueCards = Object.keys(cardCount).map(code => {
                return allCards.find(c => c.code === code);
            }).filter(card => card !== undefined);
            
            // 按费用升序排序
            uniqueCards.sort((a, b) => (a.cost || 0) - (b.cost || 0));
            
            // 渲染每种卡牌
            uniqueCards.forEach(card => {
                const count = cardCount[card.code];
                const $cardHtml = $(createBorderlandCardHtml(card, count));
                
                // 右键丢弃
                $cardHtml.on('contextmenu', function(e) {
                    e.preventDefault();
                    if (typeof ContextMenu !== 'undefined') {
                        ContextMenu.show(e, [
                            {
                                icon: '🗑️',
                                label: `丢弃 1 张 ${card.name}`,
                                action: () => discardCard(card.code)
                            }
                        ]);
                    }
                });
                
                $grid.append($cardHtml);
            });
        });
    }

    // 创建弥留之国卡牌HTML（与deck-manager保持一致的显示逻辑）
    function createBorderlandCardHtml(card, count) {
        const rarityClass = card.rarity || 'BRONZE';
        const race = card.race || [];
        const raceStr = Array.isArray(race) ? race.join(' ') : (race || '');
        const keywords = card.keywords || [];
        const keywordsStr = Array.isArray(keywords) && keywords.length > 0 ? 
            '<b class="keyword">' + keywords.join(' ') + '</b>' : '';
        const hasStats = card.cardType === '随从' && card.attack !== undefined && card.health !== undefined;
        const mark = card.mark || '';
        
        // 将中文 cardType 映射为英文 TYPE（用于灰色水印显示）
        const typeMap = {
            '随从': 'FOLLOW',
            '法术': 'SPELL',
            '护符': 'AMULET',
            '装备': 'EQUIP'
        };
        const typeEn = typeMap[card.cardType] || card.cardType || '';
        
        // 护符卡显示倒数（样式和装备卡耐久度一样）
        const isAmulet = card.cardType === '护符' || typeEn === 'AMULET';
        let amuletCountdownHtml = '';
        if (isAmulet && card.countdown !== undefined && card.countdown !== null) {
            const countdownVal = card.countdown >= 0 ? card.countdown : '∞';
            amuletCountdownHtml = `<div class="equipment-durability">${countdownVal}</div>`;
        }
        
        // 装备卡显示攻击力和耐久度（使用 addAtk 和 countdown）
        const isEquipment = card.cardType === '装备' || typeEn === 'EQUIP';
        let equipmentStatsHtml = '';
        if (isEquipment) {
            const atk = card.addAtk !== undefined ? card.addAtk : 0;
            const durability = card.countdown !== undefined ? (card.countdown >= 0 ? card.countdown : '∞') : '∞';
            equipmentStatsHtml = `<div class="equipment-atk">${atk}</div><div class="equipment-durability">${durability}</div>`;
        }
        
        // 根据重复数量计算阴影强度（应用到card-inner）
        const shadowIntensity = Math.min(count, 5); // 最多5层效果
        const shadowOffset = shadowIntensity * 2;
        const shadowBlur = shadowIntensity * 4;
        const shadowOpacity = 0.3 + shadowIntensity * 0.1;
        
        return `
            <div class="borderland-card">
                <div class="card ${typeEn} ${rarityClass} card-type-${typeEn.toLowerCase()}" data-code="${card.code}" data-keywords='${JSON.stringify(keywords)}' data-mark='${mark.replace(/'/g, "\\'")}' data-count="${count}">
                    <div class="card-inner" style="${count > 1 ? `box-shadow: ${shadowOffset}px ${shadowOffset}px ${shadowBlur}px rgba(0,0,0,${shadowOpacity}) !important;` : ''}">
                        <div class="cost">${card.cost ?? 0}</div>
                        <div class="type">${typeEn}</div>
                        ${raceStr ? `<div class="race">${raceStr}</div>` : ''}
                        <div class="name">${escapeHtml(card.name)}</div>
                        ${hasStats ? `<div class="atk">${card.attack}</div><div class="hp">${card.health}</div>` : ''}
                        ${amuletCountdownHtml}
                        ${equipmentStatsHtml}
                        <div class="description">
                            <p>${keywordsStr}${keywordsStr && mark ? '\n' : ''}${escapeHtml(mark)}</p>
                        </div>
                        <div class="job" style="display: inline-block;">${card.job || ''}</div>
                    </div>
                </div>
                ${count > 1 ? `<div class="card-count-badge">×${count}</div>` : ''}
            </div>
        `;
    }

    // 丢弃卡牌
    function discardCard(cardCode) {
        if (!confirm('确定要永久丢弃这张卡吗？此操作无法撤销！')) {
            return;
        }
        
        $.ajax({
            url: '/api/borderland/deck/discard',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ cardCode: cardCode }),
            success: function(visa) {
                currentVisa = visa;
                renderVisaStatus(visa);
                renderDeck();
            },
            error: function() {
                alert('丢弃失败');
            }
        });
    }

    // 显示带出卡牌选择界面
    window.showExportCardSelection = function(uniqueCards) {
        $.get('/api/cards/all', function(allCards) {
            const $options = $('#export-card-options');
            $options.empty();
            
            uniqueCards.forEach(code => {
                const card = allCards.find(c => c.code === code);
                if (card) {
                    const $cardHtml = $(createBorderlandCardHtml(card, 1));
                    $cardHtml.addClass('card-option');
                    $cardHtml.click(function() {
                        $('.card-option').removeClass('selected');
                        $(this).addClass('selected');
                        selectedExportCard = code;
                        $('#confirm-export-btn').prop('disabled', false);
                    });
                    $options.append($cardHtml);
                }
            });
            
            new bootstrap.Modal($('#export-card-modal')).show();
        });
    };

    // 确认带出卡牌
    window.confirmExport = function() {
        if (!selectedExportCard) {
            alert('请选择要带出的卡牌');
            return;
        }
        
        $.ajax({
            url: '/api/borderland/export',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ cardCode: selectedExportCard }),
            success: function() {
                alert('恭喜！卡牌已加入你的收藏！');
                bootstrap.Modal.getInstance($('#export-card-modal')).hide();
                selectedExportCard = null;
                loadVisaStatus();
            },
            error: function() {
                alert('带出失败');
            }
        });
    };

    // 加载战斗记录（从服务器获取）
    window.loadBattleLog = function() {
        $.ajax({
            url: '/api/borderland/battle-logs/recent?limit=15',
            type: 'GET',
            success: function(logs) {
                renderBattleLog(logs);
            },
            error: function() {
                console.error('加载战斗记录失败');
                $('#battle-log-content').html('<div style="text-align:center;padding:20px;color:rgba(255,255,255,0.5);">加载失败</div>');
            }
        });
    };

    // 渲染战斗记录
    function renderBattleLog(logs) {
        const $content = $('#battle-log-content');
        
        if (!logs || logs.length === 0) {
            $content.html('<div style="text-align:center;padding:20px;color:rgba(255,255,255,0.5);">暂无战斗记录</div>');
            return;
        }
        
        const html = logs.map(log => {
            let icon, typeClass, message;
            
            if (log.eventType === 'match') {
                icon = '⚔';
                typeClass = 'match';
                message = `${log.player1Name} vs ${log.player2Name}`;
            } else if (log.eventType === 'victory') {
                icon = '✓';
                typeClass = 'win';
                message = `${log.winnerName} 击败 ${log.player1Name === log.winnerName ? log.player2Name : log.player1Name}`;
                if (log.punishmentSeconds) {
                    message += ` (${formatPunishmentTime(log.punishmentSeconds)})`;
                }
            } else if (log.eventType === 'defeat') {
                icon = '✗';
                typeClass = 'lose';
                const loserName = log.player1Name === log.winnerName ? log.player2Name : log.player1Name;
                message = `${loserName} 被击败`;
                if (log.punishmentSeconds) {
                    message += ` (${formatPunishmentTime(log.punishmentSeconds)})`;
                }
            }
            
            const timeStr = formatTime(log.timestamp);
            
            return `
                <div class="battle-log-item ${typeClass}">
                    <div class="battle-log-icon ${typeClass}">${icon}</div>
                    <div class="battle-log-text">${escapeHtml(message)}</div>
                    <div class="battle-log-time">${timeStr}</div>
                </div>
            `;
        }).join('');
        
        $content.html(html);
    }

    // 格式化惩罚时间（精确到秒）
    function formatPunishmentTime(seconds) {
        if (seconds < 60) {
            return `${seconds}秒惩罚`;
        } else if (seconds < 3600) {
            const minutes = Math.floor(seconds / 60);
            const secs = seconds % 60;
            return secs > 0 ? `${minutes}分${secs}秒惩罚` : `${minutes}分钟惩罚`;
        } else if (seconds < 86400) {
            const hours = Math.floor(seconds / 3600);
            const minutes = Math.floor((seconds % 3600) / 60);
            return minutes > 0 ? `${hours}小时${minutes}分钟惩罚` : `${hours}小时惩罚`;
        } else {
            const days = Math.floor(seconds / 86400);
            const hours = Math.floor((seconds % 86400) / 3600);
            return hours > 0 ? `${days}天${hours}小时惩罚` : `${days}天惩罚`;
        }
    }

    // 格式化时间（精确到秒）
    function formatTime(timestamp) {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60 * 1000) {
            return '刚刚';
        } else if (diff < 60 * 60 * 1000) {
            return Math.floor(diff / (60 * 1000)) + '分钟前';
        } else if (diff < 24 * 60 * 60 * 1000) {
            const hours = date.getHours();
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            return `${hours}:${minutes}:${seconds}`;
        } else {
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const hours = date.getHours();
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            return `${month}/${day} ${hours}:${minutes}:${seconds}`;
        }
    }

    // HTML转义
    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, m => map[m]);
    }
})();
