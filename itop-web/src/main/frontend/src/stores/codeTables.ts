import { defineStore } from 'pinia'
import { reactive } from 'vue'
import { codeTableApi } from '@/api/system'
import type { CodeTableCode, CodeTableItem } from '@/types/system'

export const useCodeTableStore = defineStore('codeTables', () => {
  const tables = reactive<Record<CodeTableCode, CodeTableItem[]>>({
    REQUEST_TYPE: [],
    AFFECTED_SERVICE: []
  })
  const loaded = reactive<Record<CodeTableCode, boolean>>({
    REQUEST_TYPE: false,
    AFFECTED_SERVICE: false
  })
  const loading = reactive<Record<CodeTableCode, boolean>>({
    REQUEST_TYPE: false,
    AFFECTED_SERVICE: false
  })

  const loadTable = async (tableCode: CodeTableCode, force = false) => {
    if (!force && loaded[tableCode]) return tables[tableCode]

    loading[tableCode] = true
    try {
      const items = await codeTableApi.list(tableCode, 'active')
      tables[tableCode] = items
      loaded[tableCode] = true
      return items
    } finally {
      loading[tableCode] = false
    }
  }

  const ensureTables = async (...codes: CodeTableCode[]) => {
    await Promise.all(codes.map((code) => loadTable(code)))
  }

  const itemsFor = (tableCode: CodeTableCode) => tables[tableCode]

  const labelFor = (tableCode: CodeTableCode, value?: string, fallback = '') => {
    if (!value) return fallback
    const normalized = value.toUpperCase()
    const matched = tables[tableCode].find((item) => item.code.toUpperCase() === normalized)
    return matched?.name || fallback || value
  }

  return {
    tables,
    loaded,
    loading,
    loadTable,
    ensureTables,
    itemsFor,
    labelFor
  }
})
