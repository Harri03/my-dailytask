
import { useState, useEffect } from 'react'
import './App.css'

// Backend URL accessible from your computer and phone on the same Wi-Fi.
const API_URL = 'http://192.168.0.101:8080/api/queue'

function App() {
    const [customerName, setCustomerName] = useState('')
    const [phoneNumber, setPhoneNumber] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [registeredCustomer, setRegisteredCustomer] = useState(null)
    const [liveCustomer, setLiveCustomer] = useState(null)
    const [trackingError, setTrackingError] = useState('')

    // Read the date embedded in the QR code URL.
    const qrDate = new URLSearchParams(window.location.search).get('date')

    // Refresh queue information every 5 seconds after registration.
    useEffect(() => {
        if (!registeredCustomer?.id) return

        let cancelled = false

        const fetchQueueStatus = async () => {
            try {
                const response = await fetch(API_URL)

                if (!response.ok) {
                    throw new Error('Could not load queue information.')
                }

                const queue = await response.json()
                if (cancelled) return

                const customer = queue.find(
                    (entry) =>
                        String(entry.id) === String(registeredCustomer.id)
                )

                if (customer) {
                    setLiveCustomer(customer)
                    setTrackingError('')
                } else {
                    setTrackingError(
                        'Your queue details were not found. Please refresh.'
                    )
                }
            } catch {
                if (!cancelled) {
                    setTrackingError(
                        'Unable to refresh queue information. Retrying...'
                    )
                }
            }
        }

        fetchQueueStatus()
        const intervalId = setInterval(fetchQueueStatus, 5000)

        return () => {
            cancelled = true
            clearInterval(intervalId)
        }
    }, [registeredCustomer])

    const handleSubmit = async (event) => {
        event.preventDefault()
        setError('')

        if (!qrDate) {
            setError("Please scan today's QR code to register.")
            return
        }

        if (!customerName.trim() || !phoneNumber.trim()) {
            setError('Please enter your name and phone number.')
            return
        }

        if (!/^[6-9]\d{9}$/.test(phoneNumber)) {
            setError('Please enter a valid 10-digit Indian mobile number.')
            return
        }

        try {
            setLoading(true)

            const params = new URLSearchParams({
                customerName: customerName.trim(),
                phoneNumber,
                qrDate,
            })

            const response = await fetch(
                `${API_URL}/join?${params.toString()}`,
                { method: 'POST' }
            )

            const data = await response.json()

            if (!response.ok) {
                throw new Error(data.error || 'Unable to join the queue.')
            }

            setRegisteredCustomer(data)
            setLiveCustomer(null)
            setCustomerName('')
            setPhoneNumber('')
        } catch (err) {
            setError(
                err.message === 'Failed to fetch'
                    ? 'Cannot connect to the server. Check that the backend is running and your phone and computer are on the same Wi-Fi.'
                    : err.message
            )
        } finally {
            setLoading(false)
        }
    }

    const registerAnotherCustomer = () => {
        setRegisteredCustomer(null)
        setLiveCustomer(null)
        setTrackingError('')
        setError('')
    }

    const status =
        liveCustomer?.status ||
        registeredCustomer?.status ||
        'WAITING'

    const position =
        liveCustomer?.queuePosition ??
        liveCustomer?.position ??
        null

    const waitingTime =
        liveCustomer?.estimatedWaitingTime ??
        liveCustomer?.estimatedWaitingTimeText ??
        null

    const waitingSeconds = liveCustomer?.estimatedWaitingTimeSeconds

    const formatWaitingTime = () => {
        if (waitingTime !== null && waitingTime !== undefined) {
            return waitingTime
        }

        if (typeof waitingSeconds === 'number') {
            const minutes = Math.floor(waitingSeconds / 60)
            const seconds = waitingSeconds % 60

            return minutes > 0
                ? `${minutes} min ${seconds} sec`
                : `${seconds} sec`
        }

        return status === 'COMPLETED' ? '0 sec' : 'Calculating...'
    }

    const readableStatus = {
        WAITING: 'Waiting',
        CALLED: 'Your turn is called',
        SERVING: 'Being served',
        LATE: 'Marked as late',
        COMPLETED: 'Completed',
    }[status] || status

    return (
        <div className="customer-page">
            <header className="customer-header">
                <div className="brand">
                    <div className="brand-icon">Q</div>
                    <div>
                        <h2>QueueFlow</h2>
                        <p>Smart Queue Management</p>
                    </div>
                </div>

                <span className="online-status">
          <span className="status-dot"></span>
          Customer Portal
        </span>
            </header>

            <main className="customer-main">
                <section className="welcome-section">
                    <span className="eyebrow">SMART • SIMPLE • QUICK</span>
                    <h1>Your time matters.</h1>
                    <p>
                        Join the queue online, relax, and keep track of your turn
                        without waiting in a long line.
                    </p>

                    <div className="benefits">
                        <div className="benefit">
                            <span className="benefit-icon">🎟️</span>
                            <div>
                                <strong>Digital token</strong>
                                <p>Get your queue number</p>
                            </div>
                        </div>

                        <div className="benefit">
                            <span className="benefit-icon">⏱️</span>
                            <div>
                                <strong>Waiting time</strong>
                                <p>Know your estimated wait</p>
                            </div>
                        </div>

                        <div className="benefit">
                            <span className="benefit-icon">🔔</span>
                            <div>
                                <strong>Turn updates</strong>
                                <p>Stay informed about your turn</p>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="registration-card">
                    {registeredCustomer ? (
                        <div className="success-panel">
                            <span className="form-icon">✓</span>
                            <span className="form-label">
                REGISTRATION SUCCESSFUL
              </span>
                            <h2>Your queue ticket</h2>
                            <p>
                                Welcome, {registeredCustomer.customerName || 'Customer'}!
                            </p>

                            <div className="token-display">
                                <span>Your token number</span>
                                <strong>{registeredCustomer.tokenNumber}</strong>
                            </div>

                            <div className="tracking-details">
                                <div className="tracking-item">
                                    <span>Current status</span>
                                    <strong>{readableStatus}</strong>
                                </div>

                                <div className="tracking-item">
                                    <span>Queue position</span>
                                    <strong>
                                        {status === 'COMPLETED'
                                            ? 'Finished'
                                            : position != null
                                                ? position
                                                : 'Updating...'}
                                    </strong>
                                </div>

                                <div className="tracking-item">
                                    <span>Estimated waiting time</span>
                                    <strong>{formatWaitingTime()}</strong>
                                </div>
                            </div>

                            {trackingError && (
                                <p role="status" className="error-message">
                                    {trackingError}
                                </p>
                            )}

                            <p className="success-message">
                                Queue information refreshes automatically every 5 seconds.
                            </p>

                            <button
                                className="join-button"
                                type="button"
                                onClick={registerAnotherCustomer}
                            >
                                Register another customer <span>→</span>
                            </button>
                        </div>
                    ) : (
                        <>
                            <div className="card-heading">
                                <span className="form-icon">✦</span>
                                <span className="form-label">
                  CUSTOMER REGISTRATION
                </span>
                                <h2>Join the queue</h2>
                                <p>Enter your details to get started.</p>
                            </div>

                            <form onSubmit={handleSubmit}>
                                <label htmlFor="customerName">Full name</label>
                                <input
                                    id="customerName"
                                    type="text"
                                    placeholder="Enter your full name"
                                    value={customerName}
                                    onChange={(event) =>
                                        setCustomerName(event.target.value)
                                    }
                                    required
                                />

                                <label htmlFor="phoneNumber">Mobile number</label>
                                <div className="phone-input">
                                    <span>🇮🇳 +91</span>
                                    <input
                                        id="phoneNumber"
                                        type="tel"
                                        inputMode="numeric"
                                        maxLength={10}
                                        placeholder="10-digit mobile number"
                                        value={phoneNumber}
                                        onChange={(event) =>
                                            setPhoneNumber(
                                                event.target.value.replace(/\D/g, '')
                                            )
                                        }
                                        required
                                    />
                                </div>

                                {error && (
                                    <p role="alert" className="error-message">
                                        {error}
                                    </p>
                                )}

                                <button
                                    className="join-button"
                                    type="submit"
                                    disabled={loading}
                                >
                                    {loading ? 'Joining...' : 'Join Queue'}
                                    {!loading && <span>→</span>}
                                </button>
                            </form>

                            <div className="privacy-note">
                                <span>🔒</span>
                                Your details are used for queue management.
                            </div>
                        </>
                    )}
                </section>
            </main>

            <footer className="customer-footer">
                <p>QueueFlow © 2026 · Smarter waiting, better service.</p>
            </footer>
        </div>
    )
}

export default App
