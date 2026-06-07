import { ref, computed } from 'vue'

export function usePagination(defaultSize = 20) {
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)
  const pages = computed(() => Math.ceil(total.value / size.value))
  const hasMore = computed(() => page.value < pages.value)

  function setTotal(val: number) {
    total.value = val
  }

  function nextPage() {
    if (hasMore.value) page.value++
  }

  function reset() {
    page.value = 1
    total.value = 0
  }

  return {
    page,
    size,
    total,
    pages,
    hasMore,
    setTotal,
    nextPage,
    reset,
  }
}
