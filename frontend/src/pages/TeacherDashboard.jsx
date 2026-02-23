import { useState } from 'react';
import api from '../api/axiosConfig';

export default function TeacherDashboard({ user }) {
    const [courseName, setCourseName] = useState('');
    const [validFor, setValidFor] = useState(10);

    const [generatedCode, setGeneratedCode] = useState(null);
    const [error, setError] = useState('');

    const handleGenerate = async (e) => {
        e.preventDefault(); // Prevents the page from refreshing
        setError('');
        setGeneratedCode(null);

        try {

            const response = await api.post('/api/attendance/generate', {
                teacherId: user.id,
                courseName: courseName,
                validForMinutes: validFor
            });

            setGeneratedCode(response.data.generatedCode);
        } catch (err) {
            console.error(err);
            setError('Failed to generate code. Is the Spring Boot server running?');
        }
    };

    return (
        <div className="p-6 bg-white rounded-lg shadow-md w-full max-w-md mt-8">
            <h2 className="text-2xl font-bold mb-6 text-gray-800 text-center">Teacher Dashboard</h2>

            <form onSubmit={handleGenerate} className="flex flex-col gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Course Name</label>
                    <input
                        type="text"
                        required
                        placeholder="e.g. Data Structures"
                        value={courseName}
                        onChange={(e) => setCourseName(e.target.value)}
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Valid for (minutes)</label>
                    <input
                        type="number"
                        min="1"
                        max="60"
                        value={validFor}
                        onChange={(e) => setValidFor(e.target.value)}
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                <button
                    type="submit"
                    className="mt-2 w-full bg-blue-600 text-white font-bold py-2 rounded hover:bg-blue-700 transition"
                >
                    Generate Attendance Code
                </button>
            </form>

            {error && <p className="mt-4 text-red-500 text-center font-medium">{error}</p>}

            {generatedCode && (
                <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded text-center">
                    <p className="text-sm text-green-800 font-semibold mb-1">Share this code with students:</p>
                    <p className="text-4xl font-mono font-extrabold text-green-600 tracking-widest">{generatedCode}</p>
                </div>
            )}
        </div>
    );
}