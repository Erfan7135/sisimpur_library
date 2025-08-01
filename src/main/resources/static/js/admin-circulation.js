// API Configuration
const API_BASE_URL = '/api/v1';
const API_ENDPOINTS = {
    lendings: `${API_BASE_URL}/lendings`,
    users: `${API_BASE_URL}/users`,
    books: `${API_BASE_URL}/books`
};

// Application State
let currentUser = null;
let currentPage = 0;
let totalPages = 0;
let historyPage = 0;
let historyTotalPages = 0;
let users = [];
let availableBooks = [];

// Filter state
let activeFilters = {
    userFilter: '',
    bookFilter: ''
};
let historyFilters = {
    statusFilter: 'all',
    userFilter: '',
    bookFilter: ''
};

// DOM Elements
const welcomeMessage = document.getElementById('welcome-message');
const logoutBtn = document.getElementById('logout-btn');
const backToBooksBtn = document.getElementById('back-to-books-btn');
const manageBooksBtn = document.getElementById('manage-books-btn');
const manageUsersBtn = document.getElementById('manage-users-btn');
const manageAuthorsBtn = document.getElementById('manage-authors-btn');

// Lendings elements
const lendingsTable = document.getElementById('lendings-table');
const lendingsTbody = document.getElementById('lendings-tbody');
const lendingsLoading = document.getElementById('lendings-loading');
const lendingsError = document.getElementById('lendings-error');
const pagination = document.getElementById('pagination');

// Active lendings filter elements
const activeUserFilter = document.getElementById('active-user-filter');
const activeBookFilter = document.getElementById('active-book-filter');
const filterActiveBtn = document.getElementById('filter-active-btn');
const clearActiveFilterBtn = document.getElementById('clear-active-filter-btn');

// History elements
const historyTable = document.getElementById('history-table');
const historyTbody = document.getElementById('history-tbody');
const historyLoading = document.getElementById('history-loading');
const historyError = document.getElementById('history-error');
const historyPagination = document.getElementById('history-pagination');
const historyFilter = document.getElementById('history-filter');
const historyUserFilter = document.getElementById('history-user-filter');
const historyBookFilter = document.getElementById('history-book-filter');
const filterHistoryBtn = document.getElementById('filter-history-btn');
const clearHistoryFilterBtn = document.getElementById('clear-history-filter-btn');

// Modal elements
const lendModal = document.getElementById('lend-modal');
const lendForm = document.getElementById('lend-form');
const lendBookBtn = document.getElementById('lend-book-btn');
const refreshBtn = document.getElementById('refresh-btn');

// Form elements
const lendUserSelect = document.getElementById('lend-user');
const lendBookSelect = document.getElementById('lend-book');

// Utility Functions
function getAuthToken() {
    return localStorage.getItem('authToken');
}

function getCurrentUser() {
    const stored = localStorage.getItem('currentUser');
    return stored ? JSON.parse(stored) : null;
}

function clearUserData() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
}

function checkAdminAccess() {
    const token = getAuthToken();
    const user = getCurrentUser();
    
    if (!token || !user) {
        window.location.href = '/index.html';
        return false;
    }
    
    return true;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString();
}

function calculateDaysBorrowed(lendingDate) {
    const lentDate = new Date(lendingDate);
    const today = new Date();
    const diffTime = today - lentDate;
    return Math.floor(diffTime / (1000 * 60 * 60 * 24));
}

function getDaysClass(days) {
    if (days <= 7) return 'days-normal';
    if (days <= 14) return 'days-warning';
    return 'days-overdue';
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
            clearUserData();
            window.location.href = '/index.html';
            throw new Error('Authentication failed');
        }

        if (response.status === 403) {
            alert('Access denied. Admin privileges required.');
            window.location.href = '/index.html';
            throw new Error('Access denied');
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

async function fetchActiveLendings(page = 0, size = 10) {
    const queryParams = new URLSearchParams();
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());

    const url = `${API_ENDPOINTS.lendings}/active?${queryParams.toString()}`;
    return await makeApiRequest(url);
}

