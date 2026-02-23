import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import { useState } from 'react';
import TeacherDashboard from './pages/TeacherDashboard';
import StudentDashboard from './pages/StudentDashboard';
import LoginPage from './pages/LoginPage';

function App() {
    const [user, setUser] = useState(null);

    const handleLogout = () => {
        setUser(null);
    };

    return (
        <Router>
            <div className="min-h-screen bg-gray-50 flex flex-col">

                <nav className="bg-white shadow-sm w-full px-8 py-4 flex justify-between items-center border-b border-gray-200">
                    <div className="text-2xl font-extrabold text-blue-600 tracking-tight">
                        <Link to="/">ClassSync</Link>
                    </div>

                    <div className="flex items-center gap-4">
                        {user && (
                            <>
                                <span className="text-gray-700 font-medium">Hello, <span className="font-bold">{user.name}</span></span>
                                <button onClick={handleLogout} className="px-4 py-2 text-sm bg-red-50 text-red-600 font-bold rounded-md hover:bg-red-100 transition">
                                    Logout
                                </button>
                            </>
                        )}
                    </div>
                </nav>

                <main className="flex-grow flex flex-col items-center pt-8 px-4">
                    <Routes>
                        <Route path="/login" element={!user ? <LoginPage onLogin={setUser} /> : <Navigate to={user.role === 'TEACHER' ? "/teacher" : "/student"} />} />

                        <Route path="/teacher" element={user?.role === 'TEACHER' ? <TeacherDashboard user={user} /> : <Navigate to="/login" />} />
                        <Route path="/student" element={user?.role === 'STUDENT' ? <StudentDashboard user={user} /> : <Navigate to="/login" />} />

                        <Route path="/" element={<Navigate to="/login" />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;