(function() {
  'use strict';

  // 全局状态
  const state = {
    user: null,
    cards: [],
    packs: []
  };

  // DOM元素引用
  const logoutBtn = document.getElementById('logout-btn');
  const userDisplayName = document.getElementById('user-display-name');
  const userTicketCount = document.getElementById('user-ticket-count');
  const userDustCount = document.getElementById('user-dust-count');
  const userDustCountCollection = document.getElementById('user-dust-count-collection');
  const userInfoContainer = document.getElementById('user-info-container');
  const collectionGrid = document.getElementById('collection-grid');
  const searchInput = document.getElementById('collection-search-input');
  const typeFilter = document.getElementById('collection-type-filter');
  const pageAlert = document.getElementById('page-alert');
  const packCarouselInner = document.getElementById('pack-carousel-inner');
  const packCarouselIndicators = document.getElementById('pack-carousel-indicators');

  // 工具函数：显示提示信息
  function showAlert(message, type = 'info') {
    if (!pageAlert) return;
    pageAlert.className = `alert alert-${type} position-fixed top-0 start-50 translate-middle-x mt-3 shadow-lg`;
    pageAlert.style.zIndex = '9999';
    pageAlert.style.minWidth = '300px';
    pageAlert.style.maxWidth = '600px';
    pageAlert.style.animation = 'slideDown 0.3s ease-out';
    pageAlert.textContent = message;
    pageAlert.classList.remove('d-none');
    setTimeout(() => {
      pageAlert.style.animation = 'slideUp 0.3s ease-in';
      setTimeout(() => {
        pageAlert.classList.add('d-none');
      }, 300);
    }, 3000);
  }

  // 全局加载提示
  let loadingOverlay = null;
  
  function showLoading(message = '加载中...') {
    if (!loadingOverlay) {
      loadingOverlay = document.createElement('div');
      loadingOverlay.id = 'global-loading-overlay';
      loadingOverlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
      `;
      loadingOverlay.innerHTML = `
        <div style="background: white; padding: 30px 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); text-align: center;">
          <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;">
            <span class="visually-hidden">Loading...</span>
          </div>
          <div style="font-size: 16px; color: #333; font-weight: 500;" id="loading-message">${message}</div>
        </div>
      `;
      document.body.appendChild(loadingOverlay);
    } else {
      loadingOverlay.style.display = 'flex';
      const messageEl = loadingOverlay.querySelector('#loading-message');
      if (messageEl) messageEl.textContent = message;
    }
  }
  
  function hideLoading() {
    if (loadingOverlay) {
      loadingOverlay.style.display = 'none';
    }
  }
  
  // 暴露全局方法
  window.showLoading = showLoading;
  window.hideLoading = hideLoading;

  // 设置区域可见性
  function setSectionsVisible(visible) {
    if (userInfoContainer) {
      userInfoContainer.classList.toggle('d-none', !visible);
    }
  }

  // 检查登录状态
  async function fetchSession() {
    try {
      const response = await fetch('/api/auth/session', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (data && data.username) {
          state.user = {
            username: data.username,
            tickets: data.tickets || 0,
            arcaneDust: data.arcaneDust || 0,
            matchRating: data.matchRating || 1000
          };
          updateUserDisplay();
          await Promise.all([fetchUserCollection(), fetchAvailablePacks(), fetchUserRating(), fetchBorderlandStatus()]);
          setSectionsVisible(true);
        } else {
          redirectToLogin();
        }
      } else {
        redirectToLogin();
      }
    } catch (error) {
      console.error('Session check failed:', error);
      redirectToLogin();
    }
  }

  // 自动刷新session - 每5分钟检查一次
  function startSessionRefresh() {
    // 每5分钟刷新一次session（5 * 60 * 1000 = 300000ms）
    setInterval(async () => {
      try {
        const response = await fetch('/api/auth/session', {
          method: 'GET',
          credentials: 'include'
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data && data.tickets !== undefined) {
            state.user.tickets = data.tickets;
            updateUserDisplay();
          }
          console.log('Session refreshed');
        }
        // 401错误会被auth-interceptor自动处理跳转
      } catch (error) {
        console.error('Session refresh failed:', error);
      }
    }, 5 * 60 * 1000);
  }

  // 跳转到登录页
  function redirectToLogin() {
    window.location.href = '/login.html';
  }

  // 更新用户显示信息
  function updateUserDisplay() {
    if (userDisplayName && state.user) {
      userDisplayName.textContent = state.user.username;
    }
    if (userTicketCount && state.user) {
      userTicketCount.textContent = state.user.tickets || 0;
    }
    if (userDustCount && state.user) {
      userDustCount.textContent = state.user.arcaneDust || 0;
    }
    if (userDustCountCollection && state.user) {
      userDustCountCollection.textContent = state.user.arcaneDust || 0;
    }
    // 更新积分显示
    const userRatingElement = document.getElementById('user-rating');
    if (userRatingElement && state.user) {
      userRatingElement.textContent = state.user.matchRating || 1000;
    }
  }

  // 获取用户分数
  async function fetchUserRating() {
    try {
      const response = await fetch('/api/rating/current', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (data && data.rating !== undefined) {
          state.user.matchRating = data.rating;
          updateUserDisplay();
        }
      }
    } catch (error) {
      console.error('Fetch rating failed:', error);
    }
  }

  // 获取弥留之国状态
  async function fetchBorderlandStatus() {
    const statusElement = document.getElementById('borderland-status');
    
    // 显示加载中状态
    if (statusElement) {
      statusElement.innerHTML = `
        <span class="badge" style="background: rgba(255,255,255,0.25); color: white;">
          <span class="spinner-border spinner-border-sm me-1" role="status"></span>
          加载中...
        </span>
      `;
    }
    
    try {
      const response = await fetch('/api/borderland/visa/status', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (statusElement) {
          if (data && data.status === 'ACTIVE') {
            // 持有签证：显示进度
            const deckData = data.deckData || '';
            const uniqueCards = deckData ? new Set(deckData.split(',')).size : 0;
            
            const expiryDate = new Date();
            expiryDate.setDate(expiryDate.getDate() + (data.daysRemaining || 0));
            const year = expiryDate.getFullYear();
            const month = expiryDate.getMonth() + 1;
            const day = expiryDate.getDate();
            statusElement.innerHTML = `
              <span class="badge" style="background: rgba(255,255,255,0.35); color: white; font-weight: bold;">✓ 持有签证</span>
              <div class="small mt-1" style="color: rgba(255,255,255,0.85);">
                进度 ${uniqueCards}/54
              </div>
              <div class="small" style="color: rgba(255,255,255,0.75);">
                于${year}年${month}月${day}日过期
              </div>
            `;
          } else if (data && data.status === 'PUNISHED' && data.punishmentEndTime) {
            // 惩罚期：显示倒计时（醒目的白色文字配深色背景）
            statusElement.innerHTML = `
              <span class="badge" style="background: rgba(220, 53, 69, 0.9); color: white; font-weight: bold;">☠️ 死亡惩罚</span>
              <div class="small mt-2 px-3 py-2" id="punishment-countdown" 
                   style="background: rgba(0, 0, 0, 0.6); color: #fff; font-weight: bold; border-radius: 8px; text-shadow: 0 2px 4px rgba(0,0,0,0.5);">
                计算中...
              </div>
            `;
            
            // 启动倒计时
            updatePunishmentCountdown(data.punishmentEndTime);
          } else {
            // 未办理签证或过期：可申请签证（绿色）
            statusElement.innerHTML = `
              <span class="badge" style="background: rgba(40, 167, 69, 0.8); color: white; font-weight: bold;">可申请签证</span>
            `;
          }
        }
      } else {
        // 请求失败，显示未办理签证
        if (statusElement) {
          statusElement.innerHTML = `
            <span class="badge" style="background: rgba(255,255,255,0.25); color: white;">未办理签证</span>
          `;
        }
      }
    } catch (error) {
      console.error('Fetch borderland status failed:', error);
      // 出错时显示未办理签证
      if (statusElement) {
        statusElement.innerHTML = `
          <span class="badge" style="background: rgba(255,255,255,0.25); color: white;">未办理签证</span>
        `;
      }
    }
  }

  // 更新惩罚倒计时
  function updatePunishmentCountdown(endTimeStr) {
    const countdownElement = document.getElementById('punishment-countdown');
    if (!countdownElement) return;

    function update() {
      const now = new Date();
      const endTime = new Date(endTimeStr);
      const diff = endTime - now;

      if (diff <= 0) {
        countdownElement.textContent = '惩罚已结束';
        // 刷新状态
        fetchBorderlandStatus();
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      countdownElement.textContent = `${hours}小时${minutes}分${seconds}秒`;
      
      setTimeout(update, 1000);
    }

    update();
  }

  // 获取用户收藏
  async function fetchUserCollection() {
    try {
      const response = await fetch('/api/user/collection', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        state.cards = data.cards || [];
        if (data.tickets !== undefined) {
          state.user.tickets = data.tickets;
        }
        if (data.arcaneDust !== undefined) {
          state.user.arcaneDust = data.arcaneDust;
        }
        updateUserDisplay();
        renderCollection();
      } else {
        showAlert('获取收藏失败', 'danger');
      }
    } catch (error) {
      console.error('Fetch collection failed:', error);
      showAlert('获取收藏失败', 'danger');
    }
  }

  // 获取可用卡包
  async function fetchAvailablePacks() {
    try {
      const response = await fetch('/api/user/packs', {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        state.packs = data.packs || [];
        if (data.tickets !== undefined) {
          state.user.tickets = data.tickets;
          updateUserDisplay();
        }
        renderPacks();
      } else {
        showAlert('获取卡包列表失败', 'danger');
      }
    } catch (error) {
      console.error('Fetch packs failed:', error);
      showAlert('获取卡包列表失败', 'danger');
    }
  }

  // 分解卡牌
  async function disenchantCard(cardCode, cardName) {
    if (!confirm(`确定要分解多余的"${cardName}"吗？将保留3张，其余转换为奥术之尘。`)) {
      return;
    }

    try {
      const response = await fetch(`/api/user/disenchant?cardCode=${encodeURIComponent(cardCode)}`, {
        method: 'POST',
        credentials: 'include'
      });

      if (response.ok) {
        const result = await response.json();
        showAlert(
          `成功分解 ${result.disenchantCount} 张卡牌，获得 ${result.dustGained} 奥术之尘！当前总计：${result.totalDust} 🌟`,
          'success'
        );
        
        // 更新用户奥术之尘显示
        if (state.user) {
          state.user.arcaneDust = result.totalDust;
          updateUserDisplay();
        }
        
        // 重新获取收藏信息
        await fetchUserCollection();
      } else {
        const error = await response.text();
        showAlert(error || '分解失败', 'danger');
      }
    } catch (error) {
      console.error('Disenchant failed:', error);
      showAlert('分解失败', 'danger');
    }
  }

  // 一键分解所有多余卡牌
  window.batchDisenchantCards = async function() {
    // 统计有多少张多余卡牌
    const excessCards = state.cards.filter(card => card.quantity > 3);
    
    if (excessCards.length === 0) {
      showAlert('没有多余的卡牌可分解（数量>3）', 'info');
      return;
    }
    
    const totalExcess = excessCards.reduce((sum, card) => sum + (card.quantity - 3), 0);
    
    if (!confirm(`确定要分解所有多余的卡牌吗？\n共 ${excessCards.length} 种卡牌，${totalExcess} 张卡牌将被分解。`)) {
      return;
    }

    try {
      const response = await fetch('/api/user/disenchant-all', {
        method: 'POST',
        credentials: 'include'
      });

      if (response.ok) {
        const result = await response.json();
        showAlert(
          `成功分解 ${result.cardsProcessed} 种卡牌共 ${result.totalCardsDisenchanted} 张，获得 ${result.totalDustGained} 奥术之尘！当前总计：${result.totalDust} 🌟`,
          'success'
        );
        
        // 更新用户奥术之尘显示
        if (state.user) {
          state.user.arcaneDust = result.totalDust;
          updateUserDisplay();
        }
        
        // 重新获取收藏信息
        await fetchUserCollection();
      } else {
        const error = await response.text();
        showAlert(error || '批量分解失败', 'danger');
      }
    } catch (error) {
      console.error('Batch disenchant failed:', error);
      showAlert('批量分解失败', 'danger');
    }
  };

  // 渲染收藏卡牌
  function renderCollection() {
    if (!collectionGrid) return;

    const searchTerm = searchInput?.value.toLowerCase() || '';
    const typeValue = typeFilter?.value || '';

    // 只显示拥有的卡牌 (quantity > 0)
    let filteredCards = state.cards.filter(card => {
      const isOwned = card.quantity > 0;
      const matchSearch = !searchTerm || 
        card.name?.toLowerCase().includes(searchTerm) ||
        card.job?.toLowerCase().includes(searchTerm) ||
        card.mark?.toLowerCase().includes(searchTerm) ||
        (card.race && card.race.some(r => r.toLowerCase().includes(searchTerm)));
      
      const matchType = !typeValue || card.type === typeValue;
      
      return isOwned && matchSearch && matchType;
    });

    // 按费用升序排序
    filteredCards.sort((a, b) => (a.cost || 0) - (b.cost || 0));

    collectionGrid.innerHTML = '';
    const fragment = document.createDocumentFragment();

    filteredCards.forEach(card => {
      const col = document.createElement('div');
      col.className = 'col-md-3 mb-3';
      
      // 构建种族标签
      let raceHtml = '';
      if (card.race && card.race.length > 0) {
        raceHtml = `<div class="race">${card.race.join(' ')}</div>`;
      }
      
      // 构建关键字（粗体显示）
      const keywords = card.keywords || [];
      const keywordsHtml = Array.isArray(keywords) && keywords.length > 0 ? 
        `<b class="keyword">${keywords.join(' ')}</b>` : '';
      
      // 效果描述
      const mark = card.mark || '';
      const descriptionContent = keywordsHtml || mark ? 
        `${keywordsHtml}${keywordsHtml && mark ? '\n' : ''}${mark}` : '';
      
      // 将中文 type 映射为英文 TYPE（用于灰色水印显示）
      const typeMap = {
        '随从': 'FOLLOW',
        '法术': 'SPELL',
        '护符': 'AMULET',
        '装备': 'EQUIP'
      };
      const typeEn = typeMap[card.type] || card.type || '未知';
      
      col.innerHTML = `
        <div class="card ${typeEn} ${card.rarity || 'BRONZE'}" data-card-code="${card.code}" data-keywords='${JSON.stringify(keywords)}' data-mark='${mark.replace(/'/g, "\\'")}'>
          <div class="card-inner">
            <div class="cost">${card.cost ?? 0}</div>
            <div class="type">${typeEn}</div>
            ${raceHtml}
            <div class="name">${card.name}</div>
            ${card.type === '随从' && card.atk !== undefined && card.hp !== undefined ? 
              `<div class="atk">${card.atk}</div><div class="hp">${card.hp}</div>` : ''}
            <div class="description">
              <p>${descriptionContent}</p>
            </div>
            ${card.quantity > 1 ? `<div class="quantity">×${card.quantity}</div>` : ''}
          </div>
          <div class="job" style="display: inline-block;">${card.job || ''}</div>
        </div>
      `;
      
      // 添加右键菜单（仅当数量>3时显示分解选项）
      const cardElement = col.querySelector('.card');
      cardElement.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        
        const menuItems = [];
        
        if (card.quantity > 3) {
          const rarityDust = {
            'BRONZE': 50,
            'SILVER': 200,
            'GOLD': 800,
            'RAINBOW': 1600,
            'LEGENDARY': 2400
          };
          const dustPerCard = rarityDust[card.rarity] || 50;
          const excessCount = card.quantity - 3;
          const totalDust = dustPerCard * excessCount;
          
          menuItems.push({
            icon: '✨',
            label: `分解多余卡牌 (×${excessCount}, +${totalDust}🌟)`,
            action: () => disenchantCard(card.code, card.name)
          });
        } else {
          menuItems.push({
            icon: '❌',
            label: '数量不足3张，无法分解',
            disabled: true
          });
        }
        
        ContextMenu.show(e, menuItems);
      });
      
      fragment.appendChild(col);
    });

    collectionGrid.appendChild(fragment);

    if (filteredCards.length === 0) {
      collectionGrid.innerHTML = '<div class="col-12 text-center text-muted py-5">暂无卡牌</div>';
    }
  }

  // 渲染卡包列表（轮播图形式）
  function renderPacks() {
    if (!packCarouselInner || !packCarouselIndicators) return;

    packCarouselInner.innerHTML = '';
    packCarouselIndicators.innerHTML = '';

    state.packs.forEach((pack, index) => {
      // 创建轮播指示器
      const indicator = document.createElement('button');
      indicator.type = 'button';
      indicator.setAttribute('data-bs-target', '#packCarousel');
      indicator.setAttribute('data-bs-slide-to', index);
      if (index === 0) {
        indicator.classList.add('active');
        indicator.setAttribute('aria-current', 'true');
      }
      indicator.setAttribute('aria-label', `Slide ${index + 1}`);
      packCarouselIndicators.appendChild(indicator);

      // 创建轮播项
      const carouselItem = document.createElement('div');
      carouselItem.className = `carousel-item ${index === 0 ? 'active' : ''}`;
      
      carouselItem.innerHTML = `
        <div class="d-flex justify-content-center align-items-center" style="min-height: 400px;">
          <div class="text-center p-5">
            <div class="pack-card-display mb-4">
              <div class="pack-visual" style="
                width: 300px;
                height: 400px;
                margin: 0 auto;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 20px;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                position: relative;
                overflow: hidden;
              ">
                <div style="
                  position: absolute;
                  top: 0;
                  left: 0;
                  right: 0;
                  bottom: 0;
                  background: radial-gradient(circle at 30% 50%, rgba(255,255,255,0.2), transparent);
                "></div>
                <h2 class="text-white mb-3" style="font-size: 2.5rem; font-weight: bold; z-index: 1;">${pack.name}</h2>
                <p class="text-white-50 px-4" style="font-size: 1.1rem; z-index: 1;">${pack.description || '精彩内容等你来探索'}</p>
                <div style="
                  position: absolute;
                  bottom: 20px;
                  left: 50%;
                  transform: translateX(-50%);
                  background: rgba(255,255,255,0.2);
                  padding: 10px 20px;
                  border-radius: 30px;
                  z-index: 1;
                ">
                  <span class="text-white" style="font-weight: bold;">10张卡牌</span>
                </div>
              </div>
            </div>
            <button class="btn btn-lg btn-primary px-5 py-3" data-pack-code="${pack.code}" style="font-size: 1.2rem; border-radius: 50px;">
              <i class="bi bi-gift-fill me-2"></i>抽取卡包
            </button>
            <div class="mt-3 text-muted">
              <small>需要 1 张抽奖券</small>
            </div>
          </div>
        </div>
      `;
      
      packCarouselInner.appendChild(carouselItem);
    });

    if (state.packs.length === 0) {
      packCarouselInner.innerHTML = `
        <div class="carousel-item active">
          <div class="d-flex justify-content-center align-items-center" style="min-height: 400px;">
            <div class="text-center text-muted py-5">
              <h4>暂无可用卡包</h4>
              <p>请稍后再来查看</p>
            </div>
          </div>
        </div>
      `;
    }
  }

  // 打开卡包
  async function openPack(packCode) {
    if (!state.user || (state.user.tickets || 0) < 1) {
      showAlert('抽奖券不足', 'warning');
      return;
    }

    try {
      const response = await fetch('/api/user/open-pack', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ packCode })
      });

      if (response.ok) {
        const data = await response.json();
        state.user.tickets = data.remainingTickets;
        updateUserDisplay();
        
        // 显示抽卡结果
        renderPackResults(data.cards);
        
        // 刷新收藏
        await fetchUserCollection();
        
        showAlert(`成功抽取 ${data.cards.length} 张卡牌！`, 'success');
      } else {
        const error = await response.text();
        showAlert(error || '抽卡失败', 'danger');
      }
    } catch (error) {
      console.error('Open pack failed:', error);
      showAlert('抽卡失败', 'danger');
    }
  }

  // 显示抽卡结果
  function renderPackResults(cards) {
    const resultGrid = document.getElementById('pack-result-grid');
    if (!resultGrid) return;

    resultGrid.innerHTML = '';
    resultGrid.classList.add('pack-result-row');
    const fragment = document.createDocumentFragment();
    const cardElements = [];

    cards.forEach((card, index) => {
      const wrap = document.createElement('div');
      wrap.className = 'pack-card-wrap';
      
      // 构建关键词标签
      let raceHtml = '';
      if (card.race && card.race.length > 0) {
        raceHtml = `<div class="race">${card.race.join(' ')}</div>`;
      }
      
      // 准备关键词和标记数据用于hover提示
      // 如果没有 keywords 字段，使用 race 作为 keywords
      const keywords = card.keywords || card.race || [];
      const keywordsJson = JSON.stringify(keywords);
      const keywordsHtml = Array.isArray(keywords) && keywords.length > 0 ? 
        `<b class="keyword">${keywords.join(' ')}</b>` : '';
      const mark = card.mark || '';
      const descriptionContent = keywordsHtml || mark ? 
        `${keywordsHtml}${keywordsHtml && mark ? '\n' : ''}${mark}` : '';
      const markText = mark.replace(/'/g, "\\'");
      
      // 将中文 type 映射为英文 TYPE（用于灰色水印显示）
      const typeMap = {
        '随从': 'FOLLOW',
        '法术': 'SPELL',
        '护符': 'AMULET',
        '装备': 'EQUIP'
      };
      const typeEn = typeMap[card.type] || card.type || '未知';
      
      wrap.innerHTML = `
        <div class="card ${typeEn} ${card.rarity || 'BRONZE'}" data-keywords='${keywordsJson}' data-mark='${markText}'>
          <div class="card-inner">
            <div class="cost">${card.cost ?? 0}</div>
            <div class="type">${typeEn}</div>
            ${raceHtml}
            <div class="name">${card.name}</div>
            ${card.type === '随从' && card.atk !== undefined && card.hp !== undefined ? 
              `<div class="atk">${card.atk}</div><div class="hp">${card.hp}</div>` : ''}
            <div class="description">
              <p>${descriptionContent}</p>
            </div>
          </div>
          <div class="job" style="display: inline-block;">${card.job || ''}</div>
        </div>
      `;
      fragment.appendChild(wrap);
      cardElements.push(wrap.querySelector('.card'));
    });

    resultGrid.appendChild(fragment);
    
    // 显示模态框
    const modal = new bootstrap.Modal(document.getElementById('pack-result-modal'));
    modal.show();
    
    // 等待模态框完全显示后启动动画
    const modalElement = document.getElementById('pack-result-modal');
    modalElement.addEventListener('shown.bs.modal', function onShown() {
      modalElement.removeEventListener('shown.bs.modal', onShown);
      
      // 启动3D散开动画
      if (window.PackAnimation) {
        setTimeout(() => {
          window.PackAnimation.start(resultGrid, cardElements);
        }, 100);
      }
    });
  }

  // 退出登录
  async function handleLogout() {
    try {
      const response = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
      });

      if (response.ok) {
        redirectToLogin();
      } else {
        showAlert('退出失败', 'danger');
      }
    } catch (error) {
      console.error('Logout failed:', error);
      showAlert('退出失败', 'danger');
    }
  }

  // 初始化事件监听
  function initEventListeners() {
    // 退出登录
    logoutBtn?.addEventListener('click', handleLogout);
    
    // 搜索和筛选
    searchInput?.addEventListener('input', () => renderCollection());
    typeFilter?.addEventListener('change', () => renderCollection());
    
    // 卡包抽取按钮
    document.addEventListener('click', (event) => {
      const button = event.target instanceof HTMLElement
        ? event.target.closest('button[data-pack-code]')
        : null;
      
      if (button) {
        const packCode = button.dataset.packCode;
        if (packCode) {
          openPack(packCode);
        }
      }
    });
  }

  // CSS动画
  const style = document.createElement('style');
  style.textContent = `
    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `;
  document.head.appendChild(style);

  // 页面加载时初始化
  document.addEventListener('DOMContentLoaded', () => {
    setSectionsVisible(false);
    initEventListeners();
    fetchSession();
    startSessionRefresh(); // 启动自动刷新session
  });
})();
