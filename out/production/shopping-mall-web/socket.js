import io from 'socket.io-client'
// const socket = process.env.NODE_ENV === 'development' ? io('ws://127.0.0.1:27781') : io('wss://web.archialgo.com');
const socket = io('ws://10.192.42.244:27781')
export default socket