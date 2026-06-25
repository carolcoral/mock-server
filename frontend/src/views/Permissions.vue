<!--
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
-->

<template>
  <div class="permissions">
    <div class="page-header">
      <h1>{{ $t('permission.permission.title') }}</h1>
    </div>

    <!-- 角色选择 -->
    <el-card class="select-card">
      <div class="select-row">
        <span class="select-label">{{ $t('permission.permission.selectRole') }}：</span>
        <el-select
          v-model="selectedRoleId"
          :placeholder="$t('permission.permission.selectRolePlaceholder')"
          style="width: 300px"
          @change="handleRoleChange"
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="role.name + ' (' + role.code + ')'"
            :value="role.id"
          />
        </el-select>
        <span v-if="!selectedRoleId" class="select-hint">{{ $t('permission.permission.selectRoleHint') }}</span>
        <span v-if="selectedRoleId" class="perms-count">{{ $t('permission.permission.permissionsCount', { count: checkedPermissionIds.length }) }}</span>
      </div>
    </el-card>

    <!-- 权限分配 -->
    <el-card v-if="selectedRoleId" class="perms-card">
      <template v-if="loading">
        <el-skeleton :rows="5" animated />
      </template>
      <template v-else>
        <div v-for="group in permissionGroups" :key="group.groupName" class="perm-group">
          <div class="perm-group-header">
            <el-checkbox
              v-model="group.checkedAll"
              :indeterminate="group.indeterminate"
              @change="(val) => handleGroupCheckAll(group, val)"
            >
              <strong>{{ group.groupName }}</strong>
            </el-checkbox>
          </div>
          <div class="perm-items">
            <div v-for="perm in group.permissions" :key="perm.id" class="perm-item">
              <el-checkbox
                :model-value="checkedPermissionIds.includes(perm.id)"
                @change="(val) => handlePermCheck(perm.id, val)"
              >
                <span class="perm-name">{{ perm.name }}</span>
                <el-tag :type="perm.type === 'PAGE' ? '' : 'info'" size="small" class="perm-type-tag">
                  {{ perm.type === 'PAGE' ? $t('permission.permission.pageAccess') : $t('permission.permission.buttonOperation') }}
                </el-tag>
              </el-checkbox>
            </div>
          </div>
        </div>

        <div class="perms-footer">
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ $t('permission.permission.savePermissions') }}
          </el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()

const roleList = ref([])
const selectedRoleId = ref(null)
const permissionGroups = ref([])
const checkedPermissionIds = ref([])
const loading = ref(false)
const saving = ref(false)

const fetchRoles = async () => {
  try {
    const response = await request.get('/roles')
    if (response.code === 200) {
      roleList.value = response.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const fetchPermissions = async () => {
  loading.value = true
  try {
    const response = await request.get('/permissions')
    if (response.code === 200) {
      permissionGroups.value = (response.data || []).map(group => ({
        ...group,
        checkedAll: false,
        indeterminate: false
      }))
    } else {
      ElMessage.error(t('permission.permission.fetchFailed'))
    }
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error(t('permission.permission.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const fetchRolePermissions = async (roleId) => {
  try {
    const response = await request.get(`/permissions/role/${roleId}`)
    if (response.code === 200) {
      checkedPermissionIds.value = response.data || []
      updateGroupCheckStates()
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
  }
}

const updateGroupCheckStates = () => {
  permissionGroups.value.forEach(group => {
    const permIds = group.permissions.map(p => p.id)
    const checkedCount = permIds.filter(id => checkedPermissionIds.value.includes(id)).length
    group.checkedAll = checkedCount === permIds.length && permIds.length > 0
    group.indeterminate = checkedCount > 0 && checkedCount < permIds.length
  })
}

const handleRoleChange = (roleId) => {
  if (roleId) {
    fetchRolePermissions(roleId)
  } else {
    checkedPermissionIds.value = []
    updateGroupCheckStates()
  }
}

const handleGroupCheckAll = (group, checked) => {
  const permIds = group.permissions.map(p => p.id)
  if (checked) {
    // 全选：添加该组所有权限ID（去重）
    const newIds = permIds.filter(id => !checkedPermissionIds.value.includes(id))
    checkedPermissionIds.value = [...checkedPermissionIds.value, ...newIds]
  } else {
    // 取消全选：移除该组所有权限ID
    checkedPermissionIds.value = checkedPermissionIds.value.filter(id => !permIds.includes(id))
  }
  updateGroupCheckStates()
}

const handlePermCheck = (permId, checked) => {
  if (checked) {
    if (!checkedPermissionIds.value.includes(permId)) {
      checkedPermissionIds.value = [...checkedPermissionIds.value, permId]
    }
  } else {
    checkedPermissionIds.value = checkedPermissionIds.value.filter(id => id !== permId)
  }
  updateGroupCheckStates()
}

const handleSave = async () => {
  if (!selectedRoleId.value) return
  saving.value = true
  try {
    const response = await request.put(`/permissions/role/${selectedRoleId.value}`, checkedPermissionIds.value)
    if (response.code === 200) {
      ElMessage.success(t('permission.permission.saveSuccess'))
    } else {
      ElMessage.error(response.message || t('permission.permission.saveFailed'))
    }
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error(t('permission.permission.saveFailed'))
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchRoles()
  fetchPermissions()
})
</script>

<style scoped>
.permissions {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.select-card {
  margin-bottom: 20px;
}

.select-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.select-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.select-hint {
  font-size: 13px;
  color: #909399;
}

.perms-count {
  font-size: 13px;
  color: #409EFF;
  font-weight: 500;
}

.perms-card {
  margin-bottom: 20px;
}

.perm-group {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.perm-group:last-child {
  border-bottom: none;
}

.perm-group-header {
  margin-bottom: 12px;
}

.perm-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  padding-left: 24px;
}

.perm-item {
  min-width: 220px;
}

.perm-name {
  margin-right: 6px;
}

.perm-type-tag {
  vertical-align: middle;
}

.perms-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}
</style>
