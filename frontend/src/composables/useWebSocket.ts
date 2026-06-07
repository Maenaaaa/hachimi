import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import { getToken } from '@/utils/request'

let globalClient: Client | null = null
let globalInitCount = 0

export function useWebSocket() {
  const connected = ref(false)
  const subscriptions: (() => void)[] = []

  function connect() {
    if (globalClient?.active) {
      connected.value = true
      return
    }
    const token = getToken()
    if (!token) return

    globalClient = new Client({
      brokerURL: `ws://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    })

    globalClient.onConnect = () => {
      connected.value = true
    }

    globalClient.onDisconnect = () => {
      connected.value = false
    }

    globalClient.activate()
  }

  function subscribe(destination: string, callback: (body: string) => void) {
    if (!globalClient?.active) {
      connect()
    }
    const sub = globalClient?.subscribe(destination, (message) => {
      callback(message.body)
    })
    if (sub) {
      subscriptions.push(() => sub.unsubscribe())
    }
    return sub
  }

  function send(destination: string, body: string) {
    if (globalClient?.active) {
      globalClient.publish({ destination, body })
    }
  }

  function disconnect() {
    subscriptions.forEach((fn) => fn())
    subscriptions.length = 0
    globalClient?.deactivate()
    globalClient = null
    connected.value = false
  }

  globalInitCount++
  onUnmounted(() => {
    globalInitCount--
    if (globalInitCount <= 0) {
      disconnect()
    }
  })

  return {
    connected,
    connect,
    subscribe,
    send,
    disconnect,
  }
}
