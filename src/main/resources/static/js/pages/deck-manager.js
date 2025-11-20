// 卡组管理器 - 集成到 index.html 的单页面应用
(function() {
    let currentDeck = null;
    let myDeck = [];
    let allCards = [];
    let allDecks = [];
    let currentDeckId = null;
    let currentView = 'list'; // 'list' 或 'edit'

    // 当切换到卡组 tab 时初始化
    $('#deck-tab').on('shown.bs.tab', function() {
        if (allCards.length === 0) {
            loadAllCards().then(() => {
                loadDeckList();
                // 默认显示所有卡牌（按费用排序）
                renderSearchResults(allCards);
            });
        } else {
            loadDeckList();
        }
    });

    // 新建卡组入口改为列表中的卡片，不再使用按钮 #create-deck-btn

    $('#confirm-create-btn').click(function() {
        const deckName = $('#deck-name-input').val().trim();
        if (!deckName) {
            alert('请输入卡组名称');
            return;
        }
        createDeck(deckName);
    });

    // 返回列表
    $('#back-to-list-btn').click(function() {
        showDeckList();
    });

    // 保存卡组
    $('#save-deck-btn').click(function() {
        saveDeck();
    });

    // 搜索卡牌
    $('#search-cards-btn').click(function() {
        // 打开模态框时默认显示所有卡牌
        renderSearchResults(allCards);
        new bootstrap.Modal($('#card-search-modal')).show();
    });

    $('#do-search-btn').click(function() {
        searchCards();
    });

    // 快速编辑
    $('#quick-edit-btn').click(function() {
        // 使用 # 分隔的卡牌名称
        const deckData = myDeck.map(c => c.name).join('#');
        $('#quick-edit-textarea').val(deckData);
        new bootstrap.Modal($('#quick-edit-modal')).show();
    });

    $('#confirm-quick-edit-btn').click(function() {
        const names = $('#quick-edit-textarea').val()
            .split('#')
            .map(n => n.trim())
            .filter(n => n.length > 0);
        
        loadDeckFromNames(names);
        bootstrap.Modal.getInstance($('#quick-edit-modal')).hide();
    });

    // 重命名
    $('#confirm-rename-btn').click(function() {
        const newName = $('#rename-input').val().trim();
        if (!newName) {
            alert('请输入新名称');
            return;
        }
        if (currentDeckId) {
            renameDeck(currentDeckId, newName);
        }
    });

    // 加载所有卡牌
    function loadAllCards() {
        return $.get('/api/cards/all', function(cards) {
            allCards = cards;
            console.log('已加载', allCards.length, '张卡牌');
        });
    }

    // 加载卡组列表
    function loadDeckList() {
        $.get('/api/user/deck/list', function(decks) {
            allDecks = decks;
            renderDeckList(decks);
        }).fail(function(xhr) {
            if (xhr.status === 401) {
                alert('请先登录');
            } else {
                alert('加载卡组失败');
            }
        });
    }

    // 渲染卡组列表
    function renderDeckList(decks) {
        const grid = $('#deck-grid');
        grid.empty();

        // 新建卡组卡片
        const createDeckHtml = `
            <div class="col-12 col-md-6 col-lg-4">
                <div class="deck-card create-deck">
                    <div class="deck-card-name">➕ 新建卡组</div>
                    <div class="deck-card-info">
                        <div class="deck-card-stats">
                            <div class="deck-stat">点击创建一个新的空卡组</div>
                        </div>
                    </div>
                </div>
            </div>`;
        grid.append(createDeckHtml);

        // 添加用户自定义卡组
        decks.forEach(deck => {
            const updatedTime = formatTime(deck.updatedAt);
            const cardHtml = `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="deck-card" data-deck-id="${deck.id}">
                        <div class="deck-card-name">${escapeHtml(deck.deckName)}</div>
                        <div class="deck-card-info">
                            <div class="deck-card-stats">
                                <div class="deck-stat">
                                    卡牌数量: <strong>${deck.cardCount}</strong>
                                </div>
                                <div class="deck-stat deck-dust">
                                    合成消耗: <strong>${deck.totalDust}</strong> 尘
                                </div>
                                <div class="deck-updated">${updatedTime}</div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            grid.append(cardHtml);
        });

        // 绑定点击事件 - 整个卡片都可点击
        $('.deck-card').click(function(e) {
            // 新建卡组
            if ($(this).hasClass('create-deck')) {
                $('#deck-name-input').val('');
                new bootstrap.Modal($('#createDeckModal')).show();
                return;
            }
            const deckId = $(this).data('deck-id');
            editDeck(deckId);
        });

        // 右键菜单 - 删除和重命名
        $('.deck-card:not(.create-deck)').on('contextmenu', function(e) {
            e.preventDefault();
            const deckId = $(this).data('deck-id');
            const deckName = $(this).find('.deck-card-name').text();
            
            if (typeof ContextMenu !== 'undefined') {
                ContextMenu.show(e, [
                    {
                        icon: '✏️',
                        label: '重命名',
                        action: () => {
                            currentDeckId = deckId;
                            $('#rename-input').val(deckName);
                            new bootstrap.Modal($('#renameDeckModal')).show();
                        }
                    },
                    {
                        icon: '🗑️',
                        label: '删除',
                        action: () => {
                            if (confirm(`确定要删除卡组"${deckName}"吗？`)) {
                                deleteDeck(deckId);
                            }
                        }
                    }
                ]);
            }
        });
    }

    // 显示卡组列表视图
    function showDeckList() {
        currentView = 'list';
        $('#deck-grid').removeClass('d-none');
        $('#deck-edit-view').addClass('d-none');
        loadDeckList();
    }

    // 编辑卡组
    function editDeck(deckId) {
        console.log('编辑卡组 ID:', deckId);
        
        // 先清空当前卡组，避免随机卡组的干扰
        myDeck = [];
        currentDeck = null;
        
        $.get(`/api/user/deck/${deckId}`, function(deck) {
            console.log('加载卡组数据:', deck);
            currentDeck = deck;
            currentDeckId = deckId;
            currentView = 'edit';
            
            $('#deck-edit-title').text(deck.deckName);
            $('#deck-grid').addClass('d-none');
            $('#deck-edit-view').removeClass('d-none');
            
            const codes = deck.deckData ? deck.deckData.split(',').map(c => c.trim()).filter(c => c.length > 0) : [];
            console.log('卡组卡牌代码:', codes);
            loadDeckFromCodes(codes);
        }).fail(function() {
            alert('加载卡组失败');
        });
    }

    // 创建卡组
    function createDeck(deckName) {
        $.ajax({
            url: '/api/user/deck/create',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                deckName: deckName,
                deckData: ''
            }),
            success: function(deck) {
                bootstrap.Modal.getInstance($('#createDeckModal')).hide();
                editDeck(deck.id);
            },
            error: function() {
                alert('创建卡组失败');
            }
        });
    }

    // 保存卡组
    function saveDeck() {
        const deckData = myDeck.map(c => c.code).join(',');
        
        console.log('保存卡组 ID:', currentDeckId);
        console.log('卡组数据:', deckData);
        console.log('卡组卡牌数量:', myDeck.length);
        
        $.ajax({
            url: `/api/user/deck/${currentDeckId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                deckData: deckData
            }),
            success: function(deck) {
                console.log('保存成功，返回数据:', deck);
                currentDeck = deck;
                alert('保存成功');
            },
            error: function(xhr, status, error) {
                console.error('保存失败:', xhr, status, error);
                alert('保存失败');
            }
        });
    }

    // 重命名卡组
    function renameDeck(deckId, newName) {
        $.ajax({
            url: `/api/user/deck/${deckId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                deckName: newName
            }),
            success: function() {
                bootstrap.Modal.getInstance($('#renameDeckModal')).hide();
                currentDeckId = null;
                if (currentView === 'list') {
                    loadDeckList();
                } else {
                    $('#deck-edit-title').text(newName);
                }
            },
            error: function() {
                alert('重命名失败');
            }
        });
    }

    // 删除卡组
    function deleteDeck(deckId) {
        $.ajax({
            url: `/api/user/deck/${deckId}`,
            method: 'DELETE',
            success: function() {
                loadDeckList();
            },
            error: function() {
                alert('删除失败');
            }
        });
    }

    // 从卡牌代码加载卡组
    function loadDeckFromCodes(codes) {
        console.log('从代码加载卡组，代码数量:', codes.length);
        console.log('卡组代码:', codes);
        myDeck = [];
        codes.forEach(code => {
            const card = allCards.find(c => c.code === code);
            if (card) {
                // 深拷贝避免引用问题
                myDeck.push({...card});
            } else {
                console.warn('找不到卡牌:', code);
            }
        });
        console.log('加载后卡组大小:', myDeck.length);
        updateDeckDisplay();
    }

    // 从卡牌名称加载卡组（用于快速编辑）
    function loadDeckFromNames(names) {
        console.log('从名称加载卡组，名称数量:', names.length);
        console.log('卡牌名称:', names);
        myDeck = [];
        names.forEach(name => {
            const card = allCards.find(c => c.name === name);
            if (card) {
                // 深拷贝避免引用问题
                myDeck.push({...card});
            } else {
                console.warn('找不到卡牌:', name);
            }
        });
        console.log('加载后卡组大小:', myDeck.length);
        updateDeckDisplay();
    }

    // 搜索卡牌
    function searchCards() {
        const name = $('#card-search-name').val().trim();
        const cost = $('#card-search-cost').val();

        let results = allCards;

        if (name) {
            const searchTerm = name.toLowerCase();
            results = results.filter(c => 
                c.name && c.name.toLowerCase().includes(searchTerm)
            );
        }
        
        if (cost !== '') {
            const costNum = parseInt(cost);
            results = results.filter(c => c.cost === costNum);
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

        // 按费用排序（从低到高）
        const sortedCards = [...cards].sort((a, b) => (a.cost || 0) - (b.cost || 0));

        sortedCards.forEach(card => {
            const $card = $(createCardHtml(card));
            $card.click(function() {
                // 添加动画效果
                $(this).addClass('adding');
                setTimeout(() => {
                    $(this).removeClass('adding');
                }, 500);
                
                addCardToDeck(card);
            });
            $results.append($card);
        });
    }

    // 添加卡牌到卡组
    function addCardToDeck(card) {
        console.log('尝试添加卡牌:', card.code, card.name);
        console.log('添加前卡组大小:', myDeck.length);
        console.log('当前卡组内容:', myDeck.map(c => c.code));
        
        const cardCount = myDeck.filter(c => c.code === card.code).length;
        if (cardCount >= 3) {
            alert('该卡牌已达到上限（3张）');
            return;
        }

        if (myDeck.length >= 40) {
            alert('卡组已达到上限（40张）');
            return;
        }

        // 深拷贝避免引用问题
        myDeck.push({...card});
        console.log('添加后卡组大小:', myDeck.length);
        console.log('添加后卡组内容:', myDeck.map(c => c.code));
        updateDeckDisplay(true); // 传递 true 表示有新卡牌添加
    }

    // 更新卡组显示
    function updateDeckDisplay(hasNewCard) {
        console.log('更新卡组显示，当前卡组数量:', myDeck.length);
        const $grid = $('#card-gridview');
        const previousCount = $grid.children().length;
        $grid.empty();

        // 按费用升序排序
        const sortedDeck = [...myDeck].sort((a, b) => (a.cost || 0) - (b.cost || 0));

        sortedDeck.forEach((card, index) => {
            const $card = $(createCardHtml(card));
            
            // 如果是新添加的卡牌（最后一张），添加动画
            if (hasNewCard && index === sortedDeck.length - 1) {
                $card.addClass('newly-added');
                setTimeout(() => {
                    $card.removeClass('newly-added');
                }, 400);
            }

            // 将卡牌数据绑定到DOM元素上
            $card.data('card-data', card);

            $card.on('contextmenu', (e) => {
                e.preventDefault();
                if (typeof ContextMenu !== 'undefined') {
                    const cardData = $(e.currentTarget).data('card-data');
                    ContextMenu.show(e, [
                        {
                            icon: '🗑️',
                            label: '从卡组中删除',
                            action: () => {
                                // 找到并删除第一个匹配的卡牌
                                const idx = myDeck.findIndex(c => c.code === cardData.code);
                                if (idx !== -1) {
                                    myDeck.splice(idx, 1);
                                    console.log('删除后卡组大小:', myDeck.length);
                                    updateDeckDisplay();
                                }
                            }
                        }
                    ]);
                }
            });

            $grid.append($card);
        });

        updateStats();
    }

    // 更新统计
    function updateStats() {
        $('#deck-count').text(myDeck.length);
        
        let totalDust = 0;
        myDeck.forEach(card => {
            totalDust += getDustValue(card.rarity);
        });
        $('#deck-dust').text(totalDust);
    }

    // 获取尘数
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
        
        return `
            <div class="card ${rarityClass} card-type-${typeEn.toLowerCase()}" data-code="${card.code}" data-keywords='${JSON.stringify(keywords)}' data-mark='${mark.replace(/'/g, "\\'")}'>
                <div class="card-inner">
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
        `;
    }

    // 格式化时间
    function formatTime(timeStr) {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) return '刚刚';
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
        if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
        return date.toLocaleDateString('zh-CN');
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
