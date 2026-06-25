/*
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

import request from '@/utils/request'

/**
 * 获取项目列表
 * @returns {Promise}
 */
export function getProjectList() {
  return request({
    url: '/projects',
    method: 'get'
  })
}

/**
 * 获取有权限的项目列表（分页，用于表格展示）
 * @returns {Promise}
 */
export function getAccessibleProjects() {
  return request({
    url: '/projects/accessible',
    method: 'get'
  })
}

/**
 * 获取当前用户可访问的全部项目（不分页，用于下拉选择）
 * @returns {Promise}
 */
export function getAllAccessibleProjects() {
  return request({
    url: '/projects/accessible/all',
    method: 'get'
  })
}

/**
 * 根据ID获取项目
 * @param {Number} id 项目ID
 * @returns {Promise}
 */
export function getProjectById(id) {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

/**
 * 根据编码获取项目
 * @param {String} code 项目编码
 * @returns {Promise}
 */
export function getProjectByCode(code) {
  return request({
    url: `/projects/code/${code}`,
    method: 'get'
  })
}

/**
 * 创建项目
 * @param {Object} data 项目数据
 * @returns {Promise}
 */
export function createProject(data) {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

/**
 * 更新项目
 * @param {Object} data 项目数据
 * @returns {Promise}
 */
export function updateProject(data) {
  return request({
    url: '/projects',
    method: 'put',
    data
  })
}

/**
 * 删除项目
 * @param {Number} id 项目ID
 * @returns {Promise}
 */
export function deleteProject(id) {
  return request({
    url: `/projects/${id}`,
    method: 'delete'
  })
}

/**
 * 添加项目成员
 * @param {Number} projectId 项目ID
 * @param {Number} userId 用户ID
 * @returns {Promise}
 */
export function addProjectMember(projectId, userId) {
  return request({
    url: `/projects/${projectId}/members/${userId}`,
    method: 'post'
  })
}

/**
 * 移除项目成员
 * @param {Number} projectId 项目ID
 * @param {Number} userId 用户ID
 * @returns {Promise}
 */
export function removeProjectMember(projectId, userId) {
  return request({
    url: `/projects/${projectId}/members/${userId}`,
    method: 'delete'
  })
}
