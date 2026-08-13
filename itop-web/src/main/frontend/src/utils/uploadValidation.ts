const ALLOWED_EXTENSIONS = [
  'png', 'jpg', 'jpeg', 'gif', 'bmp', 'svg',
  'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx',
  'txt', 'log', 'csv', 'md',
  'zip', '7z', 'gz', 'tar',
  'html'
]

const ALLOWED_MIME_PREFIXES = [
  'image/',
  'text/',
  'application/pdf',
  'application/vnd.',
  'application/msword',
  'application/zip',
  'application/x-7z-compressed',
  'application/gzip',
  'application/x-tar'
]

const MAX_FILE_SIZE = 20 * 1024 * 1024
const MAX_BATCH_SIZE = 50 * 1024 * 1024

export interface ValidationResult {
  valid: boolean
  error?: string
}

export function validateFile(file: File): ValidationResult {
  if (file.size > MAX_FILE_SIZE) {
    return { valid: false, error: `文件 "${file.name}" 超过 20MB 限制` }
  }

  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    return { valid: false, error: `不支持的文件类型: .${ext}` }
  }

  if (file.type && !ALLOWED_MIME_PREFIXES.some((prefix) => file.type.startsWith(prefix))) {
    return { valid: false, error: `不支持的文件类型: ${file.type}` }
  }

  return { valid: true }
}

export function validateBatch(files: File[]): ValidationResult {
  const totalSize = files.reduce((sum, f) => sum + f.size, 0)
  if (totalSize > MAX_BATCH_SIZE) {
    return { valid: false, error: '总文件大小超过 50MB 限制' }
  }
  return { valid: true }
}

export const ACCEPT_ATTR = ALLOWED_EXTENSIONS.map((ext) => `.${ext}`).join(',')
