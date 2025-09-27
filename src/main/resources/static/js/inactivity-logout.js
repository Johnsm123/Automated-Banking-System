// Inactivity Logout - 15 minutes timeout with 5-minute warning
console.log('INACTIVITY LOGOUT SCRIPT LOADED AT:', new Date().toLocaleTimeString());

(function() {
    console.log('Inactivity logout IIFE started');
    let inactivityTimer = null;
    let warningTimer = null;
    const INACTIVITY_TIMEOUT = 6 * 60 * 1000; // 15 minutes
    const WARNING_TIME = 5 * 60 * 1000; // 5 minutes before timeout
    
    // Events that indicate user activity
    const activityEvents = [
        'mousedown', 'mousemove', 'keypress', 'scroll', 
        'touchstart', 'click', 'keydown', 'focus', 'blur'
    ];
    
    function showWarningModal() {
        const modal = document.createElement('div');
        modal.id = 'inactivity-modal';
        modal.innerHTML = `
            <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5);" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-warning">
                            <h5 class="modal-title">Session Timeout Warning</h5>
                        </div>
                        <div class="modal-body">
                            <p>Your session will expire in <strong>5 minutes</strong> due to inactivity.</p>
                            <p>Click "Stay Logged In" to continue your session.</p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" onclick="stayLoggedIn()">Stay Logged In</button>
                            <button type="button" class="btn btn-secondary" onclick="logoutNow()">Logout Now</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        document.body.classList.add('modal-open');
    }
    
    window.stayLoggedIn = function() {
        const modal = document.getElementById('inactivity-modal');
        if (modal) {
            modal.remove();
        }
        document.body.classList.remove('modal-open');
        resetInactivityTimer();
    };
    
    window.logoutNow = function() {
        const modal = document.getElementById('inactivity-modal');
        if (modal) {
            modal.remove();
        }
        document.body.classList.remove('modal-open');
        if (typeof logout === 'function') {
            logout();
        }
    };
    
    function resetInactivityTimer() {
        if (typeof isAuthenticated === 'function' && isAuthenticated()) {
            console.log('Activity detected - Resetting timers at:', new Date().toLocaleTimeString());
            
            clearTimeout(inactivityTimer);
            clearTimeout(warningTimer);
            
            // Set warning timer (10 minutes)
            warningTimer = setTimeout(() => {
                console.log('Showing warning modal at:', new Date().toLocaleTimeString());
                showWarningModal();
            }, INACTIVITY_TIMEOUT - WARNING_TIME);
            
            // Set logout timer (15 minutes)
            inactivityTimer = setTimeout(() => {
                console.log('Inactivity timeout reached - Initiating logout at:', new Date().toLocaleTimeString());
                if (typeof logout === 'function') {
                    logout();
                }
            }, INACTIVITY_TIMEOUT);
        }
    }
    
    function setupInactivityLogout() {
        // Only setup if user is authenticated
        if (typeof isAuthenticated === 'function' && isAuthenticated()) {
            console.log('Setting up inactivity logout monitoring');
            console.log('Monitoring events:', activityEvents);
            console.log('Timeout duration:', INACTIVITY_TIMEOUT / 1000, 'seconds');
            
            // Add event listeners for activity detection
            activityEvents.forEach(event => {
                document.addEventListener(event, (e) => {
                    console.log('Activity event detected:', e.type);
                    resetInactivityTimer();
                }, true);
            });
            
            // Handle page visibility changes
            document.addEventListener('visibilitychange', () => {
                if (!document.hidden) {
                    console.log('Page became visible - Resetting timer');
                    resetInactivityTimer();
                } else {
                    console.log('Page became hidden - Timer continues');
                }
            });
            
            // Start the timer
            console.log('Starting initial inactivity timer');
            resetInactivityTimer();
        } else {
            console.log('User not authenticated - Skipping inactivity setup');
        }
    }
    
    // Initialize when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        console.log('DOM loaded - Initializing inactivity logout');
        
        // Check if we're on a protected page
        const currentPath = window.location.pathname;
        const publicPaths = ['/login', '/register'];
        
        console.log('Current path:', currentPath);
        console.log('Public paths:', publicPaths);
        
        if (!publicPaths.includes(currentPath)) {
            console.log('Protected page detected - Setting up inactivity logout');
            setupInactivityLogout();
        } else {
            console.log('Public page detected - Skipping inactivity logout');
        }
    });
})();

console.log('INACTIVITY LOGOUT SCRIPT EXECUTION COMPLETE');

// Global test function to verify script is loaded
window.testInactivityScript = function() {
    console.log('Script test function called - Inactivity logout is loaded!');
    return 'Inactivity logout script is working!';
};