async function fetchLendingHistory(page = 0, size = 10, filter = 'all') {
    const queryParams = new URLSearchParams();
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    
    let url;
    if (filter === 'returned') {
        url = `${API_ENDPOINTS.lendings}/returned?${queryParams.toString()}`;
    } else if (filter === 'active') {
        url = `${API_ENDPOINTS.lendings}/active?${queryParams.toString()}`;
    } else {
        url = `${API_ENDPOINTS.lendings}?${queryParams.toString()}`;
    }

    return await makeApiRequest(url);
}

async function fetchUsers() {
    const url = `${API_ENDPOINTS.users}?size=1000&sort=name,asc`;
    return await makeApiRequest(url);
}

async function fetchAvailableBooks() {
    const url = `${API_ENDPOINTS.books}?size=1000&sort=title,asc`;
    return await makeApiRequest(url);
}

async function lendBook(userId, bookId) {
    return await makeApiRequest(`${API_ENDPOINTS.lendings}/lend`, {
        method: 'POST',
        body: JSON.stringify({
            userId: userId,
            bookId: bookId
        })
    });
}

async function returnBook(lendingId) {
    return await makeApiRequest(`${API_ENDPOINTS.lendings}/return/${lendingId}`, {
        method: 'POST'
    });
}

// UI Functions
function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
    }
}

function clearError(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = '';
    }
}

function showLoading(elementId, loading) {
    const loadingElement = document.getElementById(elementId);
    const tableElement = document.getElementById(elementId.replace('-loading', '-table'));
    
    if (loadingElement) loadingElement.style.display = loading ? 'block' : 'none';
    if (tableElement) tableElement.style.display = loading ? 'none' : 'table';
}

function createLendingRow(lending) {
    const days = calculateDaysBorrowed(lending.startDate);
    const daysClass = getDaysClass(days);
    
    return `
        <tr>
            <td>${lending.id}</td>
            <td>${escapeHtml(lending.userName)}</td>
            <td>${escapeHtml(lending.bookTitle)}</td>
            <td>${formatDate(lending.startDate)}</td>
            <td class="days-counter ${daysClass}">${days} days</td>
            <td>
                <button class="return-btn" onclick="confirmReturnBook(${lending.id}, '${escapeHtml(lending.bookTitle)}', '${escapeHtml(lending.userName)}')">
                    Return
                </button>
            </td>
        </tr>
    `;
}

function createHistoryRow(lending) {
    const status = lending.endDate ? 'returned' : 'active';
    const statusClass = `status-${status}`;
    
    return `
        <tr>
            <td>${lending.id}</td>
            <td>${escapeHtml(lending.userName)}</td>
            <td>${escapeHtml(lending.bookTitle)}</td>
            <td>${formatDate(lending.startDate)}</td>
            <td>${formatDate(lending.endDate)}</td>
            <td><span class="status-badge ${statusClass}">${status}</span></td>
        </tr>
    `;
}

function createPagination(currentPage, totalPages, onPageChange) {
    if (totalPages <= 1) return '';

    let paginationHtml = '';
    
    paginationHtml += `
        <button onclick="${onPageChange}(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
            Previous
        </button>
    `;

    paginationHtml += `
        <span class="page-info">Page ${currentPage + 1} of ${totalPages}</span>
    `;

    paginationHtml += `
        <button onclick="${onPageChange}(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            Next
        </button>
    `;

    return paginationHtml;
}

async function loadActiveLendings(page = 0) {
    try {
        showLoading('lendings-loading', true);
        clearError('lendings-error');
        
        // Fetch all active lendings (we'll do client-side filtering for now)
        const response = await fetchActiveLendings(page, 100); // Increase size for client-side filtering
        
        let filteredContent = response.content || [];
        
        // Apply client-side filtering
        if (activeFilters.userFilter || activeFilters.bookFilter) {
            filteredContent = filterLendingsClientSide(filteredContent, activeFilters.userFilter, activeFilters.bookFilter);
        }
        
        // Simple pagination for filtered results
        const itemsPerPage = 10;
        const startIndex = page * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const paginatedContent = filteredContent.slice(startIndex, endIndex);
        
        currentPage = page;
        totalPages = Math.ceil(filteredContent.length / itemsPerPage);
        
        if (paginatedContent.length > 0) {
            lendingsTbody.innerHTML = paginatedContent.map(lending => createLendingRow(lending)).join('');
        } else {
            lendingsTbody.innerHTML = '<tr><td colspan="6" class="no-data">No active lendings found matching the filter criteria.</td></tr>';
        }

        pagination.innerHTML = createPagination(currentPage, totalPages, 'changeLendingsPage');
        
    } catch (error) {
        console.error('Failed to load active lendings:', error);
        showError('lendings-error', `Failed to load active lendings: ${error.message}`);
        lendingsTbody.innerHTML = '';
    } finally {
        showLoading('lendings-loading', false);
    }
}

