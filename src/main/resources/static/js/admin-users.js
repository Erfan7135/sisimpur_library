// API Configuration
const API_BASE_URL = '/api/v1';
const API_ENDPOINTS = {
    users: `${API_BASE_URL}/users`
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
const manageAuthorsBtn = document.getElementById('manage-authors-btn');
const manageCirculationBtn = document.getElementById('manage-circulation-btn');

const usersTable = document.getElementById('users-table');
const usersTbody = document.getElementById('users-tbody');
const usersLoading = document.getElementById('users-loading');
const usersError = document.getElementById('users-error');
const pagination = document.getElementById('pagination');

const userModal = document.getElementById('user-modal');
const modalTitle = document.getElementById('modal-title');
const userForm = document.getElementById('user-form');
const addUserBtn = document.getElementById('add-user-btn');

// Form elements
const userIdInput = document.getElementById('user-id');
const userNameInput = document.getElementById('user-name');
const userEmailInput = document.getElementById('user-email');
const userPasswordInput = document.getElementById('user-password');

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

async function fetchUsers(page = 0, size = 10) {
    const queryParams = new URLSearchParams();
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    queryParams.append('sort', 'id,asc');

    const url = `${API_ENDPOINTS.users}?${queryParams.toString()}`;
    return await makeApiRequest(url);
}

async function fetchUser(id) {
    return await makeApiRequest(`${API_ENDPOINTS.users}/${id}`);
}

async function createUser(userData) {
    return await makeApiRequest(`${API_ENDPOINTS.users}/`, {
        method: 'POST',
        body: JSON.stringify(userData)
    });
}

async function updateUser(id, userData) {
    return await makeApiRequest(`${API_ENDPOINTS.users}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(userData)
    });
}

async function deleteUser(id) {
    return await makeApiRequest(`${API_ENDPOINTS.users}/${id}`, {
        method: 'DELETE'
    });
}

// UI Functions
function showError(message) {
    usersError.textContent = message;
}

function clearError() {
    usersError.textContent = '';
}

function showLoading(loading) {
    usersLoading.style.display = loading ? 'block' : 'none';
    usersTable.style.display = loading ? 'none' : 'table';
}

function createUserRow(user) {
    
    return `
        <tr>
            <td>${user.id}</td>
            <td>${escapeHtml(user.name)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td>
                <div class="action-buttons">
                    <button class="edit-btn" onclick="editUser(${user.id})">Edit</button>
                    <button class="delete-btn" onclick="confirmDeleteUser(${user.id}, '${escapeHtml(user.name)}')">Delete</button>
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

async function loadUsers(page = 0) {
    try {
        showLoading(true);
        clearError();
        
        const response = await fetchUsers(page);
        
        currentPage = response.pageable.pageNumber;
        totalPages = response.totalPages;
        
        if (response.content && response.content.length > 0) {
            usersTbody.innerHTML = response.content.map(user => createUserRow(user)).join('');
        } else {
            usersTbody.innerHTML = '<tr><td colspan="5" class="no-data">No users found.</td></tr>';
        }

        pagination.innerHTML = createPagination(currentPage, totalPages);
        
    } catch (error) {
        console.error('Failed to load users:', error);
        showError(`Failed to load users: ${error.message}`);
        usersTbody.innerHTML = '';
    } finally {
        showLoading(false);
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadUsers(page);
    }
}

// Modal Functions
function showModal(title) {
    modalTitle.textContent = title;
    userModal.style.display = 'block';
}

function hideModal() {
    userModal.style.display = 'none';
    clearForm();
}

function clearForm() {
    userForm.reset();
    userIdInput.value = '';
    updatePasswordField(false);
}

function updatePasswordField(isEditing) {
    const helpText = userPasswordInput.nextElementSibling;
    if (isEditing) {
        userPasswordInput.removeAttribute('required');
        userPasswordInput.placeholder = 'Leave empty to keep current password';
        helpText.textContent = 'Leave empty to keep current password. Otherwise, enter new password (5-20 characters).';
    } else {
        userPasswordInput.setAttribute('required', '');
        userPasswordInput.placeholder = 'Enter password';
        helpText.textContent = 'Password is required for new users (5-20 characters). Leave empty to keep current password when editing.';
    }
}

function addUser() {
    clearForm();
    updatePasswordField(false);
    showModal('Add New User');
}

async function editUser(id) {
    try {
        const user = await fetchUser(id);
        
        userIdInput.value = user.id;
        userNameInput.value = user.name;
        userEmailInput.value = user.email;
        
        updatePasswordField(true);
        showModal('Edit User');
    } catch (error) {
        console.error('Failed to load user for editing:', error);
        alert('Failed to load user details. Please try again.');
    }
}

function confirmDeleteUser(id, name) {
    const currentUserId = getCurrentUser()?.userId;
    
    if (id === currentUserId) {
        alert('You cannot delete your own account.');
        return;
    }
    
    if (confirm(`Are you sure you want to delete the user "${name}"?`)) {
        deleteUserById(id);
    }
}

async function deleteUserById(id) {
    try {
        await deleteUser(id);
        alert('User deleted successfully!');
        loadUsers(currentPage);
    } catch (error) {
        console.error('Failed to delete user:', error);
        alert(`Failed to delete user: ${error.message}`);
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

function goToAuthors() {
    window.location.href = '/admin-authors.html';
}

function goToCirculation() {
    window.location.href = '/admin-circulation.html';
}

// Event Listeners
userForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const userData = {
        name: userNameInput.value.trim(),
        email: userEmailInput.value.trim()
    };

    // Only include password if it's provided
    if (userPasswordInput.value.trim()) {
        userData.password = userPasswordInput.value.trim();
    }

    const submitBtn = userForm.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
        
        const userId = userIdInput.value;
        
        if (userId) {
            await updateUser(userId, userData);
            alert('User updated successfully!');
        } else {
            if (!userData.password) {
                alert('Password is required for new users.');
                return;
            }
            await createUser(userData);
            alert('User created successfully!');
        }
        
        hideModal();
        loadUsers(currentPage);
        
    } catch (error) {
        console.error('Failed to save user:', error);
        alert(`Failed to save user: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save User';
    }
});

// Modal event listeners
addUserBtn.addEventListener('click', addUser);

document.querySelector('.close').addEventListener('click', hideModal);
document.querySelector('.cancel-btn').addEventListener('click', hideModal);

window.addEventListener('click', (e) => {
    if (e.target === userModal) {
        hideModal();
    }
});

// Navigation event listeners
logoutBtn.addEventListener('click', logout);
backToBooksBtn.addEventListener('click', goToBooks);
manageBooksBtn.addEventListener('click', goToBooksManagement);
manageAuthorsBtn.addEventListener('click', goToAuthors);
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
    
    loadUsers();
}

// Start the application
document.addEventListener('DOMContentLoaded', initApp);
