/**
 * 轻量级右键菜单组件
 */
const ContextMenu = {
    currentMenu: null,

    /**
     * 显示右键菜单
     * @param {MouseEvent} event - 鼠标事件
     * @param {Array} items - 菜单项 [{label: '删除', action: () => {}, disabled: false}]
     */
    show(event, items) {
        event.preventDefault();
        
        // 移除旧菜单
        this.hide();
        
        // 创建菜单容器
        const menu = document.createElement('div');
        menu.className = 'context-menu';
        menu.style.left = event.pageX + 'px';
        menu.style.top = event.pageY + 'px';
        
        // 添加菜单项
        items.forEach(item => {
            if (item.separator) {
                const separator = document.createElement('div');
                separator.className = 'context-menu-separator';
                menu.appendChild(separator);
            } else {
                const menuItem = document.createElement('div');
                menuItem.className = 'context-menu-item';
                if (item.disabled) {
                    menuItem.classList.add('disabled');
                }
                
                menuItem.innerHTML = `
                    ${item.icon ? `<span class="context-menu-icon">${item.icon}</span>` : ''}
                    <span class="context-menu-label">${item.label}</span>
                `;
                
                if (!item.disabled && item.action) {
                    menuItem.addEventListener('click', () => {
                        item.action();
                        this.hide();
                    });
                }
                
                menu.appendChild(menuItem);
            }
        });
        
        document.body.appendChild(menu);
        this.currentMenu = menu;
        
        // 调整位置防止溢出
        const rect = menu.getBoundingClientRect();
        if (rect.right > window.innerWidth) {
            menu.style.left = (event.pageX - rect.width) + 'px';
        }
        if (rect.bottom > window.innerHeight) {
            menu.style.top = (event.pageY - rect.height) + 'px';
        }
        
        // 点击其他地方关闭菜单
        setTimeout(() => {
            document.addEventListener('click', this.hideHandler);
            document.addEventListener('contextmenu', this.hideHandler);
        }, 0);
    },
    
    hideHandler: function() {
        ContextMenu.hide();
    },
    
    hide() {
        if (this.currentMenu) {
            this.currentMenu.remove();
            this.currentMenu = null;
        }
        document.removeEventListener('click', this.hideHandler);
        document.removeEventListener('contextmenu', this.hideHandler);
    }
};

// 添加样式
const style = document.createElement('style');
style.textContent = `
    .context-menu {
        position: absolute;
        background: white;
        border: 1px solid #ccc;
        border-radius: 4px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        padding: 4px 0;
        min-width: 150px;
        z-index: 10000;
        font-size: 14px;
    }
    
    .context-menu-item {
        padding: 8px 16px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 8px;
        transition: background-color 0.2s;
    }
    
    .context-menu-item:hover:not(.disabled) {
        background-color: #f0f0f0;
    }
    
    .context-menu-item.disabled {
        color: #999;
        cursor: not-allowed;
    }
    
    .context-menu-separator {
        height: 1px;
        background-color: #e0e0e0;
        margin: 4px 0;
    }
    
    .context-menu-icon {
        width: 16px;
        text-align: center;
    }
    
    .context-menu-label {
        flex: 1;
    }
`;
document.head.appendChild(style);