async function loadLendingHistory(page = 0) {
    try {
        showLoading('history-loading', true);
        clearError('history-error');
        
        // Use the existing fetchLendingHistory with status filter
        const response = await fetchLendingHistory(page, 100, historyFilters.statusFilter); // Increase size for client-side filtering
        
        let filteredContent = response.content || [];
        
        // Apply client-side filtering for user and book
        if (historyFilters.userFilter || historyFilters.bookFilter) {
            filteredContent = filterLendingsClientSide(filteredContent, historyFilters.userFilter, historyFilters.bookFilter);
        }
        
        // Simple pagination for filtered results
        const itemsPerPage = 10;
        const startIndex = page * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const paginatedContent = filteredContent.slice(startIndex, endIndex);
        
        historyPage = page;
        historyTotalPages = Math.ceil(filteredContent.length / itemsPerPage);
        
        if (paginatedContent.length > 0) {
            historyTbody.innerHTML = paginatedContent.map(lending => createHistoryRow(lending)).join('');
        } else {
            historyTbody.innerHTML = '<tr><td colspan="6" class="no-data">No lending history found matching the filter criteria.</td></tr>';
        }

        historyPagination.innerHTML = createPagination(historyPage, historyTotalPages, 'changeHistoryPage');
        
    } catch (error) {
        console.error('Failed to load lending history:', error);
        showError('history-error', `Failed to load lending history: ${error.message}`);
        historyTbody.innerHTML = '';
    } finally {
        showLoading('history-loading', false);
    }
}

async function loadUsers() {
    try {
        const response = await fetchUsers();
        users = response.content ? response.content : response;
        
        lendUserSelect.innerHTML = '<option value="">Select a user</option>';
        users.forEach(user => {
            lendUserSelect.innerHTML += `<option value="${user.id}">${escapeHtml(user.name)} (${escapeHtml(user.email)})</option>`;
        });
    } catch (error) {
        console.error('Failed to load users:', error);
        alert('Failed to load users. Please refresh the page.');
    }
}

async function loadAvailableBooks() {
    try {
        const response = await fetchAvailableBooks();
        availableBooks = response.content ? response.content : response;
        
        lendBookSelect.innerHTML = '<option value="">Select a book</option>';
        availableBooks.forEach(book => {
            lendBookSelect.innerHTML += `<option value="${book.id}">${escapeHtml(book.title)} by ${escapeHtml(book.authorName)} (${book.inStock} available)</option>`;
        });
    } catch (error) {
        console.error('Failed to load available books:', error);
        alert('Failed to load available books. Please refresh the page.');
    }
}

function changeLendingsPage(page) {
    if (page >= 0 && page < totalPages) {
        loadActiveLendings(page);
    }
}

function changeHistoryPage(page) {
    if (page >= 0 && page < historyTotalPages) {
        loadLendingHistory(page);
    }
}

// Modal Functions
function showLendModal() {
    lendModal.style.display = 'block';
}

function hideLendModal() {
    lendModal.style.display = 'none';
    lendForm.reset();
}

function confirmReturnBook(lendingId, bookTitle, userName) {
    if (confirm(`Are you sure you want to return "${bookTitle}" borrowed by ${userName}?`)) {
        returnBookById(lendingId);
    }
}

async function returnBookById(lendingId) {
    try {
        await returnBook(lendingId);
        alert('Book returned successfully!');
        loadActiveLendings(currentPage);
        loadLendingHistory(historyPage);
        // Refresh available books for lending
        loadAvailableBooks();
    } catch (error) {
        console.error('Failed to return book:', error);
        alert(`Failed to return book: ${error.message}`);
    }
}

function refreshData() {
    loadActiveLendings(currentPage);
    loadLendingHistory(historyPage);
    loadUsers();
    loadAvailableBooks();
}

