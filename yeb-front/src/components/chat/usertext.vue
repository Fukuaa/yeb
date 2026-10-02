<template>
  <div class="chatComposer">
    <textarea v-model="content" :disabled="!currentSession" maxlength="2000"
              :placeholder="currentSession ? '输入消息，按 Ctrl + Enter 发送' : '请先选择联系人'"
              @keydown.ctrl.enter.prevent="send"></textarea>
    <div class="composerFooter">
      <span>{{ content.length }}/2000</span>
      <el-button size="mini" type="primary" :disabled="!canSend" @click="send">发送</el-button>
    </div>
  </div>
</template>
<script>
import {mapState} from 'vuex'
export default {
  data() { return {content: ''} },
  computed: {
    ...mapState(['currentSession', 'chatConnection']),
    canSend() { return this.currentSession && this.chatConnection === 'connected' && this.content.trim().length > 0 }
  },
  methods: {
    async send() {
      if (!this.canSend) return
      try { await this.$store.dispatch('sendMessage', this.content); this.content = '' }
      catch (error) { this.$message.error(error.message) }
    }
  }
}
</script>
<style scoped>
.chatComposer { position: absolute; bottom: 0; width: 100%; height: 150px; border-top: 1px solid #ddd; background: #fff; }
textarea { width: 100%; height: 108px; padding: 12px; resize: none; border: none; outline: none; box-sizing: border-box; font: inherit; }
.composerFooter { display: flex; justify-content: space-between; align-items: center; padding: 0 12px; color: #8c8c8c; font-size: 12px; }
</style>