<template>
  <div class="chatMessages" ref="messages">
    <div v-if="!currentSession" class="empty">选择联系人开始聊天</div>
    <template v-else>
      <div class="historyAction">
        <el-button v-if="hasMore" type="text" :loading="loading" @click="loadOlder">加载更早的消息</el-button>
        <span v-else-if="loading">正在加载聊天记录…</span>
      </div>
      <div v-if="!entries.length && !loading" class="empty">暂无聊天记录</div>
      <div v-for="entry in entries" :key="entry.id" class="messageRow" :class="{self: entry.self}">
        <p class="messageTime">{{ formatTime(entry.date) }}</p>
        <div class="messageBody">
          <el-avatar :size="32" :src="entry.self ? currentAdmin.userFace : currentSession.userFace">
            {{ (entry.self ? currentAdmin.name : currentSession.name || '?').slice(0,1) }}
          </el-avatar>
          <p class="messageText">{{ entry.content }}</p>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
import {mapState} from 'vuex'
export default {
  computed: {
    ...mapState(['sessions', 'currentAdmin', 'currentSession', 'historyHasMore', 'historyLoading']),
    entries() { return this.currentSession && this.currentAdmin ? this.sessions[this.currentAdmin.username + '#' + this.currentSession.username] || [] : [] },
    hasMore() { return this.currentSession && this.historyHasMore[this.currentSession.username] },
    loading() { return this.currentSession && this.historyLoading[this.currentSession.username] }
  },
  watch: { entries() { if (!this.loadingOlder) this.$nextTick(this.scrollBottom) } },
  data() { return {loadingOlder: false} },
  methods: {
    formatTime(value) { const date = new Date(value); return Number.isNaN(date.getTime()) ? '' : date.toLocaleString() },
    scrollBottom() { const box = this.$refs.messages; if (box) box.scrollTop = box.scrollHeight },
    async loadOlder() {
      const box = this.$refs.messages; const height = box.scrollHeight
      this.loadingOlder = true
      try { await this.$store.dispatch('loadHistory', {peer: this.currentSession.username, older: true}); await this.$nextTick(); box.scrollTop += box.scrollHeight - height }
      finally { this.loadingOlder = false }
    }
  }
}
</script>
<style scoped>
.chatMessages { position: absolute; top: 32px; bottom: 150px; width: 100%; overflow-y: auto; padding: 12px; box-sizing: border-box; }
.empty, .historyAction { text-align: center; color: #8c8c8c; font-size: 12px; padding: 12px; }
.messageTime { text-align: center; color: #8c8c8c; font-size: 12px; }
.messageBody { display: flex; align-items: flex-start; gap: 10px; }
.self .messageBody { flex-direction: row-reverse; }
.messageText { margin: 0; max-width: 75%; padding: 10px 12px; background: #fff; border-radius: 8px; white-space: pre-wrap; overflow-wrap: anywhere; }
.self .messageText { background: #b8e8b6; }
.messageRow { margin-bottom: 14px; }
</style>