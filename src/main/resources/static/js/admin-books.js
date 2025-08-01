// API Configuration
const API_BASE_URL = '/api/v1';
const API_ENDPOINTS = {
    books: `${API_BASE_URL}/books`,
    authors: `${API_BASE_URL}/authors`
};

// Application State
let currentUser = null;
let currentPage = 0;
let totalPages = 0;
let authors = [];

// DOM Elements
const welcomeMessage = document.getElementById('welcome-message');
const logoutBtn = document.getElementById('logout-btn');
const backToBooksBtn = document.getElementById('back-to-books-btn');
const manageUsersBtn = document.getElementById('manage-users-btn');
const manageAuthorsBtn = document.getElementById('manage-authors-btn');
const manageCirculationBtn = document.getElementById('manage-circulation-btn');

const booksTable = document.getElementById('books-table');
const booksTbody = document.getElementById('books-tbody');
const booksLoading = document.getElementById('books-loading');
const booksError = document.getElementById('books-error');
const pagination = document.getElementById('pagination');

const bookModal = document.getElementById('book-modal');
const modalTitle = document.getElementById('modal-title');
const bookForm = document.getElementById('book-form');
const addBookBtn = document.getElementById('add-book-btn');

// Form elements
const bookIdInput = document.getElementById('book-id');
const bookTitleInput = document.getElementById('book-title');
const bookAuthorSelect = document.getElementById('book-author');
const bookYearInput = document.getElementById('book-year');
const bookGenreInput = document.getElementById('book-genre');
const bookStockInput = document.getElementById('book-stock');

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
    
    // Note: We should ideally check the user's role from the JWT token
    // For now, we'll trust the stored user data
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

async function fetchBooks(page = 0, size = 10) {
    const queryParams = new URLSearchParams();
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    queryParams.append('sort', 'id,asc');

    const url = `${API_ENDPOINTS.books}?${queryParams.toString()}`;
    return await makeApiRequest(url);
}

async function fetchAuthors() {
    // For the dropdown, we want all authors, not paginated
    const url = `${API_ENDPOINTS.authors}?size=1000&sort=name,asc`;
    return await makeApiRequest(url);
}

async function createBook(bookData) {
    return await makeApiRequest(`${API_ENDPOINTS.books}/`, {
        method: 'POST',
        body: JSON.stringify(bookData)
    });
}

async function updateBook(id, bookData) {
    return await makeApiRequest(`${API_ENDPOINTS.books}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(bookData)
    });
}

async function deleteBook(id) {
    return await makeApiRequest(`${API_ENDPOINTS.books}/${id}`, {
        method: 'DELETE'
    });
}

// UI Functions
function showError(message) {
    booksError.textContent = message;
}

function clearError() {
    booksError.textContent = '';
}

function showLoading(loading) {
    booksLoading.style.display = loading ? 'block' : 'none';
    booksTable.style.display = loading ? 'none' : 'table';
}

function createBookRow(book) {
    return `
        <tr>
            <td>${book.id}</td>
            <td>${escapeHtml(book.title)}</td>
            <td>${escapeHtml(book.authorName)}</td>
            <td>${book.publishedYear || '-'}</td>
            <td>${escapeHtml(book.genre) || '-'}</td>
            <td>${book.inStock}</td>
            <td>${book.lendCount}</td>
            <td>
                <div class="action-buttons">
                    <button class="edit-btn" onclick="editBook(${book.id})">Edit</button>
                    <button class="delete-btn" onclick="confirmDeleteBook(${book.id}, '${escapeHtml(book.title)}')">Delete</button>
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

async function loadBooks(page = 0) {
    try {
        showLoading(true);
        clearError();
        
        const response = await fetchBooks(page);
        
        currentPage = response.pageable.pageNumber;
        totalPages = response.totalPages;
        
        if (response.content && response.content.length > 0) {
            booksTbody.innerHTML = response.content.map(book => createBookRow(book)).join('');
        } else {
            booksTbody.innerHTML = '<tr><td colspan="8" class="no-data">No books found.</td></tr>';
        }

        pagination.innerHTML = createPagination(currentPage, totalPages);
        
    } catch (error) {
        console.error('Failed to load books:', error);
        showError(`Failed to load books: ${error.message}`);
        booksTbody.innerHTML = '';
    } finally {
        showLoading(false);
    }
}

async function loadAuthors() {
    try {
        const response = await fetchAuthors();
        // Handle both paginated and non-paginated responses
        authors = response.content ? response.content : response;
        
        bookAuthorSelect.innerHTML = '<option value="">Select an author</option>';
        authors.forEach(author => {
            bookAuthorSelect.innerHTML += `<option value="${author.id}">${escapeHtml(author.name)}</option>`;
        });
    } catch (error) {
        console.error('Failed to load authors:', error);
        showError('Failed to load authors. Please refresh the page.');
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadBooks(page);
    }
}

// Modal Functions
function showModal(title) {
    modalTitle.textContent = title;
    bookModal.style.display = 'block';
}

function hideModal() {
    bookModal.style.display = 'none';
    clearForm();
}

function clearForm() {
    bookForm.reset();
    bookIdInput.value = '';
}

function addBook() {
    clearForm();
    showModal('Add New Book');
}

async function editBook(id) {
    try {
        // Fetch the book details from the API
        const response = await makeApiRequest(`${API_ENDPOINTS.books}/${id}`);
        
        bookIdInput.value = response.id;
        bookTitleInput.value = response.title;
        bookYearInput.value = response.publishedYear || '';
        bookGenreInput.value = response.genre || '';
        bookStockInput.value = response.inStock;
        
        // Find author by name
        const author = authors.find(a => a.name === response.authorName);
        if (author) {
            bookAuthorSelect.value = author.id;
        }
        
        showModal('Edit Book');
    } catch (error) {
        console.error('Failed to load book for editing:', error);
        alert('Failed to load book details. Please try again.');
    }
}

function confirmDeleteBook(id, title) {
    if (confirm(`Are you sure you want to delete the book "${title}"?`)) {
        deleteBookById(id);
    }
}

async function deleteBookById(id) {
    try {
        await deleteBook(id);
        alert('Book deleted successfully!');
        loadBooks(currentPage);
    } catch (error) {
        console.error('Failed to delete book:', error);
        alert(`Failed to delete book: ${error.message}`);
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

function goToUsers() {
    window.location.href = '/admin-users.html';
}

function goToAuthors() {
    window.location.href = '/admin-authors.html';
}

function goToCirculation() {
    window.location.href = '/admin-circulation.html';
}

// Event Listeners
bookForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const bookData = {
        title: bookTitleInput.value.trim(),
        authorId: parseInt(bookAuthorSelect.value),
        publishedYear: bookYearInput.value ? parseInt(bookYearInput.value) : null,
        genre: bookGenreInput.value.trim() || null,
        inStock: parseInt(bookStockInput.value)
    };

    const submitBtn = bookForm.querySelector('button[type="submit"]');
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
        
        const bookId = bookIdInput.value;
        
        if (bookId) {
            await updateBook(bookId, bookData);
            alert('Book updated successfully!');
        } else {
            await createBook(bookData);
            alert('Book created successfully!');
        }
        
        hideModal();
        loadBooks(currentPage);
        
    } catch (error) {
        console.error('Failed to save book:', error);
        alert(`Failed to save book: ${error.message}`);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Book';
    }
});

// Modal event listeners
addBookBtn.addEventListener('click', addBook);

document.querySelector('.close').addEventListener('click', hideModal);
document.querySelector('.cancel-btn').addEventListener('click', hideModal);

window.addEventListener('click', (e) => {
    if (e.target === bookModal) {
        hideModal();
    }
});

// Navigation event listeners
logoutBtn.addEventListener('click', logout);
backToBooksBtn.addEventListener('click', goToBooks);
manageUsersBtn.addEventListener('click', goToUsers);
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
    
    loadAuthors();
    loadBooks();
}

// Start the application
document.addEventListener('DOMContentLoaded', initApp);
