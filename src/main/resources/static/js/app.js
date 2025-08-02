// API Configuration
const API_BASE_URL = '/api/v1';
const API_ENDPOINTS = {
    login: `${API_BASE_URL}/auth/login`,
    books: `${API_BASE_URL}/books`
};

// Application State
let currentUser = null;
let currentPage = 0;
let totalPages = 0;
let currentFilters = {};

// DOM Elements
const loginSection = document.getElementById('login-section');
const booksSection = document.getElementById('books-section');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const welcomeMessage = document.getElementById('welcome-message');
const logoutBtn = document.getElementById('logout-btn');
const booksGrid = document.getElementById('books-grid');
const booksLoading = document.getElementById('books-loading');
const booksError = document.getElementById('books-error');
const pagination = document.getElementById('pagination');

// Filter elements
const titleFilter = document.getElementById('title-filter');
const authorFilter = document.getElementById('author-filter');
const genreFilter = document.getElementById('genre-filter');
const yearFilter = document.getElementById('year-filter');
const availableFilter = document.getElementById('available-filter');
const searchBtn = document.getElementById('search-btn');
const clearFiltersBtn = document.getElementById('clear-filters-btn');

// Quick login buttons
const quickAdminBtn = document.getElementById('quick-admin-btn');
const quickUserBtn = document.getElementById('quick-user-btn');

// Demo credentials
const DEMO_CREDENTIALS = {
    admin: {
        email: 'admin@example.com',
        password: 'admin123'
    },
    user: {
        email: 'alice@example.com',
        password: 'admin123'
    }
};

// Utility Functions
function getAuthToken() {
    return localStorage.getItem('authToken');
}

function setAuthToken(token) {
    localStorage.setItem('authToken', token);
}

function removeAuthToken() {
    localStorage.removeItem('authToken');
}

function setCurrentUser(user) {
    currentUser = user;
    localStorage.setItem('currentUser', JSON.stringify(user));
}

function getCurrentUser() {
    const stored = localStorage.getItem('currentUser');
    return stored ? JSON.parse(stored) : null;
}

function isCurrentUserAdmin() {
    const user = getCurrentUser();
    return user && user.role === 'ADMIN';
}

function clearUserData() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    removeAuthToken();
}

// API Functions
async function makeApiRequest(url, options = {}) {
    const token = getAuthToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            // Token expired or invalid
            clearUserData();
            showLogin();
            throw new Error('Authentication failed');
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        // Handle empty responses (like DELETE operations)
        if (response.status === 204) {
            return {};
        }

        // Check if response has content before trying to parse JSON
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else {
            // If it's not JSON, return empty object for successful operations
            return {};
        }
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

async function login(email, password) {
    const response = await makeApiRequest(API_ENDPOINTS.login, {
        method: 'POST',
        body: JSON.stringify({ email, password })
    });

    return response;
}

// Quick login function for demo purposes
async function quickLogin(userType) {
    const credentials = DEMO_CREDENTIALS[userType];
    if (!credentials) {
        console.error('Invalid user type for quick login');
        return;
    }

    const button = userType === 'admin' ? quickAdminBtn : quickUserBtn;
    const originalText = button.textContent;
    
    try {
        // Disable button and show loading state
        button.disabled = true;
        button.textContent = 'Logging in...';
        
        // Clear any previous errors
        loginError.textContent = '';
        
        // Perform login
        const response = await login(credentials.email, credentials.password);
        
        // Store auth data
        setAuthToken(response.token);
        setCurrentUser({
            userId: response.userId,
            username: response.username,
            email: response.email,
            role: response.role
        });
        
        // Show books section
        showBooks();
        
    } catch (error) {
        console.error('Quick login failed:', error);
        showError('login-error', error.message || 'Login failed. Please try again.');
    } finally {
        // Restore button state
        button.disabled = false;
        button.textContent = originalText;
    }
}

async function fetchBooks(filters = {}, page = 0, size = 10) {
    const queryParams = new URLSearchParams();
    
    // Add pagination parameters
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    queryParams.append('sort', 'id,asc');

    // Add filter parameters
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
            queryParams.append(key, value.toString());
        }
    });

    const url = `${API_ENDPOINTS.books}?${queryParams.toString()}`;
    return await makeApiRequest(url);
}

// UI Functions
function showLogin() {
    loginSection.classList.add('active');
    booksSection.classList.remove('active');
    clearErrorMessages();
}

function showBooks() {
    loginSection.classList.remove('active');
    booksSection.classList.add('active');
    clearErrorMessages();
    
    if (currentUser) {
        welcomeMessage.textContent = `Welcome, ${currentUser.username || currentUser.email}!`;
        
        // Add admin buttons only if user is admin
        if (currentUser.role === 'ADMIN') {
            addAdminButtons();
        }
    }
    
    loadBooks();
}

