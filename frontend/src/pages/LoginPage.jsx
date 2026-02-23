import { useState } from 'react';
import api from '../api/axiosConfig';
import { useNavigate } from 'react-router-dom';

export default function LoginPage({ onLogin }) {
    const [role, setRole] = useState('STUDENT');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [neptunCode, setNeptunCode] = useState('');
    const [error, setError] = useState('');

    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            // Send the login request to Spring Boot
            const response = await api.post('/api/attendance/generate', {
                email: email,
                password: password,
                neptunCode: role === 'STUDENT' ? neptunCode : null
            });

            onLogin(response.data);

            if (response.data.role === 'TEACHER') {
                navigate('/teacher');
            } else {
                navigate('/student');
            }
        } catch (err) {
            setError(err.response?.data || 'Login failed. Please check your credentials.');
        }
    };

    return (
        <div className="p-8 bg-white rounded-xl shadow-lg w-full max-w-md mt-12 border border-gray-100">
            <h2 className="text-3xl font-extrabold mb-6 text-gray-800 text-center">Welcome Back</h2>

            {/* Role Toggle Buttons */}
            <div className="flex gap-2 mb-6 bg-gray-100 p-1 rounded-lg">
                <button
                    type="button"
                    onClick={() => setRole('STUDENT')}
                    className={`w-full py-2 rounded-md font-bold text-sm transition ${role === 'STUDENT' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
                >
                    Student
                </button>
                <button
                    type="button"
                    onClick={() => setRole('TEACHER')}
                    className={`w-full py-2 rounded-md font-bold text-sm transition ${role === 'TEACHER' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
                >
                    Teacher
                </button>
            </div>

            <form onSubmit={handleLogin} className="flex flex-col gap-4">
                <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Email</label>
                    <input
                        type="email" required
                        value={email} onChange={(e) => setEmail(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Password</label>
                    <input
                        type="password" required
                        value={password} onChange={(e) => setPassword(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>

                {role === 'STUDENT' && (
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1">Neptun Code</label>
                        <input
                            type="text" required
                            value={neptunCode} onChange={(e) => setNeptunCode(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 uppercase"
                        />
                    </div>
                )}

                {error && <p className="text-red-500 text-sm font-semibold text-center mt-2">{error}</p>}

                <button type="submit" className="mt-4 w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition shadow-md">
                    Sign In
                </button>
            </form>
        </div>
    );
}