<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoadNodes, planRoute } from '../api/travel'

const roadNodes = ref([])
const form = ref({
  fromNodeId: null,
  toNodeId: null,
  targetNodeIds: [],
  strategy: 'time',
  transport: 'walk',
})
const loading = ref(false)
const result = ref(null)
const isMultiTarget = ref(false)

const availableTargets = computed(() => roadNodes.value.filter((node) => node.id !== form.value.fromNodeId))
const nodeNameMap = computed(() =>
  Object.fromEntries(roadNodes.value.map((node) => [node.id, node.name]))
)
const visitOrderText = computed(() => {
  if (!result.value?.visitOrderNodeIds?.length) {
    return '无'
  }
  const visitNames = result.value.visitOrderNodeIds.map((id) => nodeNameMap.value[id] || `节点${id}`)
  if (isMultiTarget.value) {
    visitNames.push(nodeNameMap.value[form.value.fromNodeId] || `节点${form.value.fromNodeId}`)
  }
  return visitNames.join(' -> ')
})
const pathText = computed(() => {
  if (!result.value?.pathNodeIds?.length) {
    return '无'
  }
  return result.value.pathNodeIds
    .map((id) => nodeNameMap.value[id] || `节点${id}`)
    .join(' -> ')
})

const loadRoadNodes = async () => {
  const { data } = await listRoadNodes()
  roadNodes.value = data
  if (!form.value.fromNodeId && data.length) {
    form.value.fromNodeId = data[0].id
  }
  if (!form.value.toNodeId && data.length > 1) {
    form.value.toNodeId = data[1].id
  }
}

const resetTargetSelection = () => {
  form.value.targetNodeIds = []
  result.value = null
}

const submit = async () => {
  if (!form.value.fromNodeId) {
    ElMessage.warning('请先选择起点节点')
    return
  }

  if (isMultiTarget.value && !form.value.targetNodeIds.length) {
    ElMessage.warning('多目标规划至少选择一个目标节点')
    return
  }

  if (!isMultiTarget.value && !form.value.toNodeId) {
    ElMessage.warning('请选择终点节点')
    return
  }

  loading.value = true
  result.value = null
  try {
    const payload = {
      fromNodeId: form.value.fromNodeId,
      strategy: form.value.strategy,
      transport: form.value.transport,
    }
    if (isMultiTarget.value) {
      payload.targetNodeIds = form.value.targetNodeIds
    } else {
      payload.toNodeId = form.value.toNodeId
    }

    const { data } = await planRoute(payload)
    result.value = data
    ElMessage.success('路线规划完成')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '规划失败，请检查节点参数')
  } finally {
    loading.value = false
  }
}

onMounted(loadRoadNodes)
</script>

<template>
  <section class="route-page">
    <el-card class="module-card route-card">
      <div class="module-header">
        <div>
          <h2>路线规划</h2>
          <p class="module-subtitle">支持单目标最短路径与多目标访问后返回起点的最近邻启发式规划。</p>
        </div>
      </div>

      <el-form :model="form" label-width="120px" class="route-form">
        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="多目标模式会按最近邻启发式选择下一个访问点，并在全部访问后自动返回起点。"
          class="mode-alert"
        />

        <el-form-item label="规划模式">
          <el-switch
            v-model="isMultiTarget"
            active-text="多目标往返"
            inactive-text="单目标"
            @change="resetTargetSelection"
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :md="12" :xs="24">
            <el-form-item label="起点节点">
              <el-select v-model="form.fromNodeId" class="full-width" placeholder="请选择起点">
                <el-option
                  v-for="node in roadNodes"
                  :key="node.id"
                  :label="`${node.name}（ID: ${node.id}）`"
                  :value="node.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!isMultiTarget" :md="12" :xs="24">
            <el-form-item label="终点节点">
              <el-select v-model="form.toNodeId" class="full-width" placeholder="请选择终点">
                <el-option
                  v-for="node in availableTargets"
                  :key="node.id"
                  :label="`${node.name}（ID: ${node.id}）`"
                  :value="node.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-else :md="12" :xs="24">
            <el-form-item label="目标节点集合">
              <el-select
                v-model="form.targetNodeIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                class="full-width"
                placeholder="请选择多个目标节点"
              >
                <el-option
                  v-for="node in availableTargets"
                  :key="node.id"
                  :label="`${node.name}（ID: ${node.id}）`"
                  :value="node.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :md="12" :xs="24">
            <el-form-item label="规划策略">
              <el-select v-model="form.strategy" class="full-width">
                <el-option label="最短距离" value="distance" />
                <el-option label="最短时间" value="time" />
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
        <el-button type="primary" :loading="loading" @click="submit">生成路线</el-button>
      </el-form>

      <el-divider />

      <el-empty v-if="!result" description="尚未生成路线，请先填写参数并点击按钮" />
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item v-if="result.visitOrderNodeIds?.length" label="访问顺序">
          {{ visitOrderText }}
          <span v-if="isMultiTarget" class="hint-text">（最后自动返回起点）</span>
        </el-descriptions-item>
        <el-descriptions-item label="路径节点">
          {{ pathText }}
          <div class="hint-text">该路径会展示真实经过的中间节点，因此可能包含重复节点。</div>
        </el-descriptions-item>
        <el-descriptions-item label="总距离（米）">
          {{ Math.round(result.totalDistanceMeters || 0) }}
        </el-descriptions-item>
        <el-descriptions-item label="预计耗时（分钟）">
          {{ Number(result.totalTravelMinutes || 0).toFixed(2) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </section>
</template>

<style scoped>
.full-width {
  width: 100%;
}

.route-form {
  max-width: 900px;
}

.mode-alert {
  margin-bottom: 16px;
}

.hint-text {
  margin-top: 6px;
  color: #6a6a6a;
  font-size: 12px;
}
</style>
