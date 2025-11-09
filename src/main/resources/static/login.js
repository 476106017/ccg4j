(() => {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const alertBox = document.getElementById('auth-alert');
  const tabButtons = document.querySelectorAll('#auth-tabs .nav-link');
  const panes = document.querySelectorAll('.auth-pane');

  function apiRequest(url, options = {}) {
    return fetch(url, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    }).then(async (response) => {
      if (response.ok) {
        if (response.status === 204) {
          return null;
        }
        return response.json();
      }
      let message = '操作失败';
      try {
        const data = await response.json();
        if (data?.message) {
          message = data.message;
        }
      } catch (err) {
        const text = await response.text();
        if (text) {
          message = text;
        }
      }
      throw new Error(message);
    });
  }

  function showAlert(message, type = 'danger') {
    if (!alertBox) return;
    alertBox.textContent = message;
    alertBox.className = `alert alert-${type}`;
  }

  function clearAlert() {
    if (!alertBox) return;
    alertBox.textContent = '';
    alertBox.className = 'alert d-none';
  }

  function switchPane(targetId) {
    panes.forEach(pane => {
      pane.classList.toggle('active', pane.id === targetId);
    });
    tabButtons.forEach(btn => {
      btn.classList.toggle('active', btn.dataset.target === targetId);
    });
    clearAlert();
  }

  async function handleLogin(event) {
    event.preventDefault();
    clearAlert();
    const username = (document.getElementById('login-username')?.value || '').trim();
    const password = (document.getElementById('login-password')?.value || '').trim();
    if (!username || !password) {
      showAlert('请输入用户名和密码');
      return;
    }
    try {
      await apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      });
      showAlert('登录成功！正在跳转...', 'success');
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 500);
    } catch (e) {
      showAlert(e.message || '登录失败，请检查用户名和密码');
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    clearAlert();
    const username = (document.getElementById('register-username')?.value || '').trim();
    const password = (document.getElementById('register-password')?.value || '').trim();
    if (!username || !password) {
      showAlert('请输入用户名和密码');
      return;
    }
    if (username.length < 3) {
      showAlert('用户名至少需要3个字符');
      return;
    }
    if (password.length < 6) {
      showAlert('密码至少需要6个字符');
      return;
    }
    try {
      await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password })
      });
      showAlert('注册成功！正在自动登录...', 'success');
      setTimeout(() => {
        window.location.href = 'index.html';
      }, 800);
    } catch (e) {
      showAlert(e.message || '注册失败，用户名可能已存在');
    }
  }

  async function checkSession() {
    try {
      await apiRequest('/api/auth/session');
      // 如果已登录，跳转到主页
      window.location.href = 'index.html';
    } catch (e) {
      // 未登录，继续显示登录页面
      clearAlert();
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    loginForm?.addEventListener('submit', handleLogin);
    registerForm?.addEventListener('submit', handleRegister);
    tabButtons.forEach(btn => {
      btn.addEventListener('click', () => switchPane(btn.dataset.target));
    });
    // 检查是否已登录
    checkSession();
  });
})();
