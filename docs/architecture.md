# 系统架构说明

- 前端：Vue3 + Vite + Vue Router + Pinia + Axios
- 后端：Java17 + Spring Boot + Spring Data JPA + Spring Data Elasticsearch
- 数据层：MySQL（业务主数据）+ Elasticsearch（日记全文检索）

## 模块

1. 目的地推荐模块（Top-K）
2. 路线规划模块（有向图 + Dijkstra）
3. 设施/美食查询模块
4. 日记管理与交流模块
5. 行程协作模块

