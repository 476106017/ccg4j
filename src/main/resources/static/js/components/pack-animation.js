/**
 * 卡包开包动画 - 3D散开效果
 * 模拟一摞卡牌从上往下砸到地上散开，带边界碰撞反弹
 */
(function() {
  'use strict';

  // 动画配置
  const CONFIG = {
    DROP_DURATION: 800,      // 下落动画时长(ms)
    DROP_HEIGHT: -500,       // 下落起始高度(px)
    SCATTER_DURATION: 600,   // 散开动画时长(ms)
    SCATTER_DELAY: 200,      // 散开延迟(ms)
    CARD_DELAY: 50,          // 每张卡散开间隔(ms)
    ROTATION_RANGE: 35,      // 旋转角度范围(度) - 减小以避免过大
    SPREAD_RADIUS: 450,      // 散开半径(px) - 增大以充满背景
    PERSPECTIVE: 1200,       // 3D透视距离
    TILT_ANGLE: 15,          // 倾斜角度
    CARD_WIDTH: 200,         // 卡片宽度
    CARD_HEIGHT: 280,        // 卡片高度
    BOUNCE_DAMPING: 0.6,     // 反弹阻尼系数
    MAX_BOUNCE_COUNT: 3,     // 最大反弹次数
    PADDING: 50              // 边界内边距
  };

  /**
   * 检测卡片是否超出容器边界（考虑旋转后的实际占用空间）
   */
  function checkBounds(x, y, rotateZ, containerWidth, containerHeight) {
    // 计算旋转后卡片的外接矩形
    const rad = Math.abs(rotateZ) * Math.PI / 180;
    const sin = Math.abs(Math.sin(rad));
    const cos = Math.abs(Math.cos(rad));
    
    // 旋转后的宽高
    const rotatedWidth = CONFIG.CARD_WIDTH * cos + CONFIG.CARD_HEIGHT * sin;
    const rotatedHeight = CONFIG.CARD_WIDTH * sin + CONFIG.CARD_HEIGHT * cos;
    
    const halfWidth = rotatedWidth / 2;
    const halfHeight = rotatedHeight / 2;
    
    let newX = x;
    let newY = y;
    let bounced = false;
    
    // 可用空间（容器尺寸减去内边距）
    const maxX = (containerWidth / 2) - CONFIG.PADDING;
    const maxY = (containerHeight / 2) - CONFIG.PADDING;
    
    // 检查左右边界
    if (x - halfWidth < -maxX) {
      newX = -maxX + halfWidth;
      bounced = true;
    } else if (x + halfWidth > maxX) {
      newX = maxX - halfWidth;
      bounced = true;
    }
    
    // 检查上下边界
    if (y - halfHeight < -maxY) {
      newY = -maxY + halfHeight;
      bounced = true;
    } else if (y + halfHeight > maxY) {
      newY = maxY - halfHeight;
      bounced = true;
    }
    
    return { x: newX, y: newY, bounced };
  }

  /**
   * 生成两排布局位置（上5张下5张）
   */
  function generateGridTransform(index, total, containerWidth, containerHeight) {
    const cardsPerRow = 5;
    const row = Math.floor(index / cardsPerRow); // 0 = 上排, 1 = 下排
    const col = index % cardsPerRow; // 0-4
    
    // 计算可用空间
    const availableWidth = containerWidth - CONFIG.PADDING * 2;
    const availableHeight = containerHeight - CONFIG.PADDING * 2;
    
    // 卡片间距
    const cardSpacing = 20;
    const totalCardWidth = CONFIG.CARD_WIDTH * cardsPerRow + cardSpacing * (cardsPerRow - 1);
    const rowHeight = CONFIG.CARD_HEIGHT;
    const rowSpacing = 30;
    
    // 计算起始位置（居中，并稍微向左上偏移）
    const offsetX = -30; // 向左偏移30px
    const offsetY = -40; // 向上偏移40px
    const startX = -totalCardWidth / 2 + CONFIG.CARD_WIDTH / 2 + offsetX;
    const startY = -rowHeight - rowSpacing / 2 + offsetY;
    
    // 计算该卡片的位置
    const x = startX + col * (CONFIG.CARD_WIDTH + cardSpacing);
    const y = startY + row * (rowHeight + rowSpacing);
    
    // 轻微随机旋转
    const rotateZ = (Math.random() - 0.5) * 10; // ±5度
    const rotateX = -CONFIG.TILT_ANGLE + (Math.random() - 0.5) * 8;
    
    return {
      x,
      y,
      rotateZ,
      rotateX,
      scale: 1.0
    };
  }

  /**
   * 添加轻微的落地反弹效果
   */
  function addBounceEffect(wrapper, finalTransform, containerWidth, containerHeight, delay) {
    // 简单的落地反弹效果
    const bounceDistance = 15;
    const bounceDuration = 180;
    
    setTimeout(() => {
      // 第一次反弹 - 轻微向上
      wrapper.style.transition = `transform ${bounceDuration}ms ease-out`;
      wrapper.style.transform = `
        translate3d(${finalTransform.x}px, ${finalTransform.y - bounceDistance}px, 0)
        rotateX(${finalTransform.rotateX}deg)
        rotateZ(${finalTransform.rotateZ}deg)
        scale(${finalTransform.scale})
      `;
      
      // 回落到最终位置
      setTimeout(() => {
        wrapper.style.transition = `transform ${bounceDuration * 1.5}ms cubic-bezier(0.34, 1.56, 0.64, 1)`;
        wrapper.style.transform = `
          translate3d(${finalTransform.x}px, ${finalTransform.y}px, 0)
          rotateX(${finalTransform.rotateX}deg)
          rotateZ(${finalTransform.rotateZ}deg)
          scale(${finalTransform.scale})
        `;
      }, bounceDuration);
      
    }, delay);
  }

  /**
   * 开始卡包动画
   * @param {HTMLElement} container - 卡片容器
   * @param {Array} cardElements - 卡片DOM元素数组
   */
  function startPackAnimation(container, cardElements) {
    if (!container || !cardElements || cardElements.length === 0) {
      console.error('Invalid animation parameters');
      return;
    }

    // 设置容器3D透视
    container.style.perspective = `${CONFIG.PERSPECTIVE}px`;
    container.style.perspectiveOrigin = '50% 50%';
    
    // 获取容器尺寸
    const containerRect = container.getBoundingClientRect();
    const containerWidth = containerRect.width;
    const containerHeight = containerRect.height;
    
    const total = cardElements.length;
    
    cardElements.forEach((card, index) => {
      const wrapper = card.closest('.pack-card-wrap');
      if (!wrapper) return;

      // 生成该卡牌的网格位置
      const transform = generateGridTransform(index, total, containerWidth, containerHeight);
      
      // 设置初始状态 - 卡牌堆叠在顶部
      wrapper.style.transition = 'none';
      wrapper.style.transform = `
        translate3d(0, ${CONFIG.DROP_HEIGHT}px, ${index * 2}px)
        rotateX(${CONFIG.TILT_ANGLE}deg)
        rotateZ(0deg)
        scale(1)
      `;
      wrapper.style.opacity = '1';
      wrapper.style.zIndex = total - index; // 顶部的卡z-index最大
      
      // 强制重绘
      void wrapper.offsetHeight;
      
      // 第一阶段：下落动画
      setTimeout(() => {
        wrapper.style.transition = `transform ${CONFIG.DROP_DURATION}ms cubic-bezier(0.34, 1.56, 0.64, 1)`;
        wrapper.style.transform = `
          translate3d(0, 0, ${index * 2}px)
          rotateX(${CONFIG.TILT_ANGLE}deg)
          rotateZ(0deg)
          scale(1)
        `;
      }, 50);
      
      // 第二阶段：散开动画
      const scatterDelay = CONFIG.DROP_DURATION + CONFIG.SCATTER_DELAY + index * CONFIG.CARD_DELAY;
      setTimeout(() => {
        wrapper.style.transition = `
          transform ${CONFIG.SCATTER_DURATION}ms cubic-bezier(0.18, 0.89, 0.32, 1.28),
          z-index 0ms ${CONFIG.SCATTER_DURATION}ms
        `;
        wrapper.style.transform = `
          translate3d(${transform.x}px, ${transform.y}px, 0)
          rotateX(${transform.rotateX}deg)
          rotateZ(${transform.rotateZ}deg)
          scale(${transform.scale})
        `;
        
        // 散开后调整z-index，让靠近中心的卡片在上层
        const distanceFromCenter = Math.sqrt(transform.x * transform.x + transform.y * transform.y);
        wrapper.style.zIndex = Math.floor(1000 - distanceFromCenter);
      }, scatterDelay);
      
      // 第三阶段：反弹效果
      addBounceEffect(
        wrapper, 
        transform, 
        containerWidth, 
        containerHeight, 
        scatterDelay + CONFIG.SCATTER_DURATION
      );
    });
  }

  /**
   * 重置动画
   */
  function resetAnimation(container, cardElements) {
    if (!container || !cardElements) return;
    
    cardElements.forEach((card) => {
      const wrapper = card.closest('.pack-card-wrap');
      if (!wrapper) return;
      
      wrapper.style.transition = 'none';
      wrapper.style.transform = 'none';
      wrapper.style.opacity = '0';
      wrapper.style.zIndex = '';
    });
  }

  // 导出到全局
  window.PackAnimation = {
    start: startPackAnimation,
    reset: resetAnimation,
    config: CONFIG
  };

  console.log('Pack animation module loaded');
})();
