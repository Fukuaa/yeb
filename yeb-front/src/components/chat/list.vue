<template>
  <div class="contactList">
    <button v-for="item in contacts" :key="item.username" type="button"
            :class="{active: currentSession && item.username === currentSession.username}" @click="select(item)">
      <el-badge :value="unread(item)" :hidden="!unread(item)" :max="99">
        <el-avatar :size="30" :src="item.userFace">{{ (item.name || item.username).slice(0, 1) }}</el-avatar>
      </el-badge>
      <span>{{ item.name || item.username }}</span>
    </button>
    <p v-if="!contacts.length" class="empty">没有匹配的联系人</p>
  </div>
</template>
<script>
import {mapState} from 'vuex'
export default {
  computed: {
    ...mapState(['admins', 'currentAdmin', 'currentSession', 'filterKey', 'idDot']),
    contacts() {
      const filter = this.filterKey.trim().toLowerCase()
      return this.admins.filter(item => (item.name + ' ' + item.username).toLowerCase().includes(filter))
    }
  },
  methods: {
    select(contact) { this.$store.dispatch('selectSession', contact) },
    unread(contact) { return this.currentAdmin ? this.idDot[this.currentAdmin.username + '#' + contact.username] || 0 : 0 }
  }
}
</script>
<style scoped>
.contactList { overflow-y: auto; max-height: calc(100% - 140px); }
button { display: flex; align-items: center; gap: 12px; width: 100%; padding: 14px; color: inherit; background: transparent; border: 0; border-bottom: 1px solid #292c33; text-align: left; cursor: pointer; }
button:hover, button.active { background: rgba(255,255,255,.12); }
.empty { padding: 12px; color: #aaa; font-size: 12px; }
</style>