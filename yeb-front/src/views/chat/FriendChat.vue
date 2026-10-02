<template>
  <div id="app">
    <div class="sidebar">
      <card></card>
      <list></list>
    </div>
    <div class="main">
      <div class="chatStatus">{{ statusText }}</div>
      <message></message>
      <userText></userText>
    </div>
  </div>
</template>

<script>
import card from '@/components/chat/card.vue'
import list from '@/components/chat/list.vue'
import message from '@/components/chat/message.vue'
import userText from '@/components/chat/usertext.vue'

export default {
  name: 'FriendChat',
  data () {
    return {

    }
  },
  mounted:function() {
    this.$store.dispatch('initData');
    window.addEventListener('focus', this.markRead)
  },
  beforeDestroy() {
    window.removeEventListener('focus', this.markRead)
    this.$store.commit('changecurrentSession', null)
  },
  computed: {
    statusText() {
      return {connected: '聊天已连接', connecting: '正在连接聊天服务…', reconnecting: '连接断开，正在重连…', disconnected: '聊天未连接'}[this.$store.state.chatConnection]
    }
  },
  methods: {
    markRead() {
      const peer = this.$store.state.currentSession
      if (peer) this.$store.dispatch('markRead', peer.username)
    }
  },
  components:{
    card,
    list,
    message,
    userText
  }
}
</script>

<style lang="scss" scoped>
#app {
  margin: 0 auto;
  width: 100%;
  max-width: 920px;
  height: calc(100vh - 210px);
  min-height: 420px;
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid #d9d3c8;
  .sidebar, .main {
    height: 100%;
  }
  .sidebar {
    float: left;
    color: #f4f4f4;
    background-color: #2e3238;
    width: 200px;
  }
  .main {
    position: relative;
    overflow: hidden;
    background-color: #eee;
  }
  .chatStatus { height: 32px; padding: 8px 14px; box-sizing: border-box; font-size: 12px; color: #667085; background: #fff; }
}
</style>
