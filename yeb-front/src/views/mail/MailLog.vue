<template>
  <section class="mailPage">
    <div class="mailHeader">
      <div><h2>邮件发送记录</h2><p>新员工入职后自动发送欢迎邮件；失败任务最多尝试 3 次，可手动重试。</p></div>
      <el-button icon="el-icon-refresh" :loading="loading" @click="load">刷新</el-button>
    </div>
    <div class="sendWelcome">
      <el-select v-model="employeeId" filterable remote clearable placeholder="搜索员工姓名"
                 :remote-method="searchEmployees" :loading="searching" @focus="searchEmployees('')">
        <el-option v-for="employee in employees" :key="employee.id" :value="employee.id"
                   :label="employee.name + ' · ' + (employee.email || '未填写邮箱')" :disabled="!employee.email"/>
      </el-select>
      <el-button type="primary" :disabled="!employeeId" :loading="sending" @click="sendWelcome">发送欢迎邮件</el-button>
    </div>
    <el-table :data="rows" v-loading="loading" empty-text="暂无发送记录">
      <el-table-column prop="employeeName" label="员工" width="100"/>
      <el-table-column prop="email" label="收件邮箱" min-width="180"/>
      <el-table-column label="状态" width="110">
        <template slot-scope="scope"><el-tag :type="statusType(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="count" label="尝试次数" width="90"/>
      <el-table-column label="创建时间" min-width="170"><template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template></el-table-column>
      <el-table-column label="更新时间" min-width="170"><template slot-scope="scope">{{ formatTime(scope.row.updateTime) }}</template></el-table-column>
      <el-table-column label="操作" width="100"><template slot-scope="scope">
        <el-button v-if="scope.row.status === 2" type="text" @click="retry(scope.row)">重新发送</el-button>
      </template></el-table-column>
    </el-table>
    <el-pagination background layout="total, prev, pager, next" :total="total" :current-page.sync="page"
                   :page-size="20" @current-change="load"/>
  </section>
</template>
<script>
export default {
  data() { return {rows: [], total: 0, page: 1, loading: false, employees: [], employeeId: null, searching: false, sending: false, timer: null, searchGeneration: 0} },
  mounted() { this.load(); this.timer = setInterval(this.load, 10000) },
  beforeDestroy() { clearInterval(this.timer) },
  methods: {
    async load() {
      if (this.loading) return
      this.loading = true
      try { const result = await this.getRequest('/mail-log?page=' + this.page + '&size=20'); if (result) { this.rows = result.data; this.total = result.total } }
      finally { this.loading = false }
    },
    async searchEmployees(name) {
      const current = ++this.searchGeneration
      this.searching = true
      try {
        const result = await this.getRequest('/employee/basic/?currentPage=1&size=20&name=' + encodeURIComponent(name))
        if (current === this.searchGeneration && result) this.employees = result.data || []
      } finally { if (current === this.searchGeneration) this.searching = false }
    },
    async sendWelcome() {
      const employee = this.employees.find(item => item.id === this.employeeId)
      if (!employee) return
      try { await this.$confirm('向 ' + employee.email + ' 发送入职欢迎邮件？', '发送邮件', {type: 'warning'}) }
      catch (_) { return }
      this.sending = true
      try { const result = await this.postRequest('/mail-log/welcome/' + this.employeeId); if (result) await this.load() }
      finally { this.sending = false }
    },
    async retry(row) {
      try { await this.$confirm('重新向 ' + row.email + ' 发送欢迎邮件？', '重新发送', {type: 'warning'}) }
      catch (_) { return }
      const result = await this.postRequest('/mail-log/' + encodeURIComponent(row.msgId) + '/retry')
      if (result) await this.load()
    },
    statusName(status) { return {0: '待发送', 1: '已发送', 2: '发送失败', 3: '发送中'}[status] || '未知' },
    statusType(status) { return {0: 'info', 1: 'success', 2: 'danger', 3: 'warning'}[status] || 'info' },
    formatTime(value) { return value ? new Date(value).toLocaleString() : '' }
  }
}
</script>
<style scoped>
.mailPage { background: #fff; padding: 24px; border-radius: 12px; }
.mailHeader { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
h2 { margin: 0 0 8px; } p { color: #667085; margin: 0 0 20px; }
.sendWelcome { display: flex; gap: 12px; margin-bottom: 20px; }
.sendWelcome .el-select { width: 350px; max-width: 65%; }
.el-pagination { text-align: right; margin-top: 20px; }
</style>