// Filter functions
function filterLendingsClientSide(lendings, userFilter, bookFilter) {
    return lendings.filter(lending => {
        const userMatch = !userFilter || lending.userName.toLowerCase().includes(userFilter.toLowerCase());
        const bookMatch = !bookFilter || lending.bookTitle.toLowerCase().includes(bookFilter.toLowerCase());
        return userMatch && bookMatch;
    });
}

function applyActiveFilters() {
    activeFilters.userFilter = activeUserFilter.value;
    activeFilters.bookFilter = activeBookFilter.value;
    loadActiveLendings(0); // Reset to first page when filtering
}

function clearActiveFilters() {
    activeUserFilter.value = '';
    activeBookFilter.value = '';
    activeFilters.userFilter = '';
    activeFilters.bookFilter = '';
    loadActiveLendings(0);
}

function applyHistoryFilters() {
    historyFilters.statusFilter = historyFilter.value;
    historyFilters.userFilter = historyUserFilter.value;
    historyFilters.bookFilter = historyBookFilter.value;
    loadLendingHistory(0); // Reset to first page when filtering
}

function clearHistoryFilters() {
    historyFilter.value = 'all';
    historyUserFilter.value = '';
    historyBookFilter.value = '';
    historyFilters.statusFilter = 'all';
    historyFilters.userFilter = '';
    historyFilters.bookFilter = '';
    loadLendingHistory(0);
}

// Navigation Functions
function logout() {
    clearUserData();
    window.location.href = '/index.html';
}

function goToBooks() {
    window.location.href = '/index.html';
}

function goToBooksManagement() {
    window.location.href = '/admin-books.html';
}

function goToUsers() {
    window.location.href = '/admin-users.html';
}

function goToAuthors() {
    window.location.href = '/admin-authors.html';
}

// Event Listeners
lendForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const userId = parseInt(lendUserSelect.value);
    const bookId = parseInt(lendBookSelect.value);

    if (!userId || !bookId) {
        alert('Please select both a user and a book.');
        return;
    }

    const submitBtn = lendForm.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Lending...';
        
        await lendBook(userId, bookId);
        alert('Book lent successfully!');
        
        hideLendModal();
        loadActiveLendings(currentPage);
        loadLendingHistory(historyPage);
        loadAvailableBooks(); // Refresh available books
        
    } catch (error) {
        console.error('Failed to lend book:', error);
        alert(`Failed to lend book: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Lend Book';
    }
});

// Modal event listeners
lendBookBtn.addEventListener('click', showLendModal);
refreshBtn.addEventListener('click', refreshData);

document.querySelector('.close').addEventListener('click', hideLendModal);
document.querySelector('.cancel-btn').addEventListener('click', hideLendModal);

window.addEventListener('click', (e) => {
    if (e.target === lendModal) {
        hideLendModal();
    }
});

// Filter event listeners
filterActiveBtn.addEventListener('click', applyActiveFilters);
clearActiveFilterBtn.addEventListener('click', clearActiveFilters);

filterHistoryBtn.addEventListener('click', applyHistoryFilters);
clearHistoryFilterBtn.addEventListener('click', clearHistoryFilters);

// Add Enter key support for filter inputs
activeUserFilter.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        applyActiveFilters();
    }
});

activeBookFilter.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        applyActiveFilters();
    }
});

historyUserFilter.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        applyHistoryFilters();
    }
});

historyBookFilter.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        applyHistoryFilters();
    }
});

// Navigation event listeners
logoutBtn.addEventListener('click', logout);
backToBooksBtn.addEventListener('click', goToBooks);
manageBooksBtn.addEventListener('click', goToBooksManagement);
manageUsersBtn.addEventListener('click', goToUsers);
manageAuthorsBtn.addEventListener('click', goToAuthors);

// Initialize App
function initApp() {
    if (!checkAdminAccess()) {
        return;
    }
    
    currentUser = getCurrentUser();
    
    if (currentUser) {
        welcomeMessage.textContent = `Welcome, ${currentUser.username || currentUser.email}!`;
    }
    
    // Load initial data
    loadUsers();
    loadAvailableBooks();
    loadActiveLendings();
    loadLendingHistory();
}

// Start the application
document.addEventListener('DOMContentLoaded', initApp);
