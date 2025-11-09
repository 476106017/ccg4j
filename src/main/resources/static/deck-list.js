$(document).ready(function() {
    let currentUser = null;
    let currentDeckId = null;

    // 检查登录状态
    checkLoginStatus();

    // 加载卡组列表
    loadDecks();

    // 创建卡组按钮
    $('#create-deck-btn').click(function() {
        $('#deck-name-input').val('');
        new bootstrap.Modal($('#createDeckModal')).show();
    });

    // 确认创建
    $('#confirm-create-btn').click(function() {
        const deckName = $('#deck-name-input').val().trim();
        if (!deckName) {
            alert('请输入卡组名称');
            return;
        }
        createDeck(deckName);
    });

    // 确认重命名
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

    // 退出登录
    $('#logout-btn').click(function() {
        $.post('/api/auth/logout', function() {
            window.location.href = 'index.html';
        });
    });

    // 检查登录状态
    function checkLoginStatus() {
        $.get('/api/auth/status', function(data) {
            if (!data.loggedIn) {
                window.location.href = 'index.html';
                return;
            }
            currentUser = data.user;
            $('#username-display').text(currentUser.username);
            $('#dust-display').text('奥术之尘: ' + (currentUser.arcaneDust || 0));
        }).fail(function() {
            window.location.href = 'index.html';
        });
    }

    // 加载卡组列表
    function loadDecks() {
        $.get('/api/user/deck/list', function(decks) {
            renderDecks(decks);
        }).fail(function(xhr) {
            if (xhr.status === 401) {
                window.location.href = 'index.html';
            } else {
                alert('加载卡组失败');
            }
        });
    }

    // 渲染卡组列表
    function renderDecks(decks) {
        const grid = $('#deck-grid');
        grid.empty();

        if (decks.length === 0) {
            grid.html(`
                <div class="col-12">
                    <div class="empty-state">
                        <div class="empty-state-icon">📚</div>
                        <div class="empty-state-text">还没有卡组</div>
                        <div class="empty-state-hint">点击右上角"创建新卡组"开始构筑</div>
                    </div>
                </div>
            `);
            return;
        }

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
                            <div class="deck-actions">
                                <button class="deck-action-btn rename-btn" title="重命名">
                                    ✏️
                                </button>
                                <button class="deck-action-btn delete-btn" title="删除">
                                    🗑️
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            grid.append(cardHtml);
        });

        // 绑定卡组卡片点击事件
        $('.deck-card').click(function(e) {
            // 如果点击的是按钮，不触发卡片点击
            if ($(e.target).closest('.deck-action-btn').length > 0) {
                return;
            }
            const deckId = $(this).data('deck-id');
            window.location.href = `deck-edit.html?id=${deckId}`;
        });

        // 绑定重命名按钮
        $('.rename-btn').click(function(e) {
            e.stopPropagation();
            const deckId = $(this).closest('.deck-card').data('deck-id');
            const deckName = $(this).closest('.deck-card').find('.deck-card-name').text();
            currentDeckId = deckId;
            $('#rename-input').val(deckName);
            new bootstrap.Modal($('#renameDeckModal')).show();
        });

        // 绑定删除按钮
        $('.delete-btn').click(function(e) {
            e.stopPropagation();
            const deckId = $(this).closest('.deck-card').data('deck-id');
            const deckName = $(this).closest('.deck-card').find('.deck-card-name').text();
            if (confirm(`确定要删除卡组"${deckName}"吗？`)) {
                deleteDeck(deckId);
            }
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
                deckData: '' // 空卡组
            }),
            success: function(deck) {
                bootstrap.Modal.getInstance($('#createDeckModal')).hide();
                // 直接跳转到编辑页面
                window.location.href = `deck-edit.html?id=${deck.id}`;
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    window.location.href = 'index.html';
                } else {
                    alert('创建卡组失败: ' + (xhr.responseJSON?.message || '未知错误'));
                }
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
                loadDecks();
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    window.location.href = 'index.html';
                } else {
                    alert('重命名失败: ' + (xhr.responseJSON?.message || '未知错误'));
                }
            }
        });
    }

    // 删除卡组
    function deleteDeck(deckId) {
        $.ajax({
            url: `/api/user/deck/${deckId}`,
            method: 'DELETE',
            success: function() {
                loadDecks();
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    window.location.href = 'index.html';
                } else {
                    alert('删除失败: ' + (xhr.responseJSON?.message || '未知错误'));
                }
            }
        });
    }

    // 格式化时间
    function formatTime(timeStr) {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // 1分钟内
            return '刚刚';
        } else if (diff < 3600000) { // 1小时内
            return Math.floor(diff / 60000) + '分钟前';
        } else if (diff < 86400000) { // 1天内
            return Math.floor(diff / 3600000) + '小时前';
        } else if (diff < 604800000) { // 7天内
            return Math.floor(diff / 86400000) + '天前';
        } else {
            return date.toLocaleDateString('zh-CN');
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
        return text.replace(/[&<>"']/g, m => map[m]);
    }
});
