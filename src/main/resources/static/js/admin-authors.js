// API Configuration
const API_BASE_URL = '/api/v1';
const API_ENDPOINTS = {
    authors: `${API_BASE_URL}/authors`
};

// Application State
let currentUser = null;
let currentPage = 0;
let totalPages = 0;

// DOM Elements
const welcomeMessage = document.getElementById('welcome-message');
const logoutBtn = document.getElementById('logout-btn');
const backToBooksBtn = document.getElementById('back-to-books-btn');
const manageBooksBtn = document.getElementById('manage-books-btn');
const manageUsersBtn = document.getElementById('manage-users-btn');
const manageCirculationBtn = document.getElementById('manage-circulation-btn');

const authorsTable = document.getElementById('authors-table');
const authorsTbody = document.getElementById('authors-tbody');
const authorsLoading = document.getElementById('authors-loading');
const authorsError = document.getElementById('authors-error');
const pagination = document.getElementById('pagination');

const authorModal = document.getElementById('author-modal');
const modalTitle = document.getElementById('modal-title');
const authorForm = document.getElementById('author-form');
const addAuthorBtn = document.getElementById('add-author-btn');

// Form elements
const idInput = document.getElementById('author-id');
const authorNameInput = document.getElementById('author-name');
const authorBioInput = document.getElementById('author-bio');

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

function truncateText(text, maxLength = 100) {
    if (!text) return '-';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
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

async function fetchAuthors(page = 0, size = 10) {
    const queryParams = new URLSearchParams();
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    queryParams.append('sort', 'id,asc');

    const url = `${API_ENDPOINTS.authors}?${queryParams.toString()}`;
    return await makeApiRequest(url);
}

async function fetchAuthor(id) {
    return await makeApiRequest(`${API_ENDPOINTS.authors}/${id}`);
}

async function createAuthor(authorData) {
    return await makeApiRequest(`${API_ENDPOINTS.authors}/`, {
        method: 'POST',
        body: JSON.stringify(authorData)
    });
}

async function updateAuthor(id, authorData) {
    return await makeApiRequest(`${API_ENDPOINTS.authors}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(authorData)
    });
}

async function deleteAuthor(id) {
    return await makeApiRequest(`${API_ENDPOINTS.authors}/${id}`, {
        method: 'DELETE'
    });
}

// UI Functions
function showError(message) {
    authorsError.textContent = message;
}

function clearError() {
    authorsError.textContent = '';
}

function showLoading(loading) {
    authorsLoading.style.display = loading ? 'block' : 'none';
    authorsTable.style.display = loading ? 'none' : 'table';
}

function createAuthorRow(author) {
    return `
        <tr>
            <td>${author.id}</td>
            <td>${escapeHtml(author.name)}</td>
            <td>
                <div class="bio-text" title="${escapeHtml(author.bio || '')}">
                    ${escapeHtml(truncateText(author.bio))}
                </div>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="edit-btn" onclick="editAuthor(${author.id})">Edit</button>
                    <button class="delete-btn" onclick="confirmDeleteAuthor(${author.id}, '${escapeHtml(author.name)}')">Delete</button>
                </div>
            </td>
        </tr>
    `;
}

function createPagination(currentPage, totalPages) {
    if (totalPages <= 1) return '';

    let paginationHtml = '';
    
    paginationHtml += `
        <button onclick="changePage(${currentPage - 1})" ${currentPage === 0 ? 'disabled' : ''}>
            Previous
        </button>
    `;

    paginationHtml += `
        <span class="page-info">Page ${currentPage + 1} of ${totalPages}</span>
    `;

    paginationHtml += `
        <button onclick="changePage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            Next
        </button>
    `;

    return paginationHtml;
}

async function loadAuthors(page = 0) {
    try {
        showLoading(true);
        clearError();
        
        const response = await fetchAuthors(page);
        
        currentPage = response.pageable.pageNumber;
        totalPages = response.totalPages;
        
        if (response.content && response.content.length > 0) {
            authorsTbody.innerHTML = response.content.map(author => createAuthorRow(author)).join('');
        } else {
            authorsTbody.innerHTML = '<tr><td colspan="4" class="no-data">No authors found.</td></tr>';
        }

        pagination.innerHTML = createPagination(currentPage, totalPages);
        
    } catch (error) {
        console.error('Failed to load authors:', error);
        showError(`Failed to load authors: ${error.message}`);
        authorsTbody.innerHTML = '';
    } finally {
        showLoading(false);
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadAuthors(page);
    }
}

// Modal Functions
function showModal(title) {
    modalTitle.textContent = title;
    authorModal.style.display = 'block';
}

function hideModal() {
    authorModal.style.display = 'none';
    clearForm();
}

function clearForm() {
    authorForm.reset();
    idInput.value = '';
}

function addAuthor() {
    clearForm();
    showModal('Add New Author');
}

async function editAuthor(id) {
    try {
        const author = await fetchAuthor(id);
        
        idInput.value = author.id;
        authorNameInput.value = author.name;
        authorBioInput.value = author.bio || '';
        
        showModal('Edit Author');
    } catch (error) {
        console.error('Failed to load author for editing:', error);
        alert('Failed to load author details. Please try again.');
    }
}

function confirmDeleteAuthor(id, name) {
    if (confirm(`Are you sure you want to delete the author "${name}"?\n\nNote: This will also delete all books by this author.`)) {
        deleteAuthorById(id);
    }
}

async function deleteAuthorById(id) {
    try {
        await deleteAuthor(id);
        alert('Author deleted successfully!');
        loadAuthors(currentPage);
    } catch (error) {
        console.error('Failed to delete author:', error);
        alert(`Failed to delete author: ${error.message}`);
    }
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

function goToCirculation() {
    window.location.href = '/admin-circulation.html';
}

// Event Listeners
authorForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const authorData = {
        name: authorNameInput.value.trim(),
        bio: authorBioInput.value.trim() || null
    };

    const submitBtn = authorForm.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
        
        const id = idInput.value;
        
        if (id) {
            await updateAuthor(id, authorData);
            alert('Author updated successfully!');
        } else {
            await createAuthor(authorData);
            alert('Author created successfully!');
        }
        
        hideModal();
        loadAuthors(currentPage);
        
    } catch (error) {
        console.error('Failed to save author:', error);
        alert(`Failed to save author: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Author';
    }
});

// Modal event listeners
addAuthorBtn.addEventListener('click', addAuthor);

document.querySelector('.close').addEventListener('click', hideModal);
document.querySelector('.cancel-btn').addEventListener('click', hideModal);

window.addEventListener('click', (e) => {
    if (e.target === authorModal) {
        hideModal();
    }
});

// Navigation event listeners
logoutBtn.addEventListener('click', logout);
backToBooksBtn.addEventListener('click', goToBooks);
manageBooksBtn.addEventListener('click', goToBooksManagement);
manageUsersBtn.addEventListener('click', goToUsers);
manageCirculationBtn.addEventListener('click', goToCirculation);

// Initialize App
function initApp() {
    if (!checkAdminAccess()) {
        return;
    }
    
    currentUser = getCurrentUser();
    
    if (currentUser) {
        welcomeMessage.textContent = `Welcome, ${currentUser.username || currentUser.email}!`;
    }
    
    loadAuthors();
}

// Start the application
document.addEventListener('DOMContentLoaded', initApp);
