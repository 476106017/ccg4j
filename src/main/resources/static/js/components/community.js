// Community Feature Logic

let currentChannelId = null;
let currentPostId = null;

// --- Channel Management ---

function loadChannels() {
    Request.get('/community/channels')
        .then(response => {
            if (response.code === 200) {
                renderChannelList(response.data);
            } else {
                window.showAlert(response.msg || '加载频道失败');
            }
        });
}

function renderChannelList(channels) {
    const list = $('#channel-list');
    list.empty();
    
    channels.forEach(channel => {
        const activeClass = channel.id === currentChannelId ? 'active' : '';
        const pinnedIcon = channel.isPinned ? '<i class="bi bi-pin-angle-fill text-danger me-1"></i>' : '';
        const lockIcon = channel.type === 'PRIVATE' ? '<i class="bi bi-lock-fill text-warning me-1"></i>' : '';
        const levelBadge = `<span class="badge bg-secondary ms-1" style="font-size: 0.7em;">Lv.${channel.level || 1}</span>`;
        
        const item = $(`
            <div class="list-group-item ${activeClass}" onclick="selectChannel(${channel.id})">
                <div class="d-flex justify-content-between align-items-center">
                    <h6 class="mb-1 text-truncate">${pinnedIcon}${lockIcon}${channel.name}${levelBadge}</h6>
                </div>
                <p class="mb-1 small text-muted text-truncate">${channel.description || '无描述'}</p>
                <div class="channel-meta d-flex justify-content-between">
                    <span>热度: ${channel.heat || 0}</span>
                    <span>${channel.lastActivityAt ? formatTime(channel.lastActivityAt) : ''}</span>
                </div>
            </div>
        `);
        list.append(item);
    });
}

function selectChannel(channelId) {
    currentChannelId = channelId;
    currentPostId = null;
    loadChannels(); // Refresh to update active state
    loadPosts(channelId);
}

function showCreateChannelModal() {
    $('#new-channel-name').val('');
    $('#new-channel-desc').val('');
    $('#new-channel-type').val('PRIVATE');
    new bootstrap.Modal(document.getElementById('create-channel-modal')).show();
}

function createChannel() {
    const name = $('#new-channel-name').val();
    const desc = $('#new-channel-desc').val();
    const type = $('#new-channel-type').val();
    
    if (!name) {
        window.showAlert('请输入频道名称');
        return;
    }
    
    Request.post('/community/channels', {
        name: name,
        description: desc,
        type: type
    }).then(response => {
        if (response.code === 200) {
            bootstrap.Modal.getInstance(document.getElementById('create-channel-modal')).hide();
            window.showAlert('频道创建成功');
            loadChannels();
        } else {
            window.showAlert(response.msg || '创建失败');
        }
    });
}

// --- Post Management ---

function loadPosts(channelId) {
    const container = $('#community-content-area');
    container.html('<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>');
    
    Request.get(`/community/channels/${channelId}/posts`)
        .then(response => {
            if (response.code === 200) {
                renderPostList(response.data);
            } else {
                container.html(`<div class="alert alert-danger m-3">${response.msg || '加载帖子失败'}</div>`);
            }
        });
}

function renderPostList(posts) {
    const container = $('#community-content-area');
    container.empty();
    
    const header = $(`
        <div class="d-flex justify-content-between align-items-center mb-3 p-3 border-bottom">
            <h5 class="m-0">帖子列表</h5>
            <div>
                <button class="btn btn-outline-secondary btn-sm me-2" onclick="showChannelSettings()">设置</button>
                <button class="btn btn-primary btn-sm" onclick="showCreatePostModal()">发布帖子</button>
            </div>
        </div>
        <div class="p-3" style="overflow-y: auto; height: calc(100% - 70px);">
            <div id="post-list-container"></div>
        </div>
    `);
    container.append(header);
    
    const listContainer = $('#post-list-container');
    
    if (posts.length === 0) {
        listContainer.html('<div class="text-center text-muted mt-5">暂无帖子，快来抢沙发吧！</div>');
        return;
    }
    
    posts.forEach(post => {
        const voteScore = (post.upvotes || 0) - (post.downvotes || 0);
        const item = $(`
            <div class="post-item d-flex" onclick="loadPostDetail(${post.id})">
                <div class="post-vote-box" onclick="event.stopPropagation()">
                    <div class="vote-count">${voteScore}</div>
                </div>
                <div class="post-content-preview">
                    <div class="post-title">${post.title}</div>
                    <div class="post-meta">
                        <span class="me-2"><i class="bi bi-person"></i> ${post.authorName}</span>
                        <span class="me-2"><i class="bi bi-clock"></i> ${formatTime(post.createdAt)}</span>
                        <span><i class="bi bi-chat-dots"></i> 最后回复: ${formatTime(post.lastReplyAt)}</span>
                    </div>
                </div>
            </div>
        `);
        listContainer.append(item);
    });
}

function showCreatePostModal() {
    if (!currentChannelId) return;
    $('#new-post-title').val('');
    $('#new-post-content').val('');
    new bootstrap.Modal(document.getElementById('create-post-modal')).show();
}

function createPost() {
    const title = $('#new-post-title').val();
    const content = $('#new-post-content').val();
    
    if (!title || !content) {
        window.showAlert('标题和内容不能为空');
        return;
    }
    
    Request.post('/community/posts', {
        channelId: currentChannelId,
        title: title,
        content: content
    }).then(response => {
        if (response.code === 200) {
            bootstrap.Modal.getInstance(document.getElementById('create-post-modal')).hide();
            loadPosts(currentChannelId);
        } else {
            window.showAlert(response.msg || '发布失败');
        }
    });
}

// --- Post Detail & Replies ---

function loadPostDetail(postId) {
    currentPostId = postId;
    const container = $('#community-content-area');
    container.html('<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>');
    
    Request.get(`/community/posts/${postId}`)
        .then(response => {
            if (response.code === 200) {
                renderPostDetail(response.data);
            } else {
                container.html(`<div class="alert alert-danger m-3">${response.msg || '加载详情失败'}</div>`);
            }
        });
}

function renderPostDetail(data) {
    const post = data.post;
    const replies = data.replies;
    const userVote = data.userVote || 0; // 1, -1, 0
    
    const container = $('#community-content-area');
    container.empty();
    
    const detailHtml = `
        <div class="post-detail-container">
            <div class="mb-3">
                <button class="btn btn-link text-decoration-none p-0" onclick="loadPosts(${currentChannelId})">
                    <i class="bi bi-arrow-left"></i> 返回列表
                </button>
            </div>
            
            <div class="post-detail-header">
                <div class="d-flex align-items-start">
                    <div class="d-flex flex-column align-items-center me-3">
                        <i class="bi bi-caret-up-fill vote-btn ${userVote === 1 ? 'upvoted' : ''}" onclick="vote('POST', ${post.id}, 1)"></i>
                        <span class="vote-count">${(post.upvotes || 0) - (post.downvotes || 0)}</span>
                        <i class="bi bi-caret-down-fill vote-btn ${userVote === -1 ? 'downvoted' : ''}" onclick="vote('POST', ${post.id}, -1)"></i>
                    </div>
                    <div class="flex-grow-1">
                        <h3 class="post-detail-title">${post.title}</h3>
                        <div class="text-muted small mb-3">
                            发布者: ${data.authorName} | 时间: ${formatTime(post.createdAt)}
                        </div>
                        <div class="post-detail-content">${post.content}</div>
                    </div>
                </div>
            </div>
            
            <div class="mb-4">
                <h5>发表回复</h5>
                <textarea class="form-control mb-2" id="main-reply-content" rows="3"></textarea>
                <button class="btn btn-primary btn-sm" onclick="submitReply(null)">提交回复</button>
            </div>
            
            <div class="reply-tree">
                <h5>评论 (${countReplies(replies)})</h5>
                <div id="replies-container">
                    ${renderRepliesRecursive(replies, data.userReplyVotes || {})}
                </div>
            </div>
        </div>
    `;
    
    container.html(detailHtml);
}

function countReplies(replies) {
    let count = 0;
    if (!replies) return 0;
    replies.forEach(r => {
        count++;
        if (r.children) count += countReplies(r.children);
    });
    return count;
}

function renderRepliesRecursive(replies, userVotes) {
    if (!replies || replies.length === 0) return '';
    userVotes = userVotes || {};
    
    let html = '';
    replies.forEach(reply => {
        const voteScore = (reply.upvotes || 0) - (reply.downvotes || 0);
        const userVote = userVotes[reply.id] || 0;
        html += `
            <div class="reply-item" id="reply-${reply.id}">
                <div class="reply-header d-flex justify-content-between">
                    <span>${reply.authorName} · ${formatTime(reply.createdAt)}</span>
                    <div class="d-flex align-items-center gap-2">
                        <i class="bi bi-caret-up-fill vote-btn ${userVote === 1 ? 'upvoted' : ''}" onclick="vote('REPLY', ${reply.id}, 1)"></i>
                        <span>${voteScore}</span>
                        <i class="bi bi-caret-down-fill vote-btn ${userVote === -1 ? 'downvoted' : ''}" onclick="vote('REPLY', ${reply.id}, -1)"></i>
                    </div>
                </div>
                <div class="reply-content">${reply.content}</div>
                <div class="reply-actions">
                    <span class="reply-action-btn" onclick="toggleReplyForm(${reply.id})">回复</span>
                    <span class="reply-action-btn text-danger" onclick="showBanModal(${reply.authorId})">禁言</span>
                    <span class="reply-action-btn text-danger" onclick="deleteReply(${reply.id})">删除</span>
                </div>
                
                <div class="reply-form" id="reply-form-${reply.id}">
                    <textarea class="form-control mb-2" rows="2"></textarea>
                    <button class="btn btn-primary btn-sm me-2" onclick="submitReply(${reply.id})">发送</button>
                    <button class="btn btn-secondary btn-sm" onclick="toggleReplyForm(${reply.id})">取消</button>
                </div>
                
                <div class="nested-replies">
                    ${renderRepliesRecursive(reply.children, userVotes)}
                </div>
            </div>
        `;
    });
    return html;
}

function toggleReplyForm(replyId) {
    $(`#reply-form-${replyId}`).toggleClass('active');
}

function submitReply(parentId) {
    let content;
    if (parentId) {
        content = $(`#reply-form-${parentId} textarea`).val();
    } else {
        content = $('#main-reply-content').val();
    }
    
    if (!content) {
        window.showAlert('请输入回复内容');
        return;
    }
    
    Request.post('/community/replies', {
        postId: currentPostId,
        parentId: parentId,
        content: content
    }).then(response => {
        if (response.code === 200) {
            loadPostDetail(currentPostId); // Reload to show new reply
        } else {
            window.showAlert(response.msg || '回复失败');
        }
    });
}

function deleteReply(replyId) {
    window.showConfirm('确定要删除这条回复吗？').then(confirmed => {
        if (confirmed) {
            Request.delete(`/community/replies/${replyId}`).then(response => {
                if (response.code === 200) {
                    loadPostDetail(currentPostId);
                } else {
                    window.showAlert(response.msg || '删除失败');
                }
            });
        }
    });
}

// --- Voting ---

function vote(targetType, targetId, voteType) {
    Request.post('/community/vote', {
        targetType: targetType,
        targetId: targetId,
        voteType: voteType
    }).then(response => {
        if (response.code === 200) {
            // Ideally update just the vote count locally, but reloading is easier for now
            if (targetType === 'POST') {
                loadPostDetail(currentPostId);
            } else {
                loadPostDetail(currentPostId); // Reloading whole post for reply vote update is a bit heavy but safe
            }
        } else {
            window.showAlert(response.msg || '操作失败');
        }
    });
}

// --- Admin/Mod Actions ---

function showBanModal(userId) {
    $('#ban-user-id').val(userId);
    $('#ban-channel-id').val(currentChannelId);
    new bootstrap.Modal(document.getElementById('ban-user-modal')).show();
}

function confirmBanUser() {
    const userId = $('#ban-user-id').val();
    const channelId = $('#ban-channel-id').val();
    const duration = $('#ban-duration').val();
    
    Request.post(`/community/channels/${channelId}/ban`, {
        userId: userId,
        durationType: duration
    }).then(response => {
        if (response.code === 200) {
            bootstrap.Modal.getInstance(document.getElementById('ban-user-modal')).hide();
            window.showAlert('用户已禁言');
        } else {
            window.showAlert(response.msg || '操作失败');
        }
    });
}

function showChannelSettings() {
    // TODO: Implement channel settings modal (whitelist, pin, etc.)
    // For now just a placeholder or basic info
    window.showAlert('频道设置功能开发中...');
}

// --- Utils ---

function formatTime(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleString();
}

// --- Initialization ---

$(document).ready(function() {
    // Bind tab event to load channels when switching to community tab
    $('button[data-bs-target="#community-panel"]').on('shown.bs.tab', function (e) {
        loadChannels();
    });
});
