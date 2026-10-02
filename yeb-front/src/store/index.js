import Vue from 'vue'
import Vuex from 'vuex'
import {getRequest, postRequest} from '@/utils/api'
import {getAuthorizationHeader} from '@/utils/auth'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import {Notification} from 'element-ui'

Vue.use(Vuex)
let connectionPromise = null
let reconnectTimer = null
let generation = 0
let retryCount = 0

function mergeMessages(state, peer, messages) {
  if (!state.currentAdmin) return
  const key = state.currentAdmin.username + '#' + peer
  const combined = new Map((state.sessions[key] || []).map(item => [item.id, item]))
  messages.forEach(message => combined.set(message.id, {
    ...message, self: message.from === state.currentAdmin.username
  }))
  Vue.set(state.sessions, key, Array.from(combined.values()).sort((a, b) => a.id - b.id))
}

function tokenAvailable() {
  const header = getAuthorizationHeader()
  if (!header) return false
  try {
    const token = header.trim().split(/\s+/).pop()
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.exp * 1000 > Date.now()
  } catch (_) { return false }
}

const store = new Vuex.Store({
  state: {
    routes: [], sessions: {}, admins: [],
    currentAdmin: JSON.parse(sessionStorage.getItem('user') || 'null'),
    currentSession: null, filterKey: '', stomp: null, idDot: {},
    chatConnection: 'disconnected', historyLoading: {}, historyHasMore: {}
  },
  mutations: {
    INIT_ADMIN(state, admin) {
      if ((state.currentAdmin || {}).username !== (admin || {}).username) {
        state.sessions = {}; state.idDot = {}; state.currentSession = null; state.admins = []
        state.historyLoading = {}; state.historyHasMore = {}
      }
      state.currentAdmin = admin
    },
    initRoutes(state, data) { state.routes = data },
    changecurrentSession(state, session) { state.currentSession = session },
    INIT_ADMINS(state, data) { state.admins = data },
    CHAT_CONNECTION(state, status) { state.chatConnection = status },
    CHAT_CLIENT(state, client) { state.stomp = client },
    CHAT_MESSAGES(state, {peer, messages}) { mergeMessages(state, peer, messages) },
    CHAT_UNREAD(state, unread) {
      const prefix = state.currentAdmin.username + '#'
      state.idDot = Object.keys(unread).reduce((result, peer) => {
        result[prefix + peer] = unread[peer]; return result
      }, {})
    },
    CHAT_READ(state, peer) { Vue.set(state.idDot, state.currentAdmin.username + '#' + peer, 0) },
    HISTORY_LOADING(state, {peer, loading}) { Vue.set(state.historyLoading, peer, loading) },
    HISTORY_MORE(state, {peer, more}) { Vue.set(state.historyHasMore, peer, more) }
  },
  actions: {
    connect(context) {
      if (context.state.stomp && context.state.stomp.connected) return Promise.resolve(context.state.stomp)
      if (connectionPromise) return connectionPromise
      if (!context.state.currentAdmin || !tokenAvailable()) return Promise.reject(new Error('请先登录'))
      clearTimeout(reconnectTimer)
      const currentGeneration = ++generation
      const client = Stomp.over(new SockJS('/ws/ep'))
      client.debug = () => {}
      client.heartbeat.outgoing = 10000
      client.heartbeat.incoming = 10000
      context.commit('CHAT_CLIENT', client)
      context.commit('CHAT_CONNECTION', retryCount ? 'reconnecting' : 'connecting')
      connectionPromise = new Promise((resolve, reject) => {
        client.connect({'Auth-Token': getAuthorizationHeader()}, () => {
          if (generation !== currentGeneration) { client.disconnect(); reject(new Error('连接已取消')); return }
          retryCount = 0; connectionPromise = null
          context.commit('CHAT_CONNECTION', 'connected')
          client.subscribe('/user/queue/chat', message => {
            if (generation !== currentGeneration || !context.state.currentAdmin) return
            const received = JSON.parse(message.body)
            const self = received.from === context.state.currentAdmin.username
            const peer = self ? received.to : received.from
            context.commit('CHAT_MESSAGES', {peer, messages: [received]})
            if (!self) {
              const current = context.state.currentSession
              if (current && current.username === peer && document.visibilityState === 'visible') {
                context.dispatch('markRead', peer)
              } else {
                const key = context.state.currentAdmin.username + '#' + peer
                Vue.set(context.state.idDot, key, (context.state.idDot[key] || 0) + 1)
                Notification.info({title: (received.fromNickName || peer) + ' 发来消息',
                  message: received.content.slice(0, 40), position: 'bottom-right'})
              }
            }
          })
          client.subscribe('/user/queue/errors', message => Notification.error({
            title: '消息未发送', message: JSON.parse(message.body).message
          }))
          context.dispatch('refreshUnread')
          if (context.state.currentSession) context.dispatch('loadHistory', {peer: context.state.currentSession.username})
          resolve(client)
        }, () => {
          if (generation !== currentGeneration) { reject(new Error('连接已取消')); return }
          connectionPromise = null
          context.commit('CHAT_CONNECTION', 'reconnecting')
          if (tokenAvailable() && context.state.currentAdmin) {
            const delay = Math.min(30000, 2000 * Math.pow(2, retryCount++))
            reconnectTimer = setTimeout(() => context.dispatch('connect').catch(() => {}), delay)
          } else { context.commit('CHAT_CONNECTION', 'disconnected') }
          reject(new Error('聊天连接已断开'))
        })
      })
      return connectionPromise
    },
    disconnect(context) {
      generation++; clearTimeout(reconnectTimer); reconnectTimer = null; retryCount = 0; connectionPromise = null
      const client = context.state.stomp
      if (client) {
        if (client.connected) client.disconnect()
        else if (client.ws) client.ws.close()
      }
      context.commit('CHAT_CLIENT', null); context.commit('CHAT_CONNECTION', 'disconnected')
      context.commit('changecurrentSession', null)
    },
    async initData(context) {
      const username = context.state.currentAdmin && context.state.currentAdmin.username
      const admins = await getRequest('/chat/admin')
      if (!context.state.currentAdmin || context.state.currentAdmin.username !== username) return
      if (Array.isArray(admins)) context.commit('INIT_ADMINS', admins)
      await context.dispatch('refreshUnread')
      return context.dispatch('connect').catch(() => {})
    },
    async refreshUnread(context) {
      const username = context.state.currentAdmin && context.state.currentAdmin.username
      const unread = await getRequest('/chat/unread')
      if (unread && context.state.currentAdmin && context.state.currentAdmin.username === username) context.commit('CHAT_UNREAD', unread)
    },
    async selectSession(context, peer) {
      context.commit('changecurrentSession', peer)
      const loaded = await context.dispatch('loadHistory', {peer: peer.username})
      if (loaded && context.state.currentSession && context.state.currentSession.username === peer.username) {
        await context.dispatch('markRead', peer.username)
      }
    },
    async loadHistory(context, {peer, older = false}) {
      if (context.state.historyLoading[peer]) return
      const username = context.state.currentAdmin.username
      const key = context.state.currentAdmin.username + '#' + peer
      const messages = context.state.sessions[key] || []
      let url = '/chat/history?with=' + encodeURIComponent(peer) + '&size=50'
      if (older && messages.length) url += '&beforeId=' + messages[0].id
      context.commit('HISTORY_LOADING', {peer, loading: true})
      try {
        const history = await getRequest(url)
        if (Array.isArray(history) && context.state.currentAdmin && context.state.currentAdmin.username === username) {
          context.commit('CHAT_MESSAGES', {peer, messages: history})
          context.commit('HISTORY_MORE', {peer, more: history.length === 50})
          return true
        }
      } finally { context.commit('HISTORY_LOADING', {peer, loading: false}) }
    },
    async markRead(context, peer) {
      const username = context.state.currentAdmin && context.state.currentAdmin.username
      const response = await postRequest('/chat/read?with=' + encodeURIComponent(peer))
      if (response && response.success && context.state.currentAdmin && context.state.currentAdmin.username === username) context.commit('CHAT_READ', peer)
    },
    sendMessage(context, content) {
      const client = context.state.stomp
      if (!context.state.currentSession) return Promise.reject(new Error('请先选择联系人'))
      if (!client || !client.connected) return Promise.reject(new Error('聊天尚未连接，请稍后重试'))
      if (!content.trim() || content.length > 2000) return Promise.reject(new Error('消息不能为空，且不能超过 2000 字'))
      client.send('/ws/chat', {}, JSON.stringify({to: context.state.currentSession.username, content}))
      return Promise.resolve()
    }
  }
})
export default store
