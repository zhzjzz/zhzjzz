<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoadGraph, listRoadNodes, planRoute } from '../api/travel'

const roadNodes = ref([])
const graphEdges = ref([])
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
const graphLayout = computed(() => {
  if (!roadNodes.value.length) {
    return { nodes: [], edges: [] }
  }

  const width = 640
  const height = 320
  const padding = 36
  const longitudes = roadNodes.value.map((node) => node.longitude ?? node.id)
  const latitudes = roadNodes.value.map((node) => node.latitude ?? node.id)
  const minLng = Math.min(...longitudes)
  const maxLng = Math.max(...longitudes)
  const minLat = Math.min(...latitudes)
  const maxLat = Math.max(...latitudes)
  const lngSpan = Math.max(maxLng - minLng, 0.0001)
  const latSpan = Math.max(maxLat - minLat, 0.0001)

  const nodes = roadNodes.value.map((node) => {
    const x = padding + (((node.longitude ?? node.id) - minLng) / lngSpan) * (width - padding * 2)
    const y = padding + (1 - (((node.latitude ?? node.id) - minLat) / latSpan)) * (height - padding * 2)
    return {
      ...node,
      x,
      y,
      isPathNode: result.value?.pathNodeIds?.includes(node.id) || false,
      isStart: form.value.fromNodeId === node.id,
      isEnd: !isMultiTarget.value && form.value.toNodeId === node.id,
      isTarget: isMultiTarget.value && form.value.targetNodeIds.includes(node.id),
    }
  })

  const nodeMap = Object.fromEntries(nodes.map((node) => [node.id, node]))
  const highlightedEdges = new Set()
  const currentPath = result.value?.pathNodeIds || []
  for (let i = 0; i + 1 < currentPath.length; i++) {
    highlightedEdges.add(`${currentPath[i]}-${currentPath[i + 1]}`)
  }

  const edges = graphEdges.value
    .map((edge) => {
      const fromNode = nodeMap[edge.fromNodeId]
      const toNode = nodeMap[edge.toNodeId]
      if (!fromNode || !toNode) {
        return null
      }
      return {
        ...edge,
        fromX: fromNode.x,
        fromY: fromNode.y,
        toX: toNode.x,
        toY: toNode.y,
        isHighlighted: highlightedEdges.has(`${edge.fromNodeId}-${edge.toNodeId}`),
      }
    })
    .filter(Boolean)

  return { nodes, edges, width, height }
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

const loadRoadGraph = async () => {
  const { data } = await getRoadGraph()
  if (data?.nodes?.length) {
    roadNodes.value = data.nodes
  }
  graphEdges.value = data?.edges || []
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
onMounted(loadRoadGraph)
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

      <el-divider />

      <section class="graph-panel">
        <div class="graph-header">
          <h3>路线图示意</h3>
          <p>灰线表示道路图，红线表示当前规划路径，圆点表示道路节点。</p>
        </div>
        <div class="graph-legend">
          <span><i class="legend-dot start"></i>起点</span>
          <span><i class="legend-dot target"></i>目标点</span>
          <span><i class="legend-dot path"></i>路径节点</span>
        </div>
        <svg
          v-if="graphLayout.nodes.length"
          class="route-graph"
          :viewBox="`0 0 ${graphLayout.width} ${graphLayout.height}`"
          role="img"
          aria-label="路线图示意"
        >
          <line
            v-for="edge in graphLayout.edges"
            :key="edge.id"
            :x1="edge.fromX"
            :y1="edge.fromY"
            :x2="edge.toX"
            :y2="edge.toY"
            :class="['graph-edge', { highlighted: edge.isHighlighted }]"
          />
          <g v-for="node in graphLayout.nodes" :key="node.id">
            <circle
              :cx="node.x"
              :cy="node.y"
              :r="node.isStart || node.isTarget || node.isEnd ? 11 : 9"
              :class="[
                'graph-node',
                {
                  start: node.isStart,
                  target: node.isTarget || node.isEnd,
                  path: node.isPathNode,
                },
              ]"
            />
            <text :x="node.x" :y="node.y - 16" class="graph-label">
              {{ node.name }}
            </text>
          </g>
        </svg>
        <el-empty v-else description="暂无道路图数据" />
      </section>
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

.graph-panel {
  display: grid;
  gap: 14px;
}

.graph-header h3 {
  font-size: 18px;
  color: #222222;
}

.graph-header p {
  margin-top: 4px;
  color: #6a6a6a;
  font-size: 13px;
}

.graph-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #6a6a6a;
  font-size: 13px;
}

.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
  background: #bdbdbd;
}

.legend-dot.start {
  background: #ff385c;
}

.legend-dot.target {
  background: #222222;
}

.legend-dot.path {
  background: #ffb07b;
}

.route-graph {
  width: 100%;
  min-height: 320px;
  border-radius: 24px;
  background: linear-gradient(180deg, #ffffff, #faf7f3);
  box-shadow: rgba(0, 0, 0, 0.02) 0px 0px 0px 1px,
    rgba(0, 0, 0, 0.04) 0px 2px 6px,
    rgba(0, 0, 0, 0.1) 0px 4px 8px;
}

.graph-edge {
  stroke: #d7d7d7;
  stroke-width: 4;
  stroke-linecap: round;
}

.graph-edge.highlighted {
  stroke: #ff385c;
  stroke-width: 6;
}

.graph-node {
  fill: #ffffff;
  stroke: #6a6a6a;
  stroke-width: 2;
}

.graph-node.path {
  fill: #ffd8bf;
  stroke: #ff8a3d;
}

.graph-node.start {
  fill: #ff385c;
  stroke: #d81d47;
}

.graph-node.target {
  fill: #222222;
  stroke: #222222;
}

.graph-label {
  font-size: 12px;
  fill: #222222;
  text-anchor: middle;
  font-weight: 600;
}
</style>
