/**
 * 定义枚举信息
 */
export const PIC_REVIEW_STATUS_ENUM = {
  REVIEWING: 0,
  PASS: 1,
  REJECT: 2,
}
/**
 * 中文映射
 */
export const PIC_REVIEW_STATUS_MAP = {
  0: '审核中',
  1: '通过',
  2: '拒绝',
}

export const PIC_REVIEW_STATUS_OPTIONS = Object.keys(PIC_REVIEW_STATUS_MAP).map((key) => {
  return {
    label: PIC_REVIEW_STATUS_MAP[key],
    value: key,
  }
})
