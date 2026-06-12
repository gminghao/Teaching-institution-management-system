import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout, getToken } from '@/utils/auth'

/**
 * 空闲超时自动登出组合式函数
 * @param {number} timeout 超时时间（毫秒），默认15分钟
 */
export function useIdleTimeout(timeout = 15 * 60 * 1000) {
  const router = useRouter()
  const isIdle = ref(false)
  let idleTimer = null

  const resetTimer = () => {
    isIdle.value = false
    if (idleTimer) {
      clearTimeout(idleTimer)
    }
    idleTimer = setTimeout(() => {
      if (getToken()) {
        isIdle.value = true
        // 调用后端登出接口
        fetch('/api/admin/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${getToken()}`
          }
        }).catch(() => {
          // 忽略登出接口错误
        })
        // 本地登出
        logout()
        // 通知其他标签页
        broadcastLogout()
        // 跳转到登录页
        router.push('/admin/login')
      }
    }, timeout)
  }

  // 广播登出消息
  const broadcastLogout = () => {
    try {
      if (typeof BroadcastChannel !== 'undefined') {
        const channel = new BroadcastChannel('auth_sync')
        channel.postMessage({ type: 'logout' })
        channel.close()
      } else {
        // 降级方案：使用localStorage事件
        localStorage.setItem('auth_logout_event', Date.now().toString())
      }
    } catch {
      // 忽略广播错误
    }
  }

  // 监听用户活动
  const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart']

  onMounted(() => {
    // 初始化计时器
    resetTimer()

    // 添加事件监听
    events.forEach(event => {
      document.addEventListener(event, resetTimer, { passive: true })
    })

    // 监听其他标签页的登出消息
    try {
      if (typeof BroadcastChannel !== 'undefined') {
        const channel = new BroadcastChannel('auth_sync')
        channel.onmessage = (event) => {
          if (event.data?.type === 'logout') {
            logout()
            router.push('/admin/login')
          }
        }
        // 保存channel引用以便清理
        window.__authChannel = channel
      }
    } catch {
      // BroadcastChannel不可用，使用storage事件监听
    }
  })

  onUnmounted(() => {
    // 清理计时器
    if (idleTimer) {
      clearTimeout(idleTimer)
    }

    // 移除事件监听
    events.forEach(event => {
      document.removeEventListener(event, resetTimer)
    })

    // 关闭BroadcastChannel
    if (window.__authChannel) {
      window.__authChannel.close()
      window.__authChannel = null
    }
  })

  return {
    isIdle
  }
}

// 监听localStorage事件（BroadcastChannel降级方案）
window.addEventListener('storage', (event) => {
  if (event.key === 'auth_logout_event') {
    const router = window.__appRouter
    if (router && getToken()) {
      logout()
      router.push('/admin/login')
    }
  }
})
