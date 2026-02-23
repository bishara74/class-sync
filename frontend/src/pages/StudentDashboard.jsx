import { useState } from 'react';
import axios from 'axios';

export default function StudentDashboard({ user }) {
    const [code, setCode] = useState('');
    const [status, setStatus] = useState(null);
    const [error, setError] = useState('');

    const handleCheckIn = async (e) => {
        e.preventDefault();
        setError('');
        setStatus(null);

        try {
            const response = await axios.post('http://localhost:8081/api/attendance/check-in', {
                studentId: user.id,
                code: code
            });

            // The backend returns the saved AttendanceRecord, which includes their status
            setStatus(response.data.status);
        } catch (err) {
            console.error(err);
            // If the backend throws a RuntimeException, Axios catches it here
            if (err.response && err.response.data && err.response.data.message) {
                setError(err.response.data.message);
            } else {
                setError('Check-in failed. Please check the code and try again.');
            }
        }
    };

    return (
        <div className="p-8 bg-white rounded-xl shadow-lg w-full max-w-md mt-12 text-center border border-gray-100">
            <h2 className="text-3xl font-extrabold mb-2 text-gray-800">Student Check-In</h2>
            <p className="text-gray-500 mb-8 font-medium">Enter the 6-digit code provided by your teacher.</p>

            <form onSubmit={handleCheckIn} className="flex flex-col gap-5">
                <div>
                    <input
                        type="text"
                        required
                        maxLength="6"
                        placeholder="000000"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        className="w-full text-center text-4xl font-mono tracking-[0.5em] border-2 border-gray-300 rounded-lg px-4 py-4 focus:outline-none focus:border-green-500 focus:ring-4 focus:ring-green-100 transition"
                    />
                </div>

                <button
                    type="submit"
                    className="w-full bg-green-600 text-white font-bold text-lg py-3 rounded-lg hover:bg-green-700 transition shadow-md"
                >
                    Check In
                </button>
            </form>

            {/* Show errors if there are any */}
            {error && (
                <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 font-semibold">
                    {error}
                </div>
            )}

            {/* Show the Success Status */}
            {status && (
                <div className={`mt-6 p-6 border rounded-lg ${status === 'PRESENT' ? 'bg-green-50 border-green-200 text-green-800' : 'bg-yellow-50 border-yellow-200 text-yellow-800'}`}>
                    <p className="text-sm font-bold uppercase tracking-wider mb-1">Check-in Successful</p>
                    <p className="text-3xl font-extrabold">{status}</p>
                </div>
            )}
        </div>
    );
}