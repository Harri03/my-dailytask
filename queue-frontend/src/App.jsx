import { useState, useEffect } from 'react'
function App() {

  const [name, setName] = useState('')
  const [customer, setCustomer] = useState(null)
  const [queue, setQueue] = useState([])

  const joinQueue = async () => {

    if (name.trim() === '') {
      alert('Please enter your name')
      return
    }

    const response = await fetch(
        `http://localhost:8080/api/queue/join?customerName=${name}`,
        {
          method: 'POST'
        }
    )

    const data = await response.json()

    setCustomer(data)
    setName('')
    getQueue()
  }
  const getQueue = async () => {
    const response = await fetch(
        'http://localhost:8080/api/queue'
    )

    const data = await response.json()

    setQueue(data)
  }
  useEffect(() => {
    const interval = setInterval(() => {
      getQueue()
    }, 5000)

    return () => clearInterval(interval)
  }, [])

  const currentCustomer =
      queue.find((item) => item.id === customer?.id) || customer

  return (
      <div>
        <h1>Adaptive Queue Management</h1>

        <input
            type="text"
            placeholder="Enter your name"
            value={name}
            onChange={(e) => setName(e.target.value)}
        />

        <button onClick={joinQueue}>
          Join Queue
        </button>


        {customer && (
            <>
              <h2>Your Token: {currentCustomer.tokenNumber}</h2>
              <p>Customer: {currentCustomer.customerName}</p>
              <p>Status: {currentCustomer.status}</p>
              <p>Queue Position: {currentCustomer.queuePosition}</p>
              <p>Estimated Wait: {currentCustomer.estimatedWaitingTime}</p>
            </>
        )}
        {queue.map((customer) => (
            <p key={customer.id}>
              Token {customer.tokenNumber} - {customer.customerName} - {customer.status}
            </p>
        ))}

      </div>
  )
}

export default App
