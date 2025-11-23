// Workshop Logic

let currentWorkshopTab = 'SUBMITTED'; // 'SUBMITTED' or 'IMPLEMENTED'
let currentWorkshopSort = 'newest'; // 'newest' or 'likes'

$(document).ready(function() {
    // Tab switching
    $('#workshop-submitted-tab').click(function() {
        currentWorkshopTab = 'SUBMITTED';
        $(this).addClass('active');
        $('#workshop-implemented-tab').removeClass('active');
        loadWorkshopCards();
    });

    $('#workshop-implemented-tab').click(function() {
        currentWorkshopTab = 'IMPLEMENTED';
        $(this).addClass('active');
        $('#workshop-submitted-tab').removeClass('active');
        loadWorkshopCards();
    });

    // Sort switching
    $('#workshop-sort-select').change(function() {
        currentWorkshopSort = $(this).val();
        loadWorkshopCards();
    });

    // Initial load when tab is shown
    $('button[data-bs-target="#workshop-panel"]').on('shown.bs.tab', function (e) {
        loadWorkshopCards();
    });
    
    // Also load if we are already on the tab (e.g. refresh)
    if ($('#workshop-panel').hasClass('active')) {
        loadWorkshopCards();
    }
    
    // Initialize form stats visibility
    updateWorkshopFormStats();
});

function loadWorkshopCards() {
    $('#workshop-card-list').html('<div class="text-center p-5"><div class="spinner-border text-primary" role="status"></div></div>');
    
    $.get('/api/workshop/cards', { status: currentWorkshopTab, sortBy: currentWorkshopSort }, function(cards) {
        $('#workshop-card-list').empty();
        if (cards.length === 0) {
            $('#workshop-card-list').html('<div class="col-12 text-center text-muted p-5">暂无卡牌</div>');
            return;
        }

        cards.forEach(card => {
            const standardCard = adaptWorkshopCardToStandard(card);
            const cardHtmlStr = window.cardHtml(standardCard);
            // Wrap in column and add click handler
            const wrapper = `
                <div class="col-md-6 col-lg-4 mb-4" onclick="showWorkshopCardDetail(${card.id})">
                    <div class="workshop-card-wrapper" style="cursor: pointer;">
                        ${cardHtmlStr}
                        <div class="workshop-card-stats text-center mt-2">
                            <span class="text-primary me-2"><i class="bi bi-hand-thumbs-up-fill"></i> ${card.likes}</span>
                            <small class="text-muted">by ${escapeHtml(card.authorName)}</small>
                        </div>
                    </div>
                </div>
            `;
            $('#workshop-card-list').append(wrapper);
        });
    }).fail(function() {
        $('#workshop-card-list').html('<div class="col-12 text-center text-danger p-5">加载失败，请稍后重试</div>');
    });
}

let isFetchingDetail = false;

function showWorkshopCardDetail(cardId) {
    if (isFetchingDetail) return;
    isFetchingDetail = true;

    // Show loading or just wait? Better to show a spinner if it takes time, but debounce is key.
    // Let's add a small spinner to the clicked card if possible, or just global cursor wait.
    $('body').css('cursor', 'wait');

    $.get('/api/workshop/cards/' + cardId, function(data) {
        $('body').css('cursor', 'default');
        isFetchingDetail = false;
        
        // Check if we are still on the workshop tab or if the modal is relevant?
        // User said "switch to other tab then popup", so we should check if workshop panel is active.
        if (!$('#workshop-panel').hasClass('active')) return;

        const card = data.card;
        const comments = data.comments;
        const hasVoted = data.hasVoted;
        const isAdmin = data.isAdmin;
        const currentUserId = data.currentUserId;

        const standardCard = adaptWorkshopCardToStandard(card);
        $('#workshop-detail-preview').html(window.cardHtml(standardCard));
        
        $('#workshop-detail-title').text(card.name);
        $('#workshop-detail-author').text(card.authorName);
        $('#workshop-detail-likes').text(card.likes);
        
        // Vote button state
        const voteBtn = $('#workshop-vote-btn');
        if (hasVoted) {
            voteBtn.removeClass('btn-outline-primary').addClass('btn-primary');
            voteBtn.html('<i class="bi bi-hand-thumbs-up-fill"></i> 已赞');
        } else {
            voteBtn.removeClass('btn-primary').addClass('btn-outline-primary');
            voteBtn.html('<i class="bi bi-hand-thumbs-up"></i> 点赞');
        }
        voteBtn.off('click').click(function() {
            voteWorkshopCard(card.id);
        });

        // Action Buttons Container
        const actionContainer = voteBtn.parent();
        // Remove any existing dynamic buttons (implement/withdraw) to avoid dupes
        actionContainer.find('.dynamic-btn').remove();

        // Implement button (Admin only)
        if (isAdmin && card.status === 'SUBMITTED') {
            const implementBtn = $(`<button class="btn btn-success dynamic-btn ms-2"><i class="bi bi-check-lg"></i> 实装</button>`);
            implementBtn.click(() => implementWorkshopCard(card.id));
            actionContainer.append(implementBtn);
        }

        // Withdraw button (Author or Admin)
        if (currentUserId && (card.authorId === currentUserId || isAdmin)) {
            const withdrawBtn = $(`<button class="btn btn-danger dynamic-btn ms-2"><i class="bi bi-trash"></i> 撤回</button>`);
            withdrawBtn.click(() => withdrawWorkshopCard(card.id));
            actionContainer.append(withdrawBtn);
        }

        // Comments
        const commentList = $('#workshop-comment-list');
        commentList.empty();
        // Add max-height and scroll
        commentList.css({
            'max-height': '300px',
            'overflow-y': 'auto'
        });
        
        comments.forEach(comment => {
            commentList.append(`
                <div class="workshop-comment-item">
                    <div class="d-flex justify-content-between">
                        <strong>${escapeHtml(comment.authorName)}</strong>
                        <small class="text-muted">${formatDate(comment.createdAt)}</small>
                    </div>
                    <p class="mb-0 mt-1">${escapeHtml(comment.content)}</p>
                </div>
            `);
        });

        // Comment submit
        $('#workshop-comment-submit').off('click').click(function() {
            submitWorkshopComment(card.id);
        });

        new bootstrap.Modal(document.getElementById('workshopDetailModal')).show();
    }).fail(function() {
        $('body').css('cursor', 'default');
        isFetchingDetail = false;
        // Optional: alert error
    });
}

