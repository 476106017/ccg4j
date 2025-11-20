/**
 * 全局fetch拦截器 - 自动处理登录超时
 */
(function() {
  'use strict';

  // 保存原始的fetch
  const originalFetch = window.fetch;

  // 重写fetch
  window.fetch = function(...args) {
    return originalFetch.apply(this, args)
      .then(response => {
        // 检查是否是401未授权错误
        if (response.status === 401) {
          // 如果不是登录页面，则跳转到登录页
          if (!window.location.pathname.endsWith('login.html')) {
            console.log('Session expired, redirecting to login...');
            window.location.href = '/login.html';
          }
        }
        return response;
      })
      .catch(error => {
        // 传递错误
        throw error;
      });
  };

  console.log('Auth interceptor initialized');
})();
