var distinctArr = function(arr){
    return Object.entries(
        arr.reduce((count, el) => ((count[el] = ++count[el] || 1), count), {})
      ).map(([el, count]) => `${el}${count > 1 ? count.toString() : ""}`);
}
var cardHtml = function(card){
    return `
        <div class="card col-sm-6 col-md-4 col-lg-2 id-${card.id} ${card.TYPE} ${card.canAttack?'canAttack':''} ${card.canDash?'canDash':''}">
            <img src="${card.name}.png" alt="" class="image" onerror="this.src='error.webp'">
            <div class="name ${card.upgrade?"upgrade":""}">${card.name + (card.upgrade?" +":"")}</div>
            <div class="type">${card.TYPE}</div>
            ${card.race.length>0?'<p class="race">'+card.race.join(' ')+'</p>':""}
            <div class="cost">${card.cost}</div>
            <div class="description" title="${card.mark}">
                <p>${card.keywords.length>0?'<b class="keyword">'+distinctArr(card.keywords).join(' ')+'</b>\n':""}${card.mark}<i>${card.subMarkStr}</i></p>
                <div class="job">${card.job}</div>
            </div>
            <div ${card.TYPE!="AMULET"?"hidden":""}>
                <div class="countDown">倒数：${card.countDown>0?card.countDown:"∞"}</div>
            </div>
            <div ${card.TYPE!="FOLLOW"?"hidden":""}>
                <div class="attack">${card.atk}</div>
                <div class="health-bar">
                    <div class="health-bar-inner" style="width: ${card.hp/card.maxHp*100}%;"></div>

                    <div class="health-bar-text">${card.hp}/${card.maxHp}</div>
                </div>
            </div>
        </div>
    `
}
var dictShow = function(obj){
    let show = "";
    for (let key in obj) {
        if(key.indexOf("_")<0 && obj[key]>0){
            show = show+key+":"+obj[key]+"\n";
        }
    }
    return show;
}
// 进入某个模式（选择/攻击）后用这个
var initBoard = function(){
    $('#enemy-info').removeClass("selected");
    $('#enemy-info').unbind();
    $('#my-info').removeClass("selected");
    $('#my-info').unbind();
    $(".end-button").html("结束<br/>回合");
    $(".end-button").css("background","radial-gradient(blue, #2f4f4f9f)");
    drawBoard();
}
var drawBoard = function(){
    $('#enemy-hero').empty();
    $('#enemy-hand').empty();
    $('#enemy-battlefield').empty();
    $('#my-battlefield').empty();
    $('#my-hand').empty();
    $('#my-hero').empty();


    $('#enemy-info').addClass('id-'+boardInfo.enemy.leader.id);
    $('#my-info').addClass('id-'+boardInfo.me.leader.id);
    $('#enemy-info').html("<p title='超抽效果："+boardInfo.enemy.leader.overDrawMark+"'>🗃️"+ boardInfo.enemy.deckCount+"</p>💀"+ boardInfo.enemy.graveyardCount +
        "<br/>" + "❤️"+ boardInfo.enemy.hp + "/" + boardInfo.enemy.hpMax);
    $('#my-info').html("❤️"+ boardInfo.me.hp + "/" + boardInfo.me.hpMax + "<br/>" + "💀"+ boardInfo.me.graveyardCount +
        "<br/><p title='超抽效果："+boardInfo.me.leader.overDrawMark+"'>🗃️"+ boardInfo.me.deckCount+"</p>");

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

    $('.enemy-pp-num').html(boardInfo.enemy.ppNum+" / "+boardInfo.enemy.ppMax);
    $('.my-pp-num').html(boardInfo.me.ppNum+" / "+boardInfo.me.ppMax);

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
                    initBoard();// 先还原棋盘
                    setTimeout("websocket.send('attack::"+targetMsg+" 0')",500);
                })
            }

            // 攻击敌方随从
            $('#enemy-battlefield .card').each((i,card)=>{
                if($(card).hasClass("FOLLOW")){
                    $(card).addClass("selected");
                    $(card).unbind().click(()=>{
                        initBoard();// 先还原棋盘
                        setTimeout("websocket.send('attack::"+targetMsg+" "+(i+1)+"')",500);
                    });
                }

            })
            
        }
    })
    boardInfo.me.hand.forEach(card => {
        $('#my-hand').append(cardHtml(card));
    });
    $('#my-hand .card').unbind().click(function(){
        let select = $(this).index()+1;
        
        drawBoard();// 先还原棋盘
        setTimeout("websocket.send('play::"+select+"')",500);
    })
}



var interval;		//定时器变量

function  mnyAlert(type,msg,time=2000){
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
    // console.log(time);
    //清除已存在的定时器
    clearInterval(interval)
    //将元素定时去除
    interval = window.setInterval(function () {
        $('#mny-width').remove();
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
function showKeywords(){
    $('#keywords').html("");
    keywords.forEach(keyword => {
        $('#keywords').append('<button type="button" class="btn btn-outline-dark" title='+keyword.desc+' data-dismiss="modal">'+keyword.key+'</button>');
    });
    $('#keywords-modal').modal('show');
}

var myDeck;
function editDeck(){
    let newDeck = prompt("输入牌组构成（推荐编辑好后粘贴过来）：",myDeck);
    setTimeout("websocket.send('setdeck::"+newDeck+"')",500);
}
// var userName = prompt("请问牌友如何称呼？");
userName = "Player"+Math.floor(Math.random()*1000000);

var boardInfo;// 用于重绘棋盘
var targetMsg;// 需要指定时，把指令存起来
var targetLists;// 可指定的卡牌

if ($.trim(userName)) {
    if(window.location.host.indexOf("card4j") <= 0)
        // 本地运行
        websocket = new WebSocket("ws://localhost:18081/api/"+userName);
    else
        websocket = new WebSocket("ws://www.card4j.top:18081/api/"+userName);

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
                mnyAlert(1,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "warn":
                mnyAlert(2,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "alert":
                alert(obj);
                break;
            case "myDeck":
                $('#card-gridview').html("");
                myDeck = "";
                obj.deck.forEach(card => {
                    $('#card-gridview').append(cardHtml(card));
                    myDeck += card.name;
                    myDeck += "#";
                });
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
                boardInfo = obj;
                drawBoard();
                break;
            case "discover":
                $('#discover-card-modal').modal('show');
                $('#discover-card').html("");
                obj.forEach(card => {
                    $('#discover-card').append(cardHtml(card));
                });
                $("#discover-card .card").each((k,card)=>{
                    $(card).unbind().click(()=>{
                        $('#discover-card-modal').modal('hide');
                        setTimeout("websocket.send('discover::"+(k+1)+"')",500);
                        $('#discover-card').html("");
                    });
                })
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