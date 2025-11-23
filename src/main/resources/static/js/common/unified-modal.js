/**
 * Unified Modal System
 * Replaces browser native alert() and confirm() with custom modals
 */

// Create modal HTML structure
const modalHTML = `
<div class="modal fade" id="unified-modal" tabindex="-1" data-bs-backdrop="static">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="unified-modal-title">提示</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body" id="unified-modal-body"></div>
      <div class="modal-footer" id="unified-modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" id="unified-modal-confirm">确定</button>
      </div>
    </div>
  </div>
</div>
`;

// Inject modal into document when DOM is ready
$(document).ready(function() {
    if ($('#unified-modal').length === 0) {
        $('body').append(modalHTML);
    }
});

/**
 * Show unified alert dialog
 * @param {string} message - Message to display
 * @param {string} title - Dialog title (optional)
 * @returns {Promise} Resolves when user clicks OK
 */
window.showAlert = function(message, title = '提示') {
    return new Promise((resolve) => {
        const modal = new bootstrap.Modal(document.getElementById('unified-modal'));
        $('#unified-modal-title').text(title);
        $('#unified-modal-body').html(message);
        $('#unified-modal-footer').html(`
            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">确定</button>
        `);
        
        $('#unified-modal').off('hidden.bs.modal').on('hidden.bs.modal', function() {
            resolve(true);
        });
        
        modal.show();
    });
};

/**
 * Show unified confirm dialog
 * @param {string} message - Message to display
 * @param {string} title - Dialog title (optional)
 * @returns {Promise<boolean>} Resolves to true if confirmed, false if cancelled
 */
window.showConfirm = function(message, title = '确认') {
    return new Promise((resolve) => {
        const modal = new bootstrap.Modal(document.getElementById('unified-modal'));
        $('#unified-modal-title').text(title);
        $('#unified-modal-body').html(message);
        $('#unified-modal-footer').html(`
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" id="modal-cancel-btn">取消</button>
            <button type="button" class="btn btn-primary" id="modal-confirm-btn">确定</button>
        `);
        
        let result = false;
        
        $('#modal-confirm-btn').off('click').on('click', function() {
            result = true;
            modal.hide();
        });
        
        $('#modal-cancel-btn').off('click').on('click', function() {
            result = false;
            modal.hide();
        });
        
        $('#unified-modal').off('hidden.bs.modal').on('hidden.bs.modal', function() {
            resolve(result);
        });
        
        modal.show();
    });
};
