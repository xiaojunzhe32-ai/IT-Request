import type { RequestPriority, RequestStatus } from '@/types/requests'

export const requestStatuses: RequestStatus[] = [
  'New',
  'Assigned',
  'In Progress',
  'Testing',
  'Resolved',
  'User Test Failed',
  'Closed'
]

export const requestTypes = [
  'Account Access',
  'Application Issue',
  'Network Issue',
  'Hardware Issue',
  'Data Correction',
  'Other'
]

export const priorityOptions: RequestPriority[] = ['Low', 'Medium', 'High', 'Critical']

export const statusColorMap: Record<RequestStatus, string> = {
  New: '#64748b',
  Assigned: '#2563eb',
  'In Progress': '#000080',
  Testing: '#7c3aed',
  Resolved: '#16a34a',
  'User Test Failed': '#dc2626',
  Closed: '#475569'
}

export const priorityColorMap: Record<RequestPriority, string> = {
  Low: '#64748b',
  Medium: '#2563eb',
  High: '#e11d48',
  Critical: '#dc2626'
}

export const sanitizeRequestHtml = (html: string) => {
  if (!html || typeof document === 'undefined') return ''

  const template = document.createElement('template')
  template.innerHTML = html
  const allowedTags = new Set([
    'A',
    'B',
    'BR',
    'CODE',
    'DIV',
    'EM',
    'I',
    'IMG',
    'LI',
    'OL',
    'P',
    'PRE',
    'SPAN',
    'STRONG',
    'UL'
  ])

  const cleanNode = (node: Node) => {
    Array.from(node.childNodes).forEach((child) => {
      if (child.nodeType === Node.COMMENT_NODE) {
        child.remove()
        return
      }
      if (child.nodeType !== Node.ELEMENT_NODE) return

      const element = child as HTMLElement
      if (!allowedTags.has(element.tagName)) {
        element.replaceWith(document.createTextNode(element.textContent || ''))
        return
      }

      Array.from(element.attributes).forEach((attribute) => {
        const name = attribute.name.toLowerCase()
        const value = attribute.value
        const keepLink = element.tagName === 'A' && name === 'href' && /^(https?:|mailto:)/i.test(value)
        const keepImage = element.tagName === 'IMG' && name === 'src' && /^(https?:|blob:)/i.test(value)
        const keepAlt = element.tagName === 'IMG' && name === 'alt'
        const keepAttachmentRef = element.tagName === 'IMG' && (name === 'data-attachment-id' || name === 'data-local-attachment-id')
        if (!keepLink && !keepImage && !keepAlt && !keepAttachmentRef) element.removeAttribute(attribute.name)
      })
      cleanNode(element)
    })
  }

  cleanNode(template.content)
  return template.innerHTML
}
