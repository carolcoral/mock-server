<!--
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
-->

<template>
  <div class="monaco-editor-wrapper" :style="{ height: editorHeight }">
    <div ref="editorContainer" class="monaco-editor-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'java'
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '400px'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorContainer = ref(null)
const editorHeight = ref(props.height)
let editor = null

const initEditor = () => {
  if (!editorContainer.value) return

  // Define a custom dark theme that looks good for code display
  monaco.editor.defineTheme('custom-java-theme', {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0033B3', fontStyle: 'bold' },
      { token: 'type', foreground: '0033B3' },
      { token: 'string.java', foreground: '067D17' },
      { token: 'comment', foreground: '8C8C8C', fontStyle: 'italic' },
      { token: 'annotation', foreground: '9B5C2E' },
      { token: 'number', foreground: '1750EB' },
      { token: 'delimiter', foreground: '000000' }
    ],
    colors: {
      'editor.background': '#fafafa',
      'editor.lineHighlightBackground': '#f0f0f0',
      'editorLineNumber.foreground': '#999999',
      'editorLineNumber.activeForeground': '#409EFF'
    }
  })

  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: props.language,
    theme: 'custom-java-theme',
    readOnly: props.readOnly,
    minimap: { enabled: false },
    lineNumbers: 'on',
    scrollBeyondLastLine: false,
    wordWrap: 'off',
    automaticLayout: true,
    fontSize: 13,
    lineHeight: 20,
    tabSize: 4,
    insertSpaces: true,
    folding: true,
    foldingStrategy: 'indentation',
    renderLineHighlight: 'line',
    cursorBlinking: 'smooth',
    smoothScrolling: true,
    padding: { top: 8, bottom: 8 },
    scrollbar: {
      verticalScrollbarSize: 8,
      horizontalScrollbarSize: 8
    },
    overviewRulerLanes: 0,
    hideCursorInOverviewRuler: true,
    overviewRulerBorder: false
  })

  // Listen for content changes
  editor.onDidChangeModelContent(() => {
    const value = editor.getValue()
    emit('update:modelValue', value)
  })
}

onMounted(() => {
  nextTick(() => {
    initEditor()
  })
})

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
})

// Watch for external modelValue changes
watch(() => props.modelValue, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue)
  }
})

// Watch for readOnly changes
watch(() => props.readOnly, (newValue) => {
  if (editor) {
    editor.updateOptions({ readOnly: newValue })
  }
})

// Expose editor instance for parent access
defineExpose({
  getEditor: () => editor
})
</script>

<style scoped>
.monaco-editor-wrapper {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.monaco-editor-container {
  width: 100%;
  height: 100%;
}
</style>
