<template>
  <section class="pageStub">
    <div class="pageStubMark">{{ mark }}</div>
    <div>
      <p>当前页面</p>
      <h2>{{ title }}</h2>
    </div>
  </section>
</template>

<script>
export default {
  name: "PagePlaceholder",
  props: {
    title: {
      type: String,
      required: true
    }
  },
  computed: {
    mark() {
      return this.title ? this.title.slice(0, 1) : "页"
    }
  }
}
</script>

<style scoped>
.pageStub {
  min-height: 220px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 12px 8px 28px;
}

.pageStubMark {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: #1c2430;
  color: #f3d7b0;
  font-size: 26px;
  font-weight: 650;
}

.pageStub p {
  margin: 0 0 6px;
  color: #8a8174;
  letter-spacing: 0.16em;
  font-size: 12px;
}

.pageStub h2 {
  margin: 0;
  color: #1c2430;
  font-size: 28px;
  font-weight: 650;
}
</style>