function voteWorkshopCard(cardId) {
    $.post(`/api/workshop/cards/${cardId}/vote`, function() {
        // Refresh detail to update like count and button state
        // Or just toggle locally for better UX, but refreshing is safer for sync
        // Let's just close and reopen or partial update?
        // Simple: close and reload list, but that closes modal.
        // Better: fetch detail again and update UI
        $.get('/api/workshop/cards/' + cardId, function(data) {
            $('#workshop-detail-likes').text(data.card.likes);
            const voteBtn = $('#workshop-vote-btn');
            if (data.hasVoted) {
                voteBtn.removeClass('btn-outline-primary').addClass('btn-primary');
                voteBtn.html('<i class="bi bi-hand-thumbs-up-fill"></i> 已赞');
            } else {
                voteBtn.removeClass('btn-primary').addClass('btn-outline-primary');
                voteBtn.html('<i class="bi bi-hand-thumbs-up"></i> 点赞');
            }
            loadWorkshopCards(); // Refresh background list
        });
    });
}

function submitWorkshopComment(cardId) {
    const content = $('#workshop-comment-input').val();
    if (!content.trim()) return;

    $.ajax({
        url: `/api/workshop/cards/${cardId}/comment`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ content: content }),
        success: function() {
            $('#workshop-comment-input').val('');
            // Refresh comments
            $.get('/api/workshop/cards/' + cardId, function(data) {
                const comments = data.comments;
                const commentList = $('#workshop-comment-list');
                commentList.empty();
                comments.forEach(comment => {
                    commentList.append(`
                        <div class="workshop-comment-item">
                            <div class="d-flex justify-content-between">
                                <strong>${escapeHtml(comment.authorName)}</strong>
                                <small class="text-muted">${formatDate(comment.createdAt)}</small>
                            </div>
                            <p class="mb-0 mt-1">${escapeHtml(comment.content)}</p>
                        </div>
                    `);
                });
            });
        },
        error: function(xhr) {
            alert(xhr.responseText || '评论失败');
        }
    });
}

function implementWorkshopCard(cardId) {
    if (!confirm('确定要实装这张卡牌吗？')) return;
    
    $.post(`/api/workshop/cards/${cardId}/implement`, function() {
        alert('实装成功');
        $('#workshopDetailModal').modal('hide');
        loadWorkshopCards();
    }).fail(function(xhr) {
        alert(xhr.responseText || '操作失败');
    });
}

function withdrawWorkshopCard(cardId) {
    if (!confirm('确定要撤回这张卡牌吗？此操作不可恢复。')) return;
    
    $.ajax({
        url: `/api/workshop/cards/${cardId}`,
        type: 'DELETE',
        success: function() {
            alert('撤回成功');
            $('#workshopDetailModal').modal('hide');
            loadWorkshopCards();
        },
        error: function(xhr) {
            alert(xhr.responseText || '操作失败');
        }
    });
}

function updateWorkshopFormStats() {
    const type = $('#ws-type').val();
    const attackField = $('#field-attack');
    const healthField = $('#field-health');
    const countdownField = $('#field-countdown');
    
    // Reset all fields
    attackField.addClass('d-none');
    healthField.addClass('d-none');
    countdownField.addClass('d-none');
    
    switch(type) {
        case 'FOLLOWER':
            // 随从：攻击力 + 生命值
            attackField.removeClass('d-none');
            healthField.removeClass('d-none');
            break;
        case 'SPELL':
            // 法术：无数值
            break;
        case 'AMULET':
            // 护符：倒计时
            countdownField.removeClass('d-none');
            countdownField.find('label').text('倒计时');
            break;
        case 'EQUIPMENT':
            // 装备：攻击力 + 耐久度
            attackField.removeClass('d-none');
            countdownField.removeClass('d-none');
            countdownField.find('label').text('耐久度');
            break;
    }
}

function submitWorkshopCard() {
    const type = $('#ws-type').val();
    const card = {
        name: $('#ws-name').val(),
        description: $('#ws-desc').val(),
        cost: parseInt($('#ws-cost').val()),
        cardType: type,
        job: $('#ws-job').val() || '中立',
        race: $('#ws-race').val() || null
    };
    
    // Add stats based on type
    if (type === 'FOLLOWER') {
        card.attack = parseInt($('#ws-attack').val());
        card.health = parseInt($('#ws-health').val());
        card.countdown = null;
    } else if (type === 'SPELL') {
        card.attack = 0;
        card.health = 0;
        card.countdown = null;
    } else if (type === 'AMULET') {
        card.attack = 0;
        card.health = 0;
        card.countdown = parseInt($('#ws-countdown').val());
    } else if (type === 'EQUIPMENT') {
        card.attack = parseInt($('#ws-attack').val());
        card.health = 0;
        card.countdown = parseInt($('#ws-countdown').val());
    }

    if (!card.name) {
        alert('请填写完整信息');
        return;
    }

    // Check for duplicate names (client-side pre-check)
    // Backend should also validate

    $.ajax({
        url: '/api/workshop/cards',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(card),
        success: function() {
            alert('提交成功');
            $('#createCardModal').modal('hide');
            $('#create-card-form')[0].reset();
            updateWorkshopFormStats();
            loadWorkshopCards();
        },
        error: function(xhr) {
            alert(xhr.responseText || '提交失败');
        }
    });
}

// Helpers
function getJobName(job) {
    const map = {
        'NEUTRAL': '中立',
        'ELF': '精灵',
        'ROYAL': '皇家',
        'WITCH': '巫师',
        'DRAGON': '龙族',
        'NECRO': '死灵',
        'VAMPIRE': '吸血鬼',
        'BISHOP': '主教',
        'NEMESIS': '复仇者'
    };
    return map[job] || job;
}

function getJobColor(job) {
    const map = {
        'NEUTRAL': 'secondary',
        'ELF': 'success',
        'ROYAL': 'warning',
        'WITCH': 'primary',
        'DRAGON': 'danger',
        'NECRO': 'dark',
        'VAMPIRE': 'danger',
        'BISHOP': 'warning', // light is too light
        'NEMESIS': 'info'
    };
    return map[job] || 'secondary';
}

function getCardTypeName(type) {
    const map = {
        'FOLLOWER': '随从',
        'SPELL': '法术',
        'AMULET': '护符',
        'EQUIPMENT': '装备'
    };
    return map[type] || type;
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function formatDate(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleString('zh-CN', { hour12: false }); 
}

function adaptWorkshopCardToStandard(workshopCard) {
    // cardHtml expects Chinese 'type' and will convert it to English 'TYPE' internally
    // This ensures both display (Chinese) and logic (English) work correctly
    let typeMapping = {
        'FOLLOWER': '随从',
        'SPELL': '法术',
        'AMULET': '护符',
        'EQUIPMENT': '装备'
    };
    
    return {
        id: workshopCard.id,
        name: workshopCard.name,
        mark: workshopCard.description, // Map description to mark
        cost: workshopCard.cost,
        atk: workshopCard.attack || 0, // Map attack to atk
        hp: workshopCard.health || 0, // Map health to hp
        maxHp: workshopCard.health || 0,
        countDown: workshopCard.countdown || 0, // IMPORTANT: cardHtml uses countDown, not countdown
        type: typeMapping[workshopCard.cardType] || workshopCard.cardType, // Chinese type for cardHtml to convert
        job: workshopCard.job,
        race: workshopCard.race ? [workshopCard.race] : [],
        rarity: 'BRONZE', // Default rarity
        keywords: [], // Parse keywords if needed, or leave empty
        equipment: null
    };
}
