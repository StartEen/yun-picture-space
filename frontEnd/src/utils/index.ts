import { size } from 'lodash-es'
import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知参数'
  if (size < 1024) {
    return size + 'B'
  }
  if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + 'KB'
  }
  return (size / (1024 * 1024)).toFixed(2) + 'MB'
}

/**
 * 下载图片
 * @param url 图片下载地址
 * @param name 要保存为的文件名称
 */
export function downloadImage(url?: string, name?: string) {
  if (!url) return
  saveAs(url, name)
}