function addAdminButtons() {
    // Check if admin buttons already exist
    if (document.getElementById('admin-buttons')) {
        return;
    }
    
    // Create admin buttons container
    const adminButtonsContainer = document.createElement('div');
    adminButtonsContainer.id = 'admin-buttons';
    adminButtonsContainer.className = 'admin-buttons-container';
    
    adminButtonsContainer.innerHTML = `
        <div class="admin-buttons">
            <h3>Admin Panel</h3>
            <div class="admin-button-group">
                <button id="manage-books-btn" class="admin-nav-btn">📚 Manage Books</button>
                <button id="manage-users-btn" class="admin-nav-btn">👥 Manage Users</button>
                <button id="manage-authors-btn" class="admin-nav-btn">✍️ Manage Authors</button>
                <button id="manage-circulation-btn" class="admin-nav-btn">🔄 Circulation</button>
            </div>
        </div>
    `;
    
    // Insert after the header
    const header = document.querySelector('.header');
    header.insertAdjacentElement('afterend', adminButtonsContainer);
    
    // Add event listeners
    document.getElementById('manage-books-btn').addEventListener('click', () => {
        window.location.href = '/admin-books.html';
    });
    
    document.getElementById('manage-users-btn').addEventListener('click', () => {
        window.location.href = '/admin-users.html';
    });
    
    document.getElementById('manage-authors-btn').addEventListener('click', () => {
        window.location.href = '/admin-authors.html';
    });
    
    document.getElementById('manage-circulation-btn').addEventListener('click', () => {
        window.location.href = '/admin-circulation.html';
    });
}

function clearErrorMessages() {
    loginError.textContent = '';
    booksError.textContent = '';
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
    }
}

function createBookCard(book) {
    const isAvailable = book.inStock > 0;
    
    return `
        <div class="book-card">
            <div class="book-title">${escapeHtml(book.title)}</div>
            <div class="book-author">by ${escapeHtml(book.authorName)}</div>
            <div class="book-details">
                <div><strong>Genre:</strong> ${escapeHtml(book.genre)}</div>
                <div><strong>Published:</strong> ${book.publishedYear}</div>
                <div><strong>In Stock:</strong> ${book.inStock}</div>
                <div><strong>Total Lent:</strong> ${book.lendCount}</div>
            </div>
            <div class="stock-status ${isAvailable ? 'in-stock' : 'out-of-stock'}">
                ${isAvailable ? '✅ Available' : '❌ Out of Stock'}
            </div>
        </div>
    `;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function createPagination(currentPage, totalPages) {
    if (totalPages <= 1) return '';

    let paginationHtml = '';
    
    // Previous button
    paginationHtml += `
        <button onclick="changePage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
            Previous
        </button>
    `;

    // Page info
    paginationHtml += `
        <span class="page-info">Page ${currentPage + 1} of ${totalPages}</span>
    `;

    // Next button
    paginationHtml += `
        <button onclick="changePage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            Next
        </button>
    `;

    return paginationHtml;
}

async function loadBooks(page = 0) {
    try {
        showBooksLoading(true);
        booksError.textContent = '';
        
        const response = await fetchBooks(currentFilters, page);
        
        // Update pagination info
        currentPage = response.pageable.pageNumber;
        totalPages = response.totalPages;
        
        // Render books
        if (response.content && response.content.length > 0) {
            booksGrid.innerHTML = response.content.map(book => createBookCard(book)).join('');
        } else {
            booksGrid.innerHTML = '<div class="no-books">No books found matching your criteria.</div>';
        }

        // Render pagination
        pagination.innerHTML = createPagination(currentPage, totalPages);
        
    } catch (error) {
        console.error('Failed to load books:', error);
        showError('books-error', `Failed to load books: ${error.message}`);
        booksGrid.innerHTML = '';
    } finally {
        showBooksLoading(false);
    }
}

function showBooksLoading(loading) {
    booksLoading.style.display = loading ? 'block' : 'none';
    booksGrid.style.display = loading ? 'none' : 'grid';
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadBooks(page);
    }
}

function applyFilters() {
    currentFilters = {};
    
    const title = titleFilter.value.trim();
    const author = authorFilter.value.trim();
    const genre = genreFilter.value.trim();
    const year = yearFilter.value.trim();
    const available = availableFilter.value;

    if (title) currentFilters.title = title;
    if (author) currentFilters.authorName = author;
    if (genre) currentFilters.genre = genre;
    if (year) currentFilters.publishedYear = parseInt(year);
    if (available) currentFilters.available = available === 'true';

    loadBooks(0); // Reset to first page when applying filters
}

function clearFilters() {
    titleFilter.value = '';
    authorFilter.value = '';
    genreFilter.value = '';
    yearFilter.value = '';
    availableFilter.value = '';
    currentFilters = {};
    loadBooks(0);
}

function logout() {
    clearUserData();
    showLogin();
}

// Event Listeners
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const submitBtn = loginForm.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Logging in...';
        loginError.textContent = '';
        
        const response = await login(email, password);
        
        // Store auth data
        setAuthToken(response.token);
        setCurrentUser({
            userId: response.userId,
            username: response.username,
            email: response.email,
            role: response.role
        });
        
        // Show books section
        showBooks();
        
    } catch (error) {
        console.error('Login failed:', error);
        showError('login-error', error.message || 'Login failed. Please try again.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login';
    }
});

logoutBtn.addEventListener('click', logout);

searchBtn.addEventListener('click', applyFilters);
clearFiltersBtn.addEventListener('click', clearFilters);

// Quick login button event listeners
quickAdminBtn.addEventListener('click', () => quickLogin('admin'));
quickUserBtn.addEventListener('click', () => quickLogin('user'));

// Enter key support for filters
[titleFilter, authorFilter, genreFilter, yearFilter].forEach(input => {
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            applyFilters();
        }
    });
});

// Initialize App
function initApp() {
    const token = getAuthToken();
    const user = getCurrentUser();
    
    if (token && user) {
        currentUser = user;
        showBooks();
    } else {
        showLogin();
    }
}

// Start the application
document.addEventListener('DOMContentLoaded', initApp);
