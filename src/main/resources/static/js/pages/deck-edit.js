// 立即定义全局搜索函数（避免 onclick 触发时未定义）并延后赋值实现
window.searchCards = function(){ console.warn('searchCards 仍在初始化，请稍后重试'); };

$(document).ready(function() {
    let currentDeck = null;
    let myDeck = [];
    let allCards = [];
    let deckId = null;

    // 获取URL参数
    const urlParams = new URLSearchParams(window.location.search);
    deckId = urlParams.get('id');

    console.log('URL参数 id:', deckId);
    console.log('完整URL:', window.location.href);

    if (!deckId) {
        alert('未指定卡组');
        window.location.href = 'deck-list.html';
        return;
    }

    // 初始化
    checkLoginStatus();
    // 先加载所有卡牌，再加载卡组
    loadAllCards().then(() => {
        loadDeck();
    });

    // 返回卡组列表
    $('#back-btn').click(function() {
        window.location.href = 'deck-list.html';
    });

    // 退出登录
    $('#logout-btn').click(function() {
        $.post('/api/auth/logout', function() {
            window.location.href = 'index.html';
        });
    });

    // 保存卡组
    $('#save-deck-btn').click(function() {
        saveDeck();
    });

    // 打开搜索
    $('#search-cards-btn').click(function() {
        new bootstrap.Modal($('#card-search-modal')).show();
    });

    // 执行搜索
    $('#do-search-btn').click(function() {
        searchCards();
    });

    // 快速编辑
    $('#quick-edit-btn').click(function() {
        const deckData = myDeck.map(c => c.code).join(',');
        $('#quick-edit-textarea').val(deckData);
        new bootstrap.Modal($('#quick-edit-modal')).show();
    });

    // 确认快速编辑
    $('#confirm-quick-edit-btn').click(function() {
        const codes = $('#quick-edit-textarea').val()
            .split(',')
            .map(c => c.trim())
            .filter(c => c.length > 0);
        
        loadDeckFromCodes(codes);
        bootstrap.Modal.getInstance($('#quick-edit-modal')).hide();
    });

    // 检查登录状态
    function checkLoginStatus() {
        $.get('/api/auth/status', function(data) {
            if (!data.loggedIn) {
                window.location.href = 'index.html';
                return;
            }
            $('#username-display').text(data.user.username);
        }).fail(function() {
            window.location.href = 'index.html';
        });
    }

    // 加载所有卡牌
    function loadAllCards() {
        return $.get('/api/cards/all', function(cards) {
            allCards = cards;
        });
    }

    // 加载卡组
    function loadDeck() {
        $.get(`/api/user/deck/${deckId}`, function(deck) {
            currentDeck = deck;
            $('#deck-title').text(deck.deckName);
            
            // 解析卡组数据
            const codes = deck.deckData ? deck.deckData.split(',').map(c => c.trim()).filter(c => c.length > 0) : [];
            loadDeckFromCodes(codes);
        }).fail(function(xhr) {
            if (xhr.status === 401) {
                window.location.href = 'index.html';
            } else {
                alert('加载卡组失败');
                window.location.href = 'deck-list.html';
            }
        });
    }

    // 从卡牌代码加载卡组
    function loadDeckFromCodes(codes) {
        myDeck = [];
        codes.forEach(code => {
            const card = allCards.find(c => c.code === code);
            if (card) {
                myDeck.push(card);
            }
        });
        updateDeckDisplay();
    }

    // 保存卡组
    function saveDeck() {
        const deckData = myDeck.map(c => c.code).join(',');
        
        $.ajax({
            url: `/api/user/deck/${deckId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                deckData: deckData
            }),
            success: function(deck) {
                currentDeck = deck;
                alert('保存成功');
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    window.location.href = 'index.html';
                } else {
                    alert('保存失败: ' + (xhr.responseJSON?.message || '未知错误'));
                }
            }
        });
    }

    // 搜索卡牌
    function searchCards() {
        const nameInput = $('#card-search-name').val().trim().toLowerCase();
        const typeFilter = $('#card-search-type').val();
        const costFilterRaw = $('#card-search-cost').val();

        let results = allCards.slice();

        // 费用过滤（支持7+）
        if (costFilterRaw !== '') {
            const costFilter = parseInt(costFilterRaw, 10);
            results = results.filter(c => {
                if (costFilter === 7) {
                    return c.cost >= 7;
                }
                return c.cost === costFilter;
            });
        }

        // 类型过滤
        if (typeFilter) {
            results = results.filter(c => (c.cardType || c.type) === typeFilter);
        }

        // 关键词 / 名称 / 描述 / 种族 / 职业 全文模糊匹配
        if (nameInput) {
            const terms = nameInput.split(/\s+/).filter(t => t.length > 0);
            results = results.filter(c => {
                const haystack = [
                    c.name || '',
                    c.mark || '',
                    c.description || '',
                    (c.keywords || []).join(' '),
                    (c.race || []).join(' '),
                    c.job || ''
                ].join(' ').toLowerCase();
                return terms.every(t => haystack.includes(t));
            });
        }

        renderSearchResults(results);
    }

    // 渲染搜索结果
    function renderSearchResults(cards) {
        const $results = $('#search-results');
        $results.empty();

        if (cards.length === 0) {
            $results.html('<div class="text-center p-4">没有找到卡牌</div>');
            return;
        }

        cards.forEach(card => {
            const $card = $(createCardHtml(card));
            $card.click(() => {
                addCardToDeck(card);
            });
            $results.append($card);
        });
    }

    // 将搜索函数暴露为全局，兼容 index.html / 其他页面的 onclick 调用
    // 覆盖初始化占位的全局函数
    window.searchCards = searchCards;

    // 添加卡牌到卡组
    function addCardToDeck(card) {
        // 检查同名卡数量限制
        const cardCount = myDeck.filter(c => c.code === card.code).length;
        if (cardCount >= 3) {
            alert('该卡牌已达到上限（3张）');
            return;
        }

        // 检查卡组大小
        if (myDeck.length >= 40) {
            alert('卡组已达到上限（40张）');
            return;
        }

        myDeck.push(card);
        updateDeckDisplay();
    }

    // 更新卡组显示
    function updateDeckDisplay() {
        const $grid = $('#card-gridview');
        $grid.empty();

        myDeck.forEach((card, index) => {
            const $card = $(createCardHtml(card));

            // 添加右键菜单
            $card.on('contextmenu', (e) => {
                e.preventDefault();
                if (typeof ContextMenu !== 'undefined') {
                    ContextMenu.show(e, [
                        {
                            icon: '🗑️',
                            label: '从卡组中删除',
                            action: () => {
                                myDeck.splice(index, 1);
                                updateDeckDisplay();
                            }
                        }
                    ]);
                }
            });

            $grid.append($card);
        });

        // 更新统计
        updateStats();
    }

    // 更新统计数据
    function updateStats() {
        $('#deck-count').text(myDeck.length);
        
        // 计算总尘数
        let totalDust = 0;
        myDeck.forEach(card => {
            totalDust += getDustValue(card.rarity);
        });
        $('#deck-dust').text(totalDust);
    }

    // 获取稀有度对应的尘数
    function getDustValue(rarity) {
        const dustMap = {
            'BRONZE': 100,
            'SILVER': 400,
            'GOLD': 800,
            'RAINBOW': 1600,
            'LEGENDARY': 3200
        };
        return dustMap[rarity] || 100;
    }

    // 创建卡牌HTML
    function createCardHtml(card) {
        const rarityClass = card.rarity ? card.rarity.toLowerCase() : 'bronze';
        const _cardType = card.cardType || card.type || 'FOLLOW';
        const typeClass = _cardType;
        const keywords = card.keywords || [];
        const keywordsHtml = keywords.map(k => `<span class="keyword">${k}</span>`).join(' ');
        
        // 装备卡显示攻击力和耐久度
        let statsHtml = '';
        if (card.cardType === 'EQUIPMENT') {
            const atk = card.atk || card.attack || 0;
            const durability = card.countdown !== undefined ? (card.countdown >= 0 ? card.countdown : '∞') : '∞';
            statsHtml = `<div class="card-stats equipment-stats">⚔️${atk} 🛡️${durability}</div>`;
        } else if (card.attack !== undefined) {
            statsHtml = `<div class="card-stats">${card.attack}/${card.health}</div>`;
        }
        
        return `
            <div class="card-item ${typeClass} rarity-${rarityClass}" data-code="${card.code}">
                <div class="card-header">
                    <span class="card-name">${escapeHtml(card.name)}</span>
                    <span class="card-cost">${card.cost}</span>
                </div>
                <div class="card-type">${getTypeLabel(_cardType)} (${_cardType})</div>
                ${statsHtml}
                <div class="card-description">${escapeHtml(card.description || '')}</div>
                ${keywords.length > 0 ? `<div class="card-keywords">${keywordsHtml}</div>` : ''}
            </div>
        `;
    }

    // 获取类型标签
    function getTypeLabel(type) {
        const typeMap = {
            'FOLLOW': '随从',
            'SPELL': '法术',
            'EQUIPMENT': '装备',
            'AREA': '场地',
            'AMULET': '护符'
        };
        return typeMap[type] || type;
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
});