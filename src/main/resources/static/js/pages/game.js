// 工具函数和组件已提取到 common/utils.js, components/card.js, components/leader-status.js

// 进入某个模式（选择/攻击）后用这个
var initBoard = function(){
    $(document).off("click.cancelSkill");
    $('#enemy-info').removeClass("selected");
    $('#enemy-info').unbind();
    $('#my-info').removeClass("selected");
    $('#my-info').unbind();
    $(".end-button").html("结束<br/>回合");
    $(".end-button").css("background","radial-gradient(blue, #2f4f4f9f)");
    drawBoard();
}
var clearBoard = function(){
    $('#enemy-hero').empty();
    $('#enemy-hand').empty();
    $('#enemy-battlefield').empty();
    $('#my-battlefield').empty();
    $('#my-hand').empty();
    $('#my-hero').empty();
}
var drawBoard = function(){
    clearBoard();

    $('#enemy-info').addClass('id-'+boardInfo.enemy.leader.id);
    $('#my-info').addClass('id-'+boardInfo.me.leader.id);
    
    // 生成主战者状态/效果的警告图标（黄色感叹号）
    function generateStatusWarning(leaderStatuses, playerType ) {
        if (!leaderStatuses || leaderStatuses.length === 0) {
            return '';
        }
        var tooltipContent = leaderStatuses.map(function(status) {
            if (status.type === 'status') {
                return status.label + '(' + status.value + '): ' + status.description;
            } else {
                return '效果: ' + status.label;
            }
        }).join(' | ');
        return '<div class="leader-status-warning" data-player="' + playerType + '" title="' + tooltipContent.replace(/"/g, '&quot;').replace(/'/g, '&#39;') + '">⚠️</div>';
    }
    
    var enemyStatusWarning = generateStatusWarning(boardInfo.enemy.leaderStatuses, 'enemy');
    var myStatusWarning = generateStatusWarning(boardInfo.me.leaderStatuses, 'me');
    
    // 绑定点击事件显示状态详情弹窗
    setTimeout(function() {
        $('.leader-status-warning').off('click').on('click', function() {
            var playerType = $(this).data('player');
            var statuses = playerType === 'enemy' ? boardInfo.enemy.leaderStatuses : boardInfo.me.leaderStatuses;
            showLeaderStatusModal(statuses, playerType === 'enemy' ? boardInfo.enemy.name : boardInfo.me.name);
        });
    }, 100);

    
    // 显示卡组计数与坟场，并用小红圈显示主战者当前生命值（不显示百分比/分子分母）
    $('#enemy-info').html('<span class="leader-health" title="'+boardInfo.enemy.hp+'/'+boardInfo.enemy.hpMax+'">'+boardInfo.enemy.hp+'</span>'
         + enemyStatusWarning + "<p title='超抽效果："+boardInfo.enemy.leader.overDrawMark+"'>🗃️"+ boardInfo.enemy.deckCount+"</p>💀"+ boardInfo.enemy.graveyardCount);
    $('#my-info').html("💀"+ boardInfo.me.graveyardCount + "<br/><p title='超抽效果："+boardInfo.me.leader.overDrawMark+"'>🗃️"+ boardInfo.me.deckCount+"</p>"
         + '<span class="leader-health" title="'+boardInfo.me.hp+'/'+boardInfo.me.hpMax+'">'+boardInfo.me.hp+'</span>' + myStatusWarning);

    $('#enemy-info-detail').html("<p class='skill' title='"+boardInfo.enemy.leader.skillMark+"'>"+ boardInfo.enemy.leader.skillName + "(" + boardInfo.enemy.leader.skillCost + ")</p>" +
    "<p title='"+boardInfo.enemy.leader.mark+"'>主战者："+ boardInfo.enemy.leader.name + "</p>" );
    if(boardInfo.enemy.leader.canUseSkill){
        $('#enemy-info-detail .skill').addClass("canUse");
    }else{
        $('#enemy-info-detail .skill').removeClass("canUse");
    }
    $('.enemy-pp-num').attr("title",dictShow(boardInfo.enemy.counter));

    $('#my-info-detail').html("<p title='"+boardInfo.me.leader.mark+"'>主战者："+ boardInfo.me.leader.name + "</p>" +
        "<p class='skill' title='"+boardInfo.me.leader.skillMark+"'>"+ boardInfo.me.leader.skillName + "(" + boardInfo.me.leader.skillCost + ")</p>");
    if(boardInfo.me.leader.canUseSkill){
        $('#my-info-detail .skill').addClass("canUse");
        $('#my-info-detail .skill').unbind().click(function(){setTimeout("websocket.send('skill')",500);})
    }else{
        $('#my-info-detail .skill').removeClass("canUse");
    }
    $('.my-pp-num').attr("title",dictShow(boardInfo.me.counter));

    // 使用宝石（Gem）显示 PP：显示 ppMax 个槽，未使用为 💎，已使用为 🪨（Rock）
    // isEnemy: true表示敌方（从下往上），false表示我方（从上往下）
    function renderGems(ppNum, ppMax, isEnemy){
        var perRow = 5;
        var rows = Math.ceil(ppMax / perRow);
        var html = '';
        var rowsArray = [];
        
        for(var r=0;r<rows;r++){
            var start = r*perRow;
            var end = Math.min(ppMax, start+perRow);
            var rowHtml = '';
            for(var i=start;i<end;i++){
                if(i < ppNum) rowHtml += '<span class="gem">💎</span>';
                else rowHtml += '<span class="gem used">🪨</span>';
            }
            rowsArray.push('<div class="pp-gems-row">'+rowHtml+'</div>');
        }
        
        // 敌方从下往上排列，需要反转行顺序
        if(isEnemy) {
            rowsArray.reverse();
        }
        
        html = rowsArray.join('');
        var directionClass = isEnemy ? 'pp-gems-enemy' : 'pp-gems-player';
        return '<div class="pp-gems '+directionClass+'">'+html+'</div>';
    }

    $('.enemy-pp-num').html(renderGems(boardInfo.enemy.ppNum, boardInfo.enemy.ppMax, true));
    $('.my-pp-num').html(renderGems(boardInfo.me.ppNum, boardInfo.me.ppMax, false));

    boardInfo.enemy.area.forEach(card => {
        $('#enemy-battlefield').append(cardHtml(card));
    });
    boardInfo.enemy.hand.forEach(card => {
        $('#enemy-hand').append(`
            <div class="card-back col-sm-6 col-md-4 col-lg-2"></div>
        `);
    });

    boardInfo.me.area.forEach(card => {
        $('#my-battlefield').append(cardHtml(card));
    });
    // --- 拖放攻击/使用支持（改进：drop 时会根据拖动来源发起攻击/使用） ---
    $('.card').attr('draggable', true);
    $('.card').off('dragstart dragend');
    $('.card').on('dragstart', function(e){
        var $card = $(this);
        var isMine = $card.closest('#my-battlefield, #my-hand').length>0;
        // 限制：若来自己方战场，只允许可攻击/可突进的随从拖动以发起攻击
        if($card.closest('#my-battlefield').length>0){
            if(!$card.hasClass('canAttack') && !$card.hasClass('canDash')){
                try{ e.originalEvent.dataTransfer.effectAllowed='none'; }catch(err){}
                return;
            }
        }
        // 传递来源与 index
        var from = $card.closest('#my-battlefield').length? 'my-battlefield' : ($card.closest('#my-hand').length? 'my-hand' : 'other');
        var payload = JSON.stringify({from: from, index: $card.index()});
        try{ e.originalEvent.dataTransfer.setData('text/plain', payload); }catch(err){}
        $card.addClass('dragging');
        if(isMine){
            $('#enemy-battlefield .card').addClass('possible-target');
            $('#enemy-info').addClass('possible-target');
        }else{
            $('#my-battlefield .card').addClass('possible-target');
            $('#my-info').addClass('possible-target');
        }
    });
    $('.card').on('dragend', function(e){
        $('.card').removeClass('dragging');
        $('.possible-target').removeClass('possible-target');
    });

    // 目标接受 drop（如果 drop 到具体卡牌则发起攻击/使用，否则若战场为空则攻击主战者）
    $('#enemy-battlefield, #enemy-info, #my-battlefield, #my-info').off('dragover drop');
    $('#enemy-battlefield, #enemy-info, #my-battlefield, #my-info').on('dragover', function(e){ e.preventDefault(); });
    $('#enemy-battlefield, #enemy-info, #my-battlefield, #my-info').on('drop', function(e){
        e.preventDefault();
        var data = null;
        try{ data = JSON.parse(e.originalEvent.dataTransfer.getData('text/plain')); }catch(err){ }
        var $targetCard = $(e.target).closest('.card');
        // 如果来源是我方战场，且目标是对方随从/主战者，则直接发起 attack
        if(data && data.from === 'my-battlefield'){
            var attackerIdx = data.index + 1; // 与 click 逻辑一致
            var $attackerCard = $('#my-battlefield .card').eq(data.index);
            if($targetCard.length && $targetCard.closest('#enemy-battlefield').length){
                var targetIdx = $targetCard.index() + 1;
                // 添加撞击动画，等待动画结束再发送指令
                $attackerCard.addClass('card-attack-animate');
                setTimeout(function(){
                    $attackerCard.removeClass('card-attack-animate');
                    websocket.send('attack::'+attackerIdx+' '+targetIdx);
                }, 500);
            }else if($(this).is('#enemy-info')){
                $attackerCard.addClass('card-attack-animate');
                setTimeout(function(){
                    $attackerCard.removeClass('card-attack-animate');
                    websocket.send('attack::'+attackerIdx+' 0');
                }, 500);
            }
        }
        // 如果来源是手牌，尝试直接发起带目标的 play 指令（优先使用目标卡的真实 id）
        else if(data && data.from === 'my-hand'){
            var handIdx = data.index + 1;
            if($targetCard.length && $targetCard.closest('#enemy-battlefield').length){
                // 取得目标卡的类名中 id-<id>
                var classes = ($targetCard.attr('class')||"").split(/\s+/);
                var idClass = classes.find(c=>c&&c.indexOf('id-')===0);
                var targetId = idClass? idClass.substring(3) : ($targetCard.index()+1);
                setTimeout(function(){ websocket.send('play::'+handIdx+' '+targetId); }, 100);
            }else if($(this).is('#enemy-info')){
                setTimeout(function(){ websocket.send('play::'+handIdx+' 0'); }, 100);
            }
        }
        // 清理拖拽样式
        $('.card').removeClass('dragging');
        $('.possible-target').removeClass('possible-target');
    });
    $('#my-battlefield .card').unbind().click(function(){
        if( $(this).hasClass("canAttack") || $(this).hasClass("canDash")){
            // 可以发起攻击，记录发起方
            let select = $(this).index()+1;
            targetMsg = select;
            
            $(".end-button").html("发起<br/>攻击");
            $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");

            $('#my-battlefield .card').unbind();
            $(this).addClass("selected");
            $(this).unbind().click(()=>{
                initBoard();// 还原棋盘
            });
            
            // 攻击敌方主战者
            if($(this).hasClass("canAttack")){
                $('#enemy-info').addClass("selected");
                $('#enemy-info').unbind().click(()=>{
                    var $attackerCard = $('#my-battlefield .card').eq(targetMsg-1);
                    $attackerCard.addClass('card-attack-animate');
                    setTimeout(function(){
                        $attackerCard.removeClass('card-attack-animate');
                        initBoard();// 先还原棋盘
                        websocket.send('attack::'+targetMsg+' 0');
                    }, 500);
                })
            }

            // 攻击敌方随从
            $('#enemy-battlefield .card').each((i,card)=>{
                if($(card).hasClass("FOLLOW")){
                    $(card).addClass("selected");
                    $(card).unbind().click(()=>{
                        var $attackerCard = $('#my-battlefield .card').eq(targetMsg-1);
                        $attackerCard.addClass('card-attack-animate');
                        setTimeout(function(){
                            $attackerCard.removeClass('card-attack-animate');
                            initBoard();// 先还原棋盘
                            websocket.send('attack::'+targetMsg+' '+(i+1));
                        }, 500);
                    });
                }

            })
            
        }
    })
    // 渲染手牌：根据当前 PP 判断是否可用（绿色），否则灰色
    boardInfo.me.hand.forEach(card => {
        var $c = $(cardHtml(card));
        try{
            var curPP = (boardInfo.me && boardInfo.me.ppNum) || 0;
            if(typeof card.cost === 'number' && curPP >= card.cost){
                $c.addClass('can-play');
            }else{
                $c.addClass('not-enough-pp');
            }
        }catch(e){
            // 容错：若读取发生异常，不强制样式
        }
        $('#my-hand').append($c);
    });
    $('#my-hand .card').unbind().click(function(){
        let select = $(this).index()+1;
        var $card = $(this);
        // 添加动画类
        $card.addClass('card-use-animate');
        // 1秒后发送指令并还原动画
        setTimeout(function(){
            $card.removeClass('card-use-animate');
            drawBoard();
            setTimeout(function(){
                websocket.send('play::'+select);
            }, 100);
        }, 1000);
    })

    // 调整描述字体以避免溢出
    adjustDescriptionFont();
    
    // 设置装备图标hover效果
    $('.equipment-indicator').off('mouseenter mouseleave');
    $('.equipment-indicator').on('mouseenter', function(e) {
        var $card = $(this).closest('.card');
        var equipmentData = $card.attr('data-equipment');
        if (equipmentData) {
            try {
                var equipment = JSON.parse(equipmentData);
                showCardHover(equipment, e.pageX, e.pageY);
            } catch(err) {
                console.error('Parse equipment data error:', err);
            }
        }
    }).on('mouseleave', function() {
        hideCardHover();
    });
    
    // 设置主战者影响列表hover效果
    $('.leader-affecting-indicator').off('mouseenter mouseleave');
    $('.leader-affecting-indicator').on('mouseenter', function(e) {
        var $container = $(this).closest('#my-info, #enemy-info');
        var isEnemy = $container.attr('id') === 'enemy-info';
        var affectingCards = isEnemy ? boardInfo.enemy.leader.affectingCards : boardInfo.me.leader.affectingCards;
        
        if (affectingCards && affectingCards.length > 0) {
            showCardsHover(affectingCards, e.pageX, e.pageY);
        }
    }).on('mouseleave', function() {
        hideCardHover();
    });
}

// 显示单张卡牌浮动效果
function showCardHover(card, x, y) {
    hideCardHover(); // 先清除之前的
    var $hoverDiv = $('<div class="card-hover-display"></div>');
    $hoverDiv.html(cardHtml(card));
    $hoverDiv.css({
        left: x + 20 + 'px',
        top: y - 100 + 'px'
    });
    $('body').append($hoverDiv);
    setTimeout(() => $hoverDiv.addClass('show'), 10);
}

// 显示多张卡牌浮动效果（影响列表）
function showCardsHover(cards, x, y) {
    hideCardHover();
    var $hoverDiv = $('<div class="card-hover-display"></div>');
    var html = '<div style="display: flex; flex-wrap: wrap; gap: 10px; background: rgba(0,0,0,0.8); padding: 15px; border-radius: 12px;">';
    cards.forEach(card => {
        html += cardHtml(card);
    });
    html += '</div>';
    $hoverDiv.html(html);
    $hoverDiv.css({
        left: Math.min(x + 20, window.innerWidth - 400) + 'px',
        top: Math.max(y - 150, 10) + 'px'
    });
    $('body').append($hoverDiv);
    setTimeout(() => $hoverDiv.addClass('show'), 10);
}

// 隐藏卡牌浮动效果
function hideCardHover() {
    $('.card-hover-display').remove();
}



var interval;		//定时器变量

// 消息队列
let alertQueue = [];
let isShowingAlert = false;

function  mnyAlert(type,msg,time=2000){
    // 将消息加入队列
    alertQueue.push({type, msg, time});
    
    // 如果当前没有显示消息，开始处理队列
    if (!isShowingAlert) {
        processAlertQueue();
    }
}

function processAlertQueue() {
    if (alertQueue.length === 0) {
        isShowingAlert = false;
        return;
    }
    
    isShowingAlert = true;
    const {type, msg, time} = alertQueue.shift();
    
    //判断页面中是否有#mny-width的dom元素，有的话将其去除
    if($('#mny-width').length > 0){
        $('#mny-width').remove();
    }
    
    // 先将其插入到body下
    if(type == '1'){
        $('header').append(`
        <div id="mny-width" class="alert alert-success mny-alert-position" role="alert">
            `+msg+`
        </div>
        `);
    }else if(type == '2'){
        $('header').append(`
        <div id="mny-width" class="alert alert-danger mny-alert-position" role="alert">
            `+msg+`
        </div>
        `);
    }

    //计算长度
    const mny_width = $('#mny-width').innerWidth() + 2;
    //向元素中添加内嵌样式
    $('#mny-width').css('marginLeft','-'+mny_width/2+'px');
    
    //清除已存在的定时器
    clearInterval(interval)
    //将元素定时去除并处理下一条
    interval = window.setInterval(function () {
        $('#mny-width').remove();
        clearInterval(interval);
        // 处理队列中的下一条消息
        processAlertQueue();
    }, time);
}

function swap(){
    let swapArr = [];
    $("#swap-card .card").each((i,card)=>{
        if($(card).hasClass("selected")){
            $(card).hide();
            swapArr.push(i+1);
        }
    })
    $("#swap-confirm").hide();
    websocket.send('swap::'+swapArr.join(' '));
}

function endTurn(){
    $(".end-button").html("对方<br/>回合");
    $(".end-button").css("background","radial-gradient(red, #2f4f4f9f)");
    
    setTimeout("websocket.send('end')",500);
}

function showMsg(){
    $('#msg-log-div').toggle();
}


var myDeck;
function editDeck(){
    let newDeck = prompt("输入牌组构成（推荐编辑好后粘贴过来）：",myDeck);
    setTimeout("websocket.send('setdeck::"+newDeck+"')",500);
}

// 从登录session获取真实用户名
async function fetchUserName() {
    try {
        const response = await fetch('/api/auth/session', {
            method: 'GET',
            credentials: 'include'
        });
        if (response.ok) {
            const data = await response.json();
            if (data && data.username) {
                return data.username;
            }
        }
    } catch (error) {
        console.error('Failed to fetch username:', error);
    }
    // 如果获取失败，使用随机用户名作为后备方案
    return "Player" + Math.floor(Math.random() * 1000000);
}

// 异步获取用户名并初始化WebSocket
let userName = null;
(async function initUserName() {
    userName = await fetchUserName();
    console.log("当前用户名:", userName);
    
    // 在获取用户名后初始化WebSocket
    if ($.trim(userName)) {
        initWebSocket(userName);
    }
})();

var boardInfo;// 用于重绘棋盘
var targetMsg;// 需要指定时，把指令存起来
var targetLists;// 可指定的卡牌
var boardPath = (window.location.pathname || "").toLowerCase();
var boardPages = ["", "/", "/index.html", "index.html"];
var hasBoardUi = boardPages.includes(boardPath);
var pendingAutoMatchMode = hasBoardUi ? localStorage.getItem('pendingMatchMode') : null;
var aiWaitingInterval = null;  // 弥留之国AI搜寻等待定时器
function showBorderlandAutoOverlay(){
    if(!hasBoardUi || !pendingAutoMatchMode) return;
    var $panel = $('#battle-panel');
    if(!$panel.length) return;
    var tabBtn = document.getElementById('battle-tab');
    if(tabBtn && window.bootstrap){
        var tabInstance = bootstrap.Tab.getOrCreateInstance(tabBtn);
        tabInstance.show();
    }
    var $entryRow = $panel.find('.row.g-4').first();
    $entryRow.addClass('d-none');
    if($('#borderland-auto-overlay').length === 0){
        var overlayHtml = `
            <div id="borderland-auto-overlay" class="text-center py-5">
                <div class="spinner-border text-danger mb-3" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <h3 class="text-danger mb-2">弥留之国匹配中</h3>
                <p class="text-muted mb-0">正在为你寻找AI对手，请稍候...</p>
            </div>`;
        $panel.find('.text-center').first().append(overlayHtml);
    }
    var panelNode = document.getElementById('battle-panel');
    if(panelNode){
        panelNode.scrollIntoView({behavior:'smooth'});
    }
}

function hideBorderlandAutoOverlay(){
    $('#borderland-auto-overlay').remove();
    var $panel = $('#battle-panel');
    if($panel.length){
        $panel.find('.row.g-4').first().removeClass('d-none');
    }
}
function triggerBorderlandAutoMatch(){
    if(!hasBoardUi || !pendingAutoMatchMode) return;
    showBorderlandAutoOverlay();
    if(typeof websocket === 'undefined'){
        setTimeout(triggerBorderlandAutoMatch,300);
        return;
    }
    if(websocket.readyState !== WebSocket.OPEN){
        setTimeout(triggerBorderlandAutoMatch,300);
        return;
    }
    const modeToJoin = pendingAutoMatchMode;
    mnyAlert(1,"正在为你匹配弥留之国对战...");
    setTimeout(function(){
        websocket.send('joinRoom::'+modeToJoin);
    },500);
    localStorage.removeItem('pendingMatchMode');
    pendingAutoMatchMode = null;
}

if(hasBoardUi && pendingAutoMatchMode){
    showBorderlandAutoOverlay();
    triggerBorderlandAutoMatch();
}

function initWebSocket(userName) {
if ($.trim(userName)) {
    if(window.location.host.indexOf("8.216.80.59") < 0)
        // 本地运行
        websocket = new WebSocket("ws://localhost/api/"+userName);
    else
        websocket = new WebSocket("ws://8.216.80.59/api/"+userName);

    $("username").html(userName);
    console.log("征集有趣的自定义卡牌、主战者、玩法、卡面。联系方式：（Bilibili）漆黑Ganker");
    console.log("如果你是软件开发人员，欢迎你贡献代码！项目地址：https://github.com/476106017/ccg4j");

    websocket.onerror = function () {
        console.log("连接错误");
    }
    websocket.onopen = function () {
        // alert("连接成功！");
        websocket.send("deck");
    };
    //      收到消息的回调方法
    websocket.onmessage = function (msg) {
        let data = JSON.parse(msg.data);
        let obj = data.data;
        console.log(data);
        console.log(obj);

        switch(data.channel){
            case "msg":
                hideBorderlandWaitingOverlay();
                mnyAlert(1,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "warn":
                hideBorderlandWaitingOverlay();
                mnyAlert(2,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "alert":
                hideBorderlandWaitingOverlay();
                alert(obj);
                break;
            case "redirect":
                // 重定向到指定页面
                setTimeout(() => {
                    window.location.href = obj;
                }, 2000);
                break;
            case "borderland-ai-waiting":
                // AI搜寻等待中（10秒暴露期）
                showBorderlandAIWaiting(parseInt(obj));
                break;
            case "borderland-ai-cancelled":
                // AI搜寻已取消
                hideBorderlandWaitingOverlay();
                break;
            case "borderland-invaded":
                // 被入侵警告
                hideBorderlandWaitingOverlay();
                showInvasionWarning(obj);
                break;
            case "borderland-battle-log":
                // 弥留之国战斗记录（广播给所有玩家）
                if (window.location.pathname.includes('borderland')) {
                    // 如果在弥留之国页面，重新加载战斗记录
                    if (typeof loadBattleLog === 'function') {
                        setTimeout(() => loadBattleLog(), 500);
                    }
                }
                break;
            case "search_results":
                    $('#search-results').empty();
                    // 更新搜索统计
                    $('.search-stats').text(`共找到 ${obj.length} 张卡牌`);

                    // 直接渲染所有卡牌
                    obj.forEach(card => {
                        const $card = $(cardHtml(card));
                        $card.click(function() {
                            let cardCount = myDeck ? myDeck.split('#').filter(name => name === card.name).length : 0;
                            let totalCards = myDeck ? myDeck.split('#').filter(name => name.length > 0).length : 0;
                            
                            if (cardCount >= 3) {
                                mnyAlert(2, "每张卡在卡组中最多放3张！");
                                return;
                            }
                            if (totalCards >= 40) {
                                mnyAlert(2, "卡组最多40张卡牌！");
                                return;
                            }
                            
                            myDeck = myDeck || "";
                            myDeck += card.name + "#";
                            websocket.send('setdeck::' + myDeck);
                            mnyAlert(1, "已添加 " + card.name + " 到卡组");
                            
                            $card.addClass('adding');
                            setTimeout(() => $card.removeClass('adding'), 300);
                        });
                        $('#search-results').append($card);
                    });

                    // 调整描述字体
                    adjustDescriptionFont('#search-results');
                    break;

                case "myDeck":
                    // 清空卡组视图
                    $('#card-gridview').html("");
                    myDeck = "";
                    
                    // 检查并添加清空按钮（如果不存在）
                    if ($('#deck-actions').length === 0) {
                        $('#card-gridview').before(`
                            <div id="deck-actions" style="margin-bottom:20px;">
                                <button id="clear-deck-all" class="btn btn-sm btn-danger">清空全部卡牌</button>
                            </div>
                        `);
                    }
                    
                    // 重新绑定清空按钮事件
                    $('#clear-deck-all').off('click').on('click', function(){
                        if(confirm('确定要清空当前卡组吗？此操作不可恢复')){
                            myDeck = '';
                            websocket.send('setdeck::');
                            $('#card-gridview').html('');
                            mnyAlert(1, '已清空卡组');
                        }
                    });
                    obj.deck.forEach((card, index) => {
                        const $card = $(cardHtml(card));
                        
                        // 添加删除按钮
                        const $deleteBtn = $('<button type="button" class="btn btn-danger btn-sm delete-card">×</button>');
                        
                        // 删除按钮点击事件
                        $deleteBtn.click(function(e) {
                            e.stopPropagation(); // 阻止事件冒泡
                            
                            // 使用Bootstrap的模态框进行二次确认
                            if (confirm('确定要从卡组中移除 ' + card.name + ' 吗？')) {
                                // 从卡组中移除这张卡
                                let cards = myDeck.split('#').filter(name => name.length > 0);
                                // 找到第一个匹配的卡牌并移除
                                let removed = false;
                                cards = cards.filter(name => {
                                    if (!removed && name === card.name) {
                                        removed = true;
                                        return false;
                                    }
                                    return true;
                                });
                                
                                // 更新卡组
                                myDeck = cards.join('#') + '#';
                                websocket.send('setdeck::' + myDeck);
                                
                                // 显示删除成功提示
                                mnyAlert(1, "已从卡组中移除 " + card.name);
                            }
                        });
                        
                        $card.append($deleteBtn);
                        $('#card-gridview').append($card);
                        myDeck += card.name;
                        myDeck += "#";
                    });
                    
                    // 调整描述字体
                    adjustDescriptionFont('#card-gridview');
                // 调整卡组视图中的描述字体
                adjustDescriptionFont('#card-gridview');
                // websocket.send('joinRoom');// test
                break;
            case "presetDeck":
                $('#deck-preset').html("");
                obj.forEach(deck => {
                    $('#deck-preset').append('<button type="button" class="btn btn-outline-dark" data-dismiss="modal" onclick="websocket.send(\'usedeck::'+deck.name+'\');">'+deck.name+'</button>');
                });
                $('#deck-preset-modal').modal('show');
                break;
            case "waitRoom":
                $('#roomCode').html(obj);
                $('#wait-room-modal').modal('show');
                break;
            case "swap":
                hideBorderlandAutoOverlay();
                $('#wait-room-modal').modal('hide');
                $('#swap-card-modal').modal('show');
                $("#swap-confirm").show();
                $('#swap-card').html("");
                obj.forEach(card => {
                    $('#swap-card').append(cardHtml(card));
                });
                $("#swap-card .card").each((k,card)=>{
                    $(card).click(()=>{
                        if($(card).hasClass("selected"))
                            $(card).removeClass("selected");
                        else
                            $(card).addClass("selected");
                    });
                })
                // 调整换牌弹窗中描述字体
                adjustDescriptionFont('#swap-card');
                // swap();// test
                break;
            case "swapOver":
                $('#swap-card-modal').modal('hide');
                $('#swap-card').html("");
                $('#senjou-modal').modal('show');
                break;
            case "enemyTurn":
                $(".end-button").html("对方<br/>回合");
                $(".end-button").css("background","radial-gradient(red, #2f4f4f9f)");
                break;
            case "yourTurn":
                $(".end-button").html("结束<br/>回合");
                $(".end-button").css("background","radial-gradient(blue, #2f4f4f9f)");
                break;
            case "battleInfo":
                // 比较上一帧与当前帧，播放受击动画
                var prevBoard = boardInfo;
                boardInfo = obj;
                drawBoard();
                try{
                    if(prevBoard){
                        // 随从受伤检测（我方随从）
                        (boardInfo.me.area||[]).forEach(card => {
                            var prev = (prevBoard.me && prevBoard.me.area)||[];
                            var pc = prev.find(c=>c.id === card.id);
                            if(pc && (pc.hp > card.hp)){
                                var $el = $('.id-'+card.id).first();
                                $el.addClass('card-hit');
                                setTimeout(()=> $el.removeClass('card-hit'), 700);
                            }
                        });
                        // 随从受伤检测（敌方随从）
                        (boardInfo.enemy.area||[]).forEach(card => {
                            var prev = (prevBoard.enemy && prevBoard.enemy.area)||[];
                            var pc = prev.find(c=>c.id === card.id);
                            if(pc && (pc.hp > card.hp)){
                                var $el = $('.id-'+card.id).first();
                                $el.addClass('card-hit');
                                setTimeout(()=> $el.removeClass('card-hit'), 700);
                            }
                        });
                        // 主战者受伤检测
                        if(prevBoard.me && boardInfo.me && prevBoard.me.hp > boardInfo.me.hp){
                            var $el = $('#my-info .leader-health');
                            $el.addClass('card-hit');
                            setTimeout(()=> $el.removeClass('card-hit'), 700);
                        }
                        if(prevBoard.enemy && boardInfo.enemy && prevBoard.enemy.hp > boardInfo.enemy.hp){
                            var $el = $('#enemy-info .leader-health');
                            $el.addClass('card-hit');
                            setTimeout(()=> $el.removeClass('card-hit'), 700);
                        }
                    }
                }catch(e){console.log(e)}
                break;
            case "clearBoard":
                clearBoard();
                break;
            case "discover":
                $('#discover-card-modal').modal('show');
                $('#discover-card').html("");
                obj.forEach(card => {
                    $('#discover-card').append(cardHtml(card));
                });
                $("#discover-card .card").each((k,card)=>{
                    $(card).unbind().click(()=>{
                var idx = k+1;
                $('#discover-card-modal').modal('hide');
                setTimeout(function(){ websocket.send('discover::'+idx); }, 500);
                $('#discover-card').html("");
                    });
                })
                // 调整发现弹窗中描述字体
                adjustDescriptionFont('#discover-card');
                break;
            case "skill":
                $(".end-button").html("技能<br/>目标");
                $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");
                $('#my-hand .card').unbind();
                targetLists = obj;// 加载待选择项
                $('#my-battlefield .card').unbind();// 禁止攻击事件
                
                $('#my-info-detail .skill').addClass("selected");
                $('#my-info-detail .skill').unbind().click(()=>{
                    initBoard();// 还原棋盘
                });

                targetLists.forEach(obj=>{
                    $(".id-"+obj.id).addClass("selected");
                    $(".id-"+obj.id).unbind().click(()=>{
                        // 选择结束
                        initBoard();// 先还原棋盘
                        setTimeout("websocket.send('skill::"+obj.id+"')",500);

                    });
                })
                
                // 点击空白处取消
                setTimeout(() => {
                    $(document).on("click.cancelSkill", function(e){
                        // 如果点击的不是目标，也不是技能按钮
                        if(!$(e.target).closest(".selected").length && !$(e.target).closest(".skill").length){
                            initBoard();
                        }
                    });
                }, 100);
                break;
            case "target":
                $(".end-button").html("效果<br/>目标");
                $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");
                $('#my-hand .card').unbind();
                targetMsg = obj.pref+' ';
                targetLists = obj.targetLists;// 加载待选择项
                $('#my-battlefield .card').unbind();// 禁止攻击事件

                targetLists[0].forEach(obj=>{
                    $(".id-"+obj.id).addClass("selected");
                    $(".id-"+obj.id).unbind().click(()=>{
                        targetMsg+=obj.id;

                        if(targetLists[1]){
                            // 选择第二个目标
                            targetLists[0].forEach(obj=>{
                                $(".id-"+obj.id).removeClass("selected");
                                $(".id-"+obj.id).unbind();
                            });
                            targetLists[1].forEach(obj=>{
                                $(".id-"+obj.id).addClass("selected");
                                $(".id-"+obj.id).unbind().click(()=>{
                                    targetMsg+=" "+obj.id;
                                    initBoard();// 先还原棋盘
                                    setTimeout("websocket.send('play::"+targetMsg+"')",500);
                                });
                            });
                            $(".end-button").html("第二<br/>目标");
                            $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");
                        }else{
                            // 选择结束
                            initBoard();// 先还原棋盘
                            setTimeout("websocket.send('play::"+targetMsg+"')",500);
                        }

                    });
                })
                break;
        }
    };
    //      连接关闭的回调方法
    websocket.onclose = function () {
        // alert("已断开和服务器的连接，请刷新页面！");
    };
}
}
// 根据描述容器的可见高度，逐步缩小文字直到不溢出（带最小字体限制）
function adjustDescriptionFont(containerSelector){
    // containerSelector 可选，默认处理页面上所有 description
    var $ps = containerSelector? $(containerSelector).find('.description p') : $('.description p');
    $ps.each((i, p) => {
        var $p = $(p);
        var $desc = $p.closest('.description');
        var maxFont = 16; // 起始字体大小（px）
        var minFont = 10; // 最小字体大小（px）
        // 先设置为最大起始大小
        $p.css('font-size', maxFont + 'px');
        // 当内容高度超过容器可见高度时，逐步减小字体
        try{
            while($p[0].scrollHeight > $desc[0].clientHeight && parseFloat($p.css('font-size')) > minFont){
                var cur = parseFloat($p.css('font-size')) - 1;
                $p.css('font-size', cur + 'px');
            }
        }catch(e){
            // 容错：如果元素被移除或不可测量，跳过
        }
    });
}

// 预先加载关键词数据
let keywordsData = null;

function loadKeywords() {
    $.ajax({
        url: 'keyword.json',
        dataType: 'json',
        cache: false,  // 禁用缓存
        success: function(data) {
            keywordsData = data.keywords;
            console.log('关键词数据加载成功:', keywordsData.length, '个词条');
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error('加载关键词数据失败:', textStatus, errorThrown);
        }
    });
}

// 页面加载完成后加载关键词数据
$(document).ready(loadKeywords);

// 卡牌 hover 提示：显示关键字与触发器的说明
$(document).on('mouseenter', '.card', function(e) {
    var $card = $(this);
    
    // 清除任何可能存在的旧提示
    $('.card-tooltip-left, .card-tooltip-right').remove();
    
    // 获取卡牌的关键词和描述
    var kw = $card.data('keywords') || [];
    var mark = $card.attr('data-mark') || '';
    
    // 构建关键词提示
    var leftHtml = '';
    if (kw && kw.length > 0 && keywordsData) {
        leftHtml = '<div class="card-tooltip-title">词条</div>';
        kw.forEach(k => {
            var keywordInfo = keywordsData.find(x => x.key === k || x.key === (k+""));
            var desc = keywordInfo ? keywordInfo.desc : '（无描述）';
            leftHtml += '<div class="keyword-inline"><b>' + k + ':</b> ' + desc + '</div>';
        });
    }

    // 构建触发器提示
    var triggers = [];
    if (/战吼/.test(mark)) {
        triggers.push({key: '战吼', desc: '在此随从进入战场/使用时触发的效果'});
    }
    if (/回合结束时/.test(mark)) {
        triggers.push({key: '回合结束时', desc: '在回合结束阶段触发的效果'});
    }
    if (/出牌时/.test(mark) && !/战吼/.test(mark)) {
        triggers.push({key: '出牌时', desc: '打出该卡时触发的效果'});
    }

    // 额外触发器说明：受伤、亡语、增幅、瞬念召唤、揭示、腐蚀、离场时、超杀等
    if (/受伤时|受到伤害时|受伤/.test(mark)) {
        triggers.push({key: '受伤时', desc: '该单位受到伤害时触发的效果（通常在伤害结算后）'});
    }
    if (/亡语|亡語/.test(mark)) {
        triggers.push({key: '亡语', desc: '随从离场或被破坏后触发的效果'});
    }
    if (/增幅|魔力增幅|增幅效果/.test(mark)) {
        triggers.push({key: '增幅', desc: '打出其他卡片时触发效果'});
    }
    if (/瞬念召唤|瞬念/.test(mark)) {
        triggers.push({key: '瞬念召唤', desc: '当此卡符合指定条件时，从牌堆召唤并触发效果'});
    }
    if (/揭示/.test(mark)) {
        triggers.push({key: '揭示', desc: '当此卡符合指定条件时，从牌堆抽到手中并触发效果'});
    }
    if (/腐蚀/.test(mark)) {
        triggers.push({key: '腐蚀', desc: '当使用费用高于此卡片的其他卡片时触发的效果'});
    }
    if (/离场时|不在场时/.test(mark)) {
        triggers.push({key: '离场时', desc: '当该随从离开战场（返回手牌、被除外、被破坏等）时触发的效果'});
    }
    if (/超杀|超额杀死|超杀时/.test(mark)) {
        triggers.push({key: '超杀', desc: '当对手单位被造成的伤害超过其剩余生命时触发的额外效果'});
    }

    var rightHtml = '';
    if (triggers.length > 0) {
        rightHtml = '<div class="card-tooltip-title">触发器</div>';
        triggers.forEach(t => {
            rightHtml += '<div class="keyword-inline"><b>' + t.key + ':</b> ' + t.desc + '</div>';
        });
    }

    // 添加提示并定位。优先将 tooltip 放入最近的滚动容器（.scroll-container），如果找不到则回退到 body
    if (leftHtml || rightHtml) {
        // 卡牌相关尺寸/位置
        var cardPosition = $card.position();
        var cardOffset = $card.offset();
        var cardWidth = $card.outerWidth();
        var cardHeight = $card.outerHeight();
        var $container = $card.closest('.scroll-container');

        // 决定要追加到哪个容器：优先滚动容器，否则 body
        var $target = ($container && $container.length) ? $container : $('body');

        if (leftHtml) {
            var $leftTooltip = $('<div class="card-tooltip-left">' + leftHtml + '</div>').appendTo($target);

            // 根据追加目标选择定位方式
            if ($target.is('body')) {
                // 如果在 body 上，使用 fixed 相对于视口定位（考虑滚动）
                var viewportTop = cardOffset.top - $(window).scrollTop();
                var viewportLeft = cardOffset.left - $(window).scrollLeft();
                $leftTooltip.css({
                    position: 'fixed',
                    top: viewportTop + (cardHeight/2) - ($leftTooltip.outerHeight()/2),
                    left: viewportLeft - $leftTooltip.outerWidth() - 10
                }).fadeIn(120);
            } else {
                // 在滚动容器内，使用相对容器的 position() 值
                $leftTooltip.css({
                    position: 'absolute',
                    top: cardPosition.top + (cardHeight/2) - ($leftTooltip.outerHeight()/2),
                    left: cardPosition.left - $leftTooltip.outerWidth() - 10
                }).fadeIn(120);
            }
        }

        if (rightHtml) {
            var $rightTooltip = $('<div class="card-tooltip-right">' + rightHtml + '</div>').appendTo($target);

            if ($target.is('body')) {
                var viewportTopR = cardOffset.top - $(window).scrollTop();
                var viewportLeftR = cardOffset.left - $(window).scrollLeft();
                $rightTooltip.css({
                    position: 'fixed',
                    top: viewportTopR + (cardHeight/2) - ($rightTooltip.outerHeight()/2),
                    left: viewportLeftR + cardWidth + 10
                }).fadeIn(120);
            } else {
                $rightTooltip.css({
                    position: 'absolute',
                    top: cardPosition.top + (cardHeight/2) - ($rightTooltip.outerHeight()/2),
                    left: cardPosition.left + cardWidth + 10
                }).fadeIn(120);
            }
        }
    }
});

$(document).on('mouseleave', '.card', function(e){
    var $card = $(this);
    // 移除所有tooltip
    $('.card-tooltip-left, .card-tooltip-right').fadeOut(120, function() {
        $(this).remove();
    });
});

// 卡牌搜索功能（用于对战中）
function searchCards() {
    const name = $('#card-search-name').val();
    const type = $('#card-search-type').val();
    const cost = $('#card-search-cost').val();
    
    // 发送搜索请求到服务器
    websocket.send(JSON.stringify({
        type: 'search_cards',
        data: {
            name: name,
            cardType: type,
            cost: cost
        }
    }));
}

// 弥留之国AI搜寻等待遮罩层（10秒暴露期）
function showBorderlandAIWaiting(seconds) {
    let remaining = seconds;
    const $overlay = $(`
        <div id="borderland-waiting-overlay" style="
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.9);
            z-index: 99999;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            color: white;
        ">
            <div style="text-align: center; max-width: 500px; padding: 20px;">
                <h2 style="color: #ffc107; margin-bottom: 20px;">🤖 搜寻AI中...</h2>
                <div style="background: rgba(255,255,255,0.1); border-radius: 10px; padding: 15px; margin-bottom: 20px;">
                    <div style="font-size: 48px; font-weight: bold; color: #ffc107;" id="countdown-seconds">${remaining}</div>
                    <div style="font-size: 14px; color: #ccc;">秒后开始AI对战</div>
                </div>
                <div style="width: 100%; background: rgba(255,255,255,0.2); height: 20px; border-radius: 10px; overflow: hidden;">
                    <div id="ai-wait-progress-bar" style="
                        width: 0%;
                        height: 100%;
                        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
                        transition: width 0.3s ease;
                    "></div>
                </div>
                <div style="margin-top: 20px;">
                    <button id="cancel-ai-search-btn" style="
                        padding: 12px 30px;
                        font-size: 16px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border: none;
                        border-radius: 25px;
                        cursor: pointer;
                        transition: all 0.3s;
                        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                    ">取消搜寻</button>
                </div>
                <div style="margin-top: 20px; padding: 15px; background: rgba(255,50,50,0.2); border: 2px solid #ff3232; border-radius: 10px;">
                    <p style="font-size: 16px; color: #ff6666; margin: 0;">
                        ⚠️ <strong>暴露期警告</strong> ⚠️<br/>
                        <span style="font-size: 14px;">正在搜寻玩家的猎杀者可以入侵你！</span>
                    </p>
                </div>
            </div>
        </div>
    `);
    
    $('body').append($overlay);
    
    // 取消按钮事件
    $('#cancel-ai-search-btn').on('click', function() {
        websocket.send('cancelAISearch');
        hideBorderlandWaitingOverlay();
    });
    
    aiWaitingInterval = setInterval(() => {
        remaining--;
        const progress = ((seconds - remaining) / seconds) * 100;
        $('#countdown-seconds').text(remaining);
        $('#ai-wait-progress-bar').css('width', progress + '%');
        
        if (remaining <= 0) {
            clearInterval(aiWaitingInterval);
        }
    }, 1000);
}

function hideBorderlandWaitingOverlay() {
    $('#borderland-waiting-overlay').remove();
    if (aiWaitingInterval) {
        clearInterval(aiWaitingInterval);
        aiWaitingInterval = null;
    }
}

function showInvasionWarning(message) {
    const $warning = $(`
        <div style="
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%);
            color: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(255,0,0,0.5);
            z-index: 100000;
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            animation: invasion-pulse 0.5s ease-in-out infinite alternate;
        ">
            ⚠️ ${message} ⚠️
        </div>
        <style>
            @keyframes invasion-pulse {
                from { transform: translate(-50%, -50%) scale(1); }
                to { transform: translate(-50%, -50%) scale(1.05); }
            }
        </style>
    `);
    
    $('body').append($warning);
    
    setTimeout(() => {
        $warning.fadeOut(500, function() {
            $(this).remove();
        });
    }, 3000);
}
