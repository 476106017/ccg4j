/**
 * 显示主战者状态与效果弹窗
 * @param {Array} leaderStatuses 状态列表
 * @param {String} playerName 玩家名称
 */
var showLeaderStatusModal = function(leaderStatuses, playerName) {
    var $modalBody = $('#leader-status-list');
    $modalBody.empty();
    
    if (!leaderStatuses || leaderStatuses.length === 0) {
        $modalBody.html('<p class="text-muted">当前没有状态或效果</p>');
    } else {
        var html = '<div class="leader-status-content">';
        
        // 显示状态
        var statuses = leaderStatuses.filter(s => s.type === 'status');
        if (statuses.length > 0) {
            html += '<div class="row mb-3">';
            statuses.forEach(function(status) {
                html += '<div class="col-6 mb-2">';
                html += '<div class="status-item p-2 border rounded">';
                html += '<div class="fw-bold">' + status.label + ' (' + status.value + ')</div>';
                html += '<small class="text-muted">' + status.description + '</small>';
                html += '</div></div>';
            });
            html += '</div>';
        }
        
        // 显示效果卡牌
        var effects = leaderStatuses.filter(s => s.type === 'effect' && s.card);
        if (effects.length > 0) {
            html += '<h6 class="mb-2">影响的卡牌效果</h6>';
            html += '<div class="row leader-status-cards">';
            effects.forEach(function(effect) {
                html += '<div class="col-4">';
                html += cardHtml(effect.card);
                html += '</div>';
            });
            html += '</div>';
        }
        
        html += '</div>';
        $modalBody.html(html);
    }
    
    $('#leaderStatusLabel').text(playerName + ' 的主战者状态与效果');
    $('#leader-status-modal').modal('show');
}
