// Authentication utilities

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    // Only check auth on protected pages (not login/register)
    const currentPath = window.location.pathname;
    const publicPaths = ['/login', '/register'];
    
    if (!publicPaths.includes(currentPath)) {
        if (!isAuthenticated()) {
            window.location.href = '/login';
            return;
        }
        
        // Verify token is still valid
        verifyToken();
    }
    
    // Add token to all navigation links
    addTokenToNavigation();
});

// Add JWT token to navigation requests
function addTokenToNavigation() {
    const token = localStorage.getItem('accessToken');
    if (token) {
        // Intercept all navigation clicks
        document.addEventListener('click', function(e) {
            const link = e.target.closest('a');
            if (link && link.href && !link.href.includes('api') && !link.href.includes('http')) {
                // Add token as header for internal navigation
                fetch(link.href, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                }).then(response => {
                    if (response.ok) {
                        return response.text();
                    } else if (response.status === 401) {
                        logout();
                    }
                }).then(html => {
                    if (html) {
                        document.open();
                        document.write(html);
                        document.close();
                        history.pushState(null, '', link.href);
                    }
                }).catch(() => {
                    // Fallback to normal navigation
                    window.location.href = link.href;
                });
                
                e.preventDefault();
            }
        });
    }
}

// Verify token validity
async function verifyToken() {
    try {
        await apiCall('/auth/me');
    } catch (error) {
        console.error('Token verification failed:', error);
        logout();
    }
}

// Login function
async function login(email, password) {
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed');
        }
        
        const data = await response.json();
        
        // Store tokens and user info
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data.user));
        
        return data;
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

// Register function
async function register(userData) {
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Registration failed');
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Registration error:', error);
        throw error;
    }
}

// Get current user
function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

// Check user roles
function hasRole(role) {
    const user = getCurrentUser();
    return user && user.role === role;
}

function isAdmin() {
    return hasRole('ADMIN');
}

function isLoanOfficer() {
    return hasRole('LOAN_OFFICER');
}

function isCustomer() {
    return hasRole('USER');
}

// Logout function
async function logout() {
    try {
        // Call backend logout endpoint
        const token = localStorage.getItem('accessToken');
        if (token) {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
        }
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        // Clear all stored data
        localStorage.clear();
        sessionStorage.clear();
        
        // Clear specific items if localStorage.clear() doesn't work
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        
        // Update navbar if function exists
        if (typeof updateNavbarAuth === 'function') {
            updateNavbarAuth();
        }
        
        // Redirect to login page
        window.location.href = '/login';
    }
}

// Redirect based on role
function redirectBasedOnRole() {
    const user = getCurrentUser();
    
    if (user && user.role === 'ADMIN') {
        window.location.href = '/admin';
    } else if (user && user.role === 'LOAN_OFFICER') {
        window.location.href = '/loan-management';
    } else {
        window.location.href = '/dashboard';
    }
}

// Check if user is authenticated
function isAuthenticated() {
    return localStorage.getItem('accessToken') !== null;
}