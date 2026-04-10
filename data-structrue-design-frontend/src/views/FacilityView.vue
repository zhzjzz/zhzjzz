<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoadNodes, searchNearbyFacilities } from '../api/travel'

const loading = ref(false)
const roadNodes = ref([])
const results = ref([])

const form = ref({
  fromNodeId: null,
  type: '',
  keyword: '',
  maxDistanceMeters: 1000,
  transport: 'walk',
})

const facilityTypeOptions = ['洗手间', '食堂', '超市', '咖啡馆', '图书馆', '商店']

const selectedNodeName = computed(() => {
  const node = roadNodes.value.find((item) => item.id === form.value.fromNodeId)
  return node?.name || '未选择'
})

const loadRoadNodes = async () => {
  const { data } = await listRoadNodes()
  roadNodes.value = data
  if (!form.value.fromNodeId && data.length) {
    form.value.fromNodeId = data[0].id
  }
}

const search = async () => {
  if (!form.value.fromNodeId) {
    ElMessage.warning('请先选择当前位置节点')
    return
  }

  loading.value = true
  try {
    const params = {
      fromNodeId: form.value.fromNodeId,
      transport: form.value.transport,
    }
    if (form.value.type) params.type = form.value.type
    if (form.value.keyword.trim()) params.keyword = form.value.keyword.trim()
    if (form.value.maxDistanceMeters) params.maxDistanceMeters = form.value.maxDistanceMeters

    const { data } = await searchNearbyFacilities(params)
    results.value = data
    ElMessage.success(`已找到 ${data.length} 个附近场所`)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadRoadNodes()
  if (form.value.fromNodeId) {
    await search()
  }
})
</script>

<template>
  <section class="facility-page">
    <el-card class="module-card facility-card">
      <div class="module-header">
        <div>
          <h2>场所查询</h2>
          <p class="module-subtitle">从当前位置出发，按路径距离查找附近服务设施，并支持类别过滤与关键字检索。</p>
        </div>
      </div>

      <el-form :model="form" label-width="120px">
        <el-row :gutter="12">
          <el-col :md="12" :xs="24">
            <el-form-item label="当前位置节点">
              <el-select v-model="form.fromNodeId" class="full-width" placeholder="请选择起点节点">
                <el-option
                  v-for="node in roadNodes"
                  :key="node.id"
                  :label="`${node.name}（ID: ${node.id}）`"
                  :value="node.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="交通工具">
              <el-select v-model="form.transport" class="full-width">
                <el-option label="步行" value="walk" />
                <el-option label="自行车" value="bike" />
                <el-option label="电瓶车" value="shuttle" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :md="8" :xs="24">
            <el-form-item label="设施类别">
              <el-select v-model="form.type" class="full-width" clearable placeholder="全部类别">
                <el-option
                  v-for="item in facilityTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="关键字">
              <el-input v-model="form.keyword" placeholder="名称/类别/所属地点" clearable />
            </el-form-item>
          </el-col>
          <el-col :md="8" :xs="24">
            <el-form-item label="最大距离（米）">
              <el-input-number v-model="form.maxDistanceMeters" :min="100" :step="100" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="toolbar">
          <el-tag type="info" effect="plain">当前起点：{{ selectedNodeName }}</el-tag>
          <div class="toolbar-actions">
            <el-button @click="loadRoadNodes">刷新节点</el-button>
            <el-button type="primary" :loading="loading" @click="search">查询附近场所</el-button>
          </div>
        </div>
      </el-form>

      <el-divider />

      <el-empty v-if="!results.length && !loading" description="当前条件下暂无可达设施" />
      <el-table v-else :data="results" border stripe v-loading="loading">
        <el-table-column prop="facility.name" label="场所名称" min-width="180" />
        <el-table-column prop="facility.facilityType" label="类别" width="130" />
        <el-table-column prop="facility.destination.name" label="所属目的地" min-width="160" />
        <el-table-column prop="nearestNodeName" label="映射节点" min-width="140" />
        <el-table-column prop="routeDistanceMeters" label="路径距离（米）" width="150">
          <template #default="{ row }">
            {{ Math.round(row.routeDistanceMeters) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.full-width {
  width: 100%;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}
</style>
