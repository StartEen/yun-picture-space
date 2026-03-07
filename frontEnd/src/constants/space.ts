//空间级别枚举
export const SPACE_LEVEL_ENUM = {
  COMMON: 0,
  PROFESSIONAL: 1,
  FLAGSHIP: 2,
} as const

//空间级别文本映射
export const SPACE_LEVEL_MAP: Record<number, string> = {
  0: '普通空间',
  1: '专业空间',
  2: '旗舰空间',
}

//空间级别选项映射
export const SPACE_LEVEL_OPTIONS = Object.entries(SPACE_LEVEL_MAP).map((key) => {
  const value = Number(key)
  return {
    label: SPACE_LEVEL_MAP[value],
    value: value,
  }
})
