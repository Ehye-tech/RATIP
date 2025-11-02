import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import RATIP from './ratip_platform'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <RATIP />
    </>
  )
}

export default App
