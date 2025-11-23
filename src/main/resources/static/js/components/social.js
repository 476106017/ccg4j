$(document).ready(function() {
    // Inject Social Panel HTML
    const socialHtml = `
        <div class="social-panel" id="social-panel">
            <div class="social-header" id="social-header">
                <span>社交</span>
                <span id="social-toggle-icon">▲</span>
            </div>
            <div class="social-tabs">
                <div class="social-tab active" data-tab="chat">聊天</div>
                <div class="social-tab" data-tab="friends">好友</div>
            </div>
            <div class="social-content">
                <div class="social-view active" id="view-chat">
                    <div class="chat-messages" id="chat-messages"></div>
                    <div class="chat-input-area">
                        <input type="text" class="chat-input" id="chat-input" placeholder="输入消息..." />
                        <button class="chat-send-btn" id="chat-send-btn">发送</button>
                    </div>
                </div>
                <div class="social-view" id="view-friends">
                    <div class="add-friend-area">
                        <input type="text" class="chat-input" id="add-friend-input" placeholder="输入用户名添加..." />
                        <button class="chat-send-btn" id="add-friend-btn">+</button>
                    </div>
                    <div class="friend-list" id="friend-list">
                        <!-- Friend items will be added here -->
                    </div>
                </div>
            </div>
        </div>
        
        <div class="context-menu" id="chat-context-menu">
            <div class="context-menu-item" id="ctx-chat-info">查看信息</div>
            <div class="context-menu-item" id="ctx-chat-add">关注</div>
            <div class="context-menu-item" id="ctx-chat-private">私聊</div>
            <div class="context-menu-item" id="ctx-chat-block">屏蔽发言</div>
            <div class="context-menu-item" id="ctx-chat-challenge">发起挑战</div>
        </div>

        <div class="modal fade" id="player-info-modal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">玩家信息</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" id="player-info-body">
                        <!-- Info loaded here -->
                    </div>
                </div>
            </div>
        </div>
    `;
    $('body').append(socialHtml);

    // State
    let currentChatTarget = null; // null for global, userId for private
    let currentChatTargetName = null;
    let contextMenuTargetId = null;
    let contextMenuTargetName = null;

    // Toggle Panel
    $('#social-header').click(function() {
        $('#social-panel').toggleClass('expanded');
        const icon = $('#social-panel').hasClass('expanded') ? '▼' : '▲';
        $('#social-toggle-icon').text(icon);
    });

    // Tab Switching
    $('.social-tab').click(function() {
        $('.social-tab').removeClass('active');
        $(this).addClass('active');
        $('.social-view').removeClass('active');
        $('#view-' + $(this).data('tab')).addClass('active');
    });

    // Send Message
    function sendMessage() {
        const content = $('#chat-input').val().trim();
        if (!content) return;

        if (currentChatTarget) {
            // Private Chat
            const payload = {
                type: 'chat_private',
                data: {
                    targetId: currentChatTarget,
                    content: content
                }
            };
            if (typeof websocket !== 'undefined' && websocket.readyState === WebSocket.OPEN) {
                websocket.send(JSON.stringify(payload));
            } else {
                showAlert('未连接服务器');
            }
        } else {
            // Global Chat
            const payload = {
                type: 'chat_global',
                data: {
                    content: content
                }
            };
            if (typeof websocket !== 'undefined' && websocket.readyState === WebSocket.OPEN) {
                websocket.send(JSON.stringify(payload));
            } else {
                showAlert('未连接服务器');
            }
        }
        $('#chat-input').val('');
    }

    $('#chat-send-btn').click(sendMessage);
    $('#chat-input').keypress(function(e) {
        if (e.which == 13) sendMessage();
    });

    // Handle Incoming Messages
    window.addEventListener('ws-message', function(e) {
        const data = e.detail;
        const channel = data.channel;
        const msgData = data.data;

        if (channel === 'chat_global') {
            appendMessage(msgData.sender, msgData.senderId, msgData.content, 'global', getCurrentTimestamp());
        } else if (channel === 'chat_private') {
            appendMessage(msgData.sender, msgData.senderId, msgData.content, 'private', getCurrentTimestamp());
        } else if (channel === 'challenge_request') {
            showConfirm(msgData.sender + " 向你发起挑战！是否接受？").then((result) => {
                if (result && typeof websocket !== 'undefined') {
                    websocket.send("joinRoom::" + msgData.roomId);
                }
            });
        }
    });

    function appendMessage(sender, senderId, content, type, timestamp) {
        const $msg = $('<div class="chat-message ' + type + '"></div>');
        const $sender = $('<span class="sender"></span>').text(sender + ': ');
        const $content = $('<span class="content"></span>').text(content);
        
        // Add timestamp if provided
        if (timestamp) {
            const $timestamp = $('<span class="timestamp"></span>').text('[' + timestamp + '] ');
            $msg.append($timestamp);
        }
        
        $sender.contextmenu(function(e) {
            e.preventDefault();
            if (!senderId) return; // System message or self?
            
            contextMenuTargetId = senderId;
            contextMenuTargetName = sender;
            
            // Check if menu would go off bottom of screen
            const menuHeight = 150; // Approximate menu height
            const windowHeight = $(window).height();
            const spaceBelow = windowHeight - e.pageY;
            
            let top = e.pageY;
            let left = e.pageX;
            
            // If menu would go off bottom, position it upward
            if (spaceBelow < menuHeight) {
                top = e.pageY - menuHeight;
            }
            
            $('#chat-context-menu').css({
                top: top + 'px',
                left: left + 'px'
            }).show();
            
            $(document).one('click', function() {
                $('#chat-context-menu').hide();
            });
        });

        $msg.append($sender).append($content);
        $('#chat-messages').append($msg);
        
        // Auto scroll
        const chatDiv = document.getElementById('chat-messages');
        chatDiv.scrollTop = chatDiv.scrollHeight;
    }
    
    // Context Menu Actions
    $('#ctx-chat-info').click(function() {
        if (!contextMenuTargetId) return;
        
        $.get('/friend/info', { userId: contextMenuTargetId }, function(res) {
            if (res.code === 200) {
                const user = res.data;
                const lastLogin = user.lastLoginDate ? user.lastLoginDate : '未知';
                const activity = user.activityScore ? user.activityScore : 0;
                
                let html = `
                    <p><strong>用户名:</strong> ${user.username}</p>
                    <p><strong>匹配分:</strong> ${user.matchRating}</p>
                    <p><strong>活跃度:</strong> ${activity}</p>
                    <p><strong>最后登录:</strong> ${lastLogin}</p>
                `;
                
                $('#player-info-body').html(html);
                new bootstrap.Modal(document.getElementById('player-info-modal')).show();
            } else {
                showAlert(res.msg);
            }
        });
    });
    
    $('#ctx-chat-add').click(function() {
        $.post('/friend/request', { username: contextMenuTargetName }, function(res) {
            showAlert(res.msg);
        });
    });
    
    $('#ctx-chat-private').click(function() {
        startPrivateChat(contextMenuTargetId, contextMenuTargetName);
    });
    
    $('#ctx-chat-block').click(function() {
        showConfirm('确定屏蔽 ' + contextMenuTargetName + ' 的发言吗？').then((result) => {
            if (result) {
                $.post('/friend/block', { blockId: contextMenuTargetId }, function(res) {
                    showAlert(res.msg);
                });
            }
        });
    });
    
    $('#ctx-chat-challenge').click(function() {
        if (!contextMenuTargetId) return;
        showConfirm('确定向 ' + contextMenuTargetName + ' 发起挑战吗？').then((result) => {
            if (result) {
                if (typeof websocket !== 'undefined' && websocket.readyState === WebSocket.OPEN) {
                    websocket.send("challenge::" + contextMenuTargetId);
                } else {
                    showAlert('未连接服务器');
                }
            }
        });
    });

    // Friend List Logic
    function loadFriendList() {
        $.get('/friend/list', function(res) {
            if (res.code === 200) {
                renderFriendList(res.data);
            }
        });
    }

    function renderFriendList(friends) {
        $('#friend-list').empty();
        friends.forEach(f => {
            const statusClass = f.online ? 'online' : 'offline';
            const $item = $(`
                <div class="friend-item" data-id="${f.id}" data-name="${f.username}">
                    <div class="friend-status ${statusClass}"></div>
                    <div class="friend-name">${f.username} (${f.status})</div>
                    <div class="friend-actions">
                        <button class="friend-action-btn btn-chat"><i class="bi bi-chat-dots"></i></button>
                        <button class="friend-action-btn btn-remove"><i class="bi bi-x"></i></button>
                    </div>
                </div>
            `);
            
            $item.find('.btn-chat').click(function(e) {
                e.stopPropagation();
                startPrivateChat(f.id, f.username);
            });
            
            $item.find('.btn-remove').click(function(e) {
                e.stopPropagation();
                showConfirm('确定删除好友 ' + f.username + '?').then((result) => {
                    if (result) {
                        removeFriend(f.id);
                    }
                });
            });

            $('#friend-list').append($item);
        });
    }

    function startPrivateChat(userId, username) {
        currentChatTarget = userId;
        currentChatTargetName = username;
        $('#chat-input').attr('placeholder', '私聊 @' + username + '...');
        $('.social-tab[data-tab="chat"]').click();
    }

    function removeFriend(friendId) {
        $.post('/friend/remove', { friendId: friendId }, function(res) {
            if (res.code === 200) {
                loadFriendList();
            } else {
                showAlert(res.msg);
            }
        });
    }

    $('#add-friend-btn').click(function() {
        const username = $('#add-friend-input').val().trim();
        if (!username) return;
        
        $.post('/friend/request', { username: username }, function(res) {
            showAlert(res.msg);
            if (res.code === 200) {
                $('#add-friend-input').val('');
                loadFriendList();
            }
        });
    });

    // Helper function to get current timestamp  
    function getCurrentTimestamp() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        return hours + ':' + minutes + ':' + seconds;
    }

    // Load chat history
    function loadChatHistory() {
        $.get('/chat/history', function(res) {
            if (res.code === 200 && res.data) {
                $('#chat-messages').empty(); // Clear existing messages
                res.data.forEach(msg => {
                    appendMessage(
                        msg.senderName, 
                        msg.senderId, 
                        msg.content, 
                        msg.type.toLowerCase(), 
                        msg.timestamp
                    );
                });
            }
        }).fail(function() {
            console.log('Failed to load chat history');
        });
    }

    // Initial Load
    // Load chat history when page loads
    loadChatHistory();
    
    // Load friend list when tab is clicked
    $('.social-tab[data-tab="friends"]').click(loadFriendList);
    
    // Also load once on startup if logged in
    loadFriendList();
});
