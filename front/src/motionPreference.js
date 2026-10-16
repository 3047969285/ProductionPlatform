// 动效开关的唯一来源。
// 之前各处直接读系统的 prefers-reduced-motion：Windows 里关掉"显示动画"后整站变成静态，
// 登录页还会退化成要手动点"播放开场"。现在默认始终播放动效，
// 只有用户在导航栏里手动关闭（记在 localStorage）才降级。
const KEY = 'wb-motion'

export function motionOff() {
  try { return localStorage.getItem(KEY) === 'off' } catch { return false }
}

export function setMotionOff(off) {
  try {
    if (off) localStorage.setItem(KEY, 'off')
    else localStorage.removeItem(KEY)
  } catch { /* 隐私模式下无法持久化，忽略 */ }
}

const REDUCE_QUERY = /prefers-reduced-motion\s*:\s*reduce/

export function installMotionPreference() {
  if (typeof window === 'undefined' || !window.matchMedia || window.__wbMotionPatched) return
  window.__wbMotionPatched = true
  const original = window.matchMedia.bind(window)
  const off = motionOff()
  document.documentElement.toggleAttribute('data-reduce-motion', off)

  // 所有 JS 动效（含 gsap.matchMedia）都经由 window.matchMedia 判断，这里统一改写结果。
  window.matchMedia = (query) => {
    if (!REDUCE_QUERY.test(String(query))) return original(query)
    return {
      matches: off,
      media: String(query),
      onchange: null,
      addEventListener() {},
      removeEventListener() {},
      addListener() {},
      removeListener() {},
      dispatchEvent: () => false,
    }
  }
}
