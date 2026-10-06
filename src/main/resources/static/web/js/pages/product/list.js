document.addEventListener('DOMContentLoaded', function() {
    const productGrid = document.getElementById('productGrid');
    const productItems = document.querySelectorAll('.product-item');
    const productCount = document.getElementById('productCount');
    const filterSearch = document.getElementById('filterSearch');
    const sortSelect = document.getElementById('sortSelect');
    const filterDiscount = document.getElementById('filterDiscount');
    const resetBtn = document.getElementById('resetFilters');
    
    const minPriceInput = document.getElementById('minPriceInput');
    const maxPriceInput = document.getElementById('maxPriceInput');
    const applyPriceBtn = document.getElementById('applyPriceBtn');

    const volumeFiltersContainer = document.getElementById('volumeFilters');
    const concentrationFiltersContainer = document.getElementById('concentrationFilters');
    
    const paginationWrapper = document.getElementById('paginationWrapper');
    const paginationContainer = document.getElementById('paginationContainer');

    const ITEMS_PER_PAGE = 12;
    let currentPage = 1;
    let filteredItems = [];

    // --- 1. Dynamic Extraction for Volumes & Concentrations ---
    let allVolumes = new Set();
    let allConcentrations = new Set();

    productItems.forEach(item => {
        const vols = item.dataset.volumes ? item.dataset.volumes.split(',') : [];
        const concs = item.dataset.concentrations ? item.dataset.concentrations.split(',') : [];
        vols.forEach(v => { if (v.trim() !== '') allVolumes.add(v.trim()) });
        concs.forEach(c => { if (c.trim() !== '') allConcentrations.add(c.trim()) });
    });

    // Sort uniquely
    const sortedVolumes = Array.from(allVolumes).sort((a, b) => parseFloat(a) - parseFloat(b));
    const sortedConcentrations = Array.from(allConcentrations).sort((a, b) => parseFloat(a) - parseFloat(b));

    // Render HTML
    if (volumeFiltersContainer) {
        let html = '';
        sortedVolumes.forEach(vol => {
            html += '<label class="filter-checkbox d-block"><input type="checkbox" name="volumeFilter" value="' + vol + '"> ' + vol + 'ml</label>';
        });
        volumeFiltersContainer.innerHTML = html;
    }

    if (concentrationFiltersContainer) {
        let html = '';
        sortedConcentrations.forEach(conc => {
            html += '<label class="filter-checkbox d-block"><input type="checkbox" name="concentrationFilter" value="' + conc + '"> ' + conc + '%</label>';
        });
        concentrationFiltersContainer.innerHTML = html;
    }

    // --- 2. Filtering Logic ---
    function applyFilters() {
        const searchTerm = filterSearch ? filterSearch.value.toLowerCase().trim() : '';
        const selectedCategory = document.querySelector('input[name="categoryFilter"]:checked');
        const selectedBrand = document.querySelector('input[name="brandFilter"]:checked');
        const onlyDiscount = filterDiscount ? filterDiscount.checked : false;

        const categoryVal = selectedCategory ? selectedCategory.value : '';
        const brandVal = selectedBrand ? selectedBrand.value : '';
        
        const minPrice = (minPriceInput && minPriceInput.value) ? parseFloat(minPriceInput.value) : 0;
        const maxPriceStr = (maxPriceInput && maxPriceInput.value) ? maxPriceInput.value.trim() : '';
        const maxPrice = maxPriceStr === '' ? null : parseFloat(maxPriceStr);

        // Get selected dynamic filters
        const selectedVolumes = Array.from(document.querySelectorAll('input[name="volumeFilter"]:checked')).map(cb => cb.value);
        const selectedConcentrations = Array.from(document.querySelectorAll('input[name="concentrationFilter"]:checked')).map(cb => cb.value);

        let items = Array.from(productItems);
        filteredItems = [];

        items.forEach(item => {
            const name = (item.dataset.name || '').toLowerCase();
            const category = item.dataset.category || '';
            const brand = item.dataset.brand || '';
            let price = parseFloat(item.dataset.price) || 0;
            const discount = parseFloat(item.dataset.discount) || 0;
            
            const vols = item.dataset.volumes ? item.dataset.volumes.split(',') : [];
            const concs = item.dataset.concentrations ? item.dataset.concentrations.split(',') : [];

            if (discount > 0) { price = price - discount; }

            let show = true;

            if (searchTerm && !name.includes(searchTerm)) show = false;
            if (categoryVal && category !== categoryVal) show = false;
            if (brandVal && brand !== brandVal) show = false;
            if (minPrice > 0 && price < minPrice) show = false;
            if (maxPrice !== null && price > maxPrice) show = false;
            if (onlyDiscount && (!discount || discount <= 0)) show = false;

            // Volume filtering (OR logic within volume)
            if (show && selectedVolumes.length > 0) {
                const matchVol = selectedVolumes.some(sv => vols.includes(sv));
                if (!matchVol) show = false;
            }

            // Concentration filtering (OR logic within concentration)
            if (show && selectedConcentrations.length > 0) {
                const matchConc = selectedConcentrations.some(sc => concs.includes(sc));
                if (!matchConc) show = false;
            }

            if (show) {
                filteredItems.push(item);
            }
            item.style.display = 'none'; // Hide all initially
        });

        // --- 3. Sorting ---
        const sortValue = sortSelect ? sortSelect.value : 'default';
        filteredItems.sort((a, b) => {
            let priceA = parseFloat(a.dataset.price) || 0;
            let discountA = parseFloat(a.dataset.discount) || 0;
            if (discountA > 0) priceA -= discountA;
            
            let priceB = parseFloat(b.dataset.price) || 0;
            let discountB = parseFloat(b.dataset.discount) || 0;
            if (discountB > 0) priceB -= discountB;

            switch (sortValue) {
                case 'name-asc': return (a.dataset.name || '').localeCompare(b.dataset.name || '', 'vi');
                case 'name-desc': return (b.dataset.name || '').localeCompare(a.dataset.name || '', 'vi');
                case 'price-asc': return priceA - priceB;
                case 'price-desc': return priceB - priceA;
                case 'wishlist-desc': 
                    let wA = parseInt(a.dataset.wishlistCount) || 0;
                    let wB = parseInt(b.dataset.wishlistCount) || 0;
                    return wB - wA;
                case 'rating-desc': 
                    let rA = parseFloat(a.dataset.rating) || 0;
                    let rB = parseFloat(b.dataset.rating) || 0;
                    return rB - rA;
                default: return 0;
            }
        });

        currentPage = 1;
        renderPage();
    }

    // --- 4. Pagination & Rendering ---
    function renderPage() {
        // Hide all items first
        productItems.forEach(item => item.style.display = 'none');

        const totalItems = filteredItems.length;
        if (totalItems === 0) {
            if (productCount) productCount.textContent = 'Không tìm thấy sản phẩm nào';
            if (paginationWrapper) paginationWrapper.style.display = 'none';
            return;
        }

        if (productCount) productCount.textContent = 'Hiển thị ' + totalItems + ' sản phẩm';

        const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
        if (currentPage > totalPages) currentPage = totalPages;

        const startIdx = (currentPage - 1) * ITEMS_PER_PAGE;
        const endIdx = Math.min(startIdx + ITEMS_PER_PAGE, totalItems);

        // Remove all items from DOM and append only current page items to keep order
        if (productGrid) {
            productGrid.innerHTML = '';
            for (let i = startIdx; i < endIdx; i++) {
                const item = filteredItems[i];
                item.style.display = '';
                productGrid.appendChild(item);
            }
        }

        renderPaginationControls(totalPages);
    }

    function renderPaginationControls(totalPages) {
        if (!paginationWrapper || !paginationContainer) return;

        if (totalPages <= 1) {
            paginationWrapper.style.display = 'none';
            return;
        }
        
        paginationWrapper.style.display = 'flex';
        let html = '';

        // Prev Button
        if (currentPage > 1) {
            html += '<li class="page-item"><a class="page-link text-dark fw-bold" href="#" data-page="' + (currentPage - 1) + '"><i class="bi bi-chevron-left"></i></a></li>';
        } else {
            html += '<li class="page-item disabled"><a class="page-link text-muted" href="#"><i class="bi bi-chevron-left"></i></a></li>';
        }

        // Page Numbers
        for (let i = 1; i <= totalPages; i++) {
            if (i === currentPage) {
                html += '<li class="page-item active"><a class="page-link bg-dark border-dark text-white fw-bold" href="#" data-page="' + i + '">' + i + '</a></li>';
            } else {
                html += '<li class="page-item"><a class="page-link text-dark" href="#" data-page="' + i + '">' + i + '</a></li>';
            }
        }

        // Next Button
        if (currentPage < totalPages) {
            html += '<li class="page-item"><a class="page-link text-dark fw-bold" href="#" data-page="' + (currentPage + 1) + '"><i class="bi bi-chevron-right"></i></a></li>';
        } else {
            html += '<li class="page-item disabled"><a class="page-link text-muted" href="#"><i class="bi bi-chevron-right"></i></a></li>';
        }

        paginationContainer.innerHTML = html;
        
        // Add listeners
        paginationContainer.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const page = this.getAttribute('data-page');
                if (page) {
                    currentPage = parseInt(page);
                    renderPage();
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                }
            });
        });
    }

    // --- 5. Event Listeners ---
    if (filterSearch) filterSearch.addEventListener('input', applyFilters);
    if (sortSelect) sortSelect.addEventListener('change', applyFilters);
    if (filterDiscount) filterDiscount.addEventListener('change', applyFilters);
    if (applyPriceBtn) applyPriceBtn.addEventListener('click', applyFilters);

    const viewGrid = document.getElementById('viewGrid');
    const viewList = document.getElementById('viewList');
    if (viewGrid && viewList) {
        viewGrid.addEventListener('click', function() {
            if (productGrid) productGrid.classList.remove('list-view');
            viewGrid.classList.add('active');
            viewList.classList.remove('active');
        });
        viewList.addEventListener('click', function() {
            if (productGrid) productGrid.classList.add('list-view');
            viewList.classList.add('active');
            viewGrid.classList.remove('active');
        });
    }

    document.querySelectorAll('input[name="categoryFilter"], input[name="brandFilter"]').forEach(el => {
        el.addEventListener('change', applyFilters);
    });

    document.querySelectorAll('input[name="volumeFilter"], input[name="concentrationFilter"]').forEach(el => {
        el.addEventListener('change', applyFilters);
    });
    
    // Add event listeners to dynamically generated checkboxes
    if (volumeFiltersContainer) {
        volumeFiltersContainer.addEventListener('change', function(e) {
            if (e.target && e.target.name === 'volumeFilter') applyFilters();
        });
    }
    if (concentrationFiltersContainer) {
        concentrationFiltersContainer.addEventListener('change', function(e) {
            if (e.target && e.target.name === 'concentrationFilter') applyFilters();
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', () => {
            if (filterSearch) filterSearch.value = '';
            document.querySelectorAll('input[name="categoryFilter"]').forEach(e => e.checked = false);
            document.querySelectorAll('input[name="brandFilter"]').forEach(e => e.checked = false);
            document.querySelectorAll('input[name="volumeFilter"]').forEach(e => e.checked = false);
            document.querySelectorAll('input[name="concentrationFilter"]').forEach(e => e.checked = false);
            
            // Check 'Tất cả' by default if exists
            const allCat = document.querySelector('input[name="categoryFilter"][value=""]');
            const allBrand = document.querySelector('input[name="brandFilter"][value=""]');
            if (allCat) allCat.checked = true;
            if (allBrand) allBrand.checked = true;

            if (minPriceInput) minPriceInput.value = '';
            if (maxPriceInput) maxPriceInput.value = '';
            if (filterDiscount) filterDiscount.checked = false;
            if (sortSelect) sortSelect.value = 'default';
            
            applyFilters();
        });
    }

    // Pre-check category/brand from URL if available
    if (window.PRODUCT_DATA) {
        if (window.PRODUCT_DATA.currentCategoryId) {
            const catCb = document.querySelector('input[name="categoryFilter"][value="' + window.PRODUCT_DATA.currentCategoryId + '"]');
            // If the value in DOM is name, we must match name.
            // Wait, in list.html: th:value="${cat.name}". So currentCategoryId in window.PRODUCT_DATA won't match name directly if it's an ID.
            // But list.html has: th:checked="${currentCategoryId != null && currentCategoryId.toString() == cat.id.toString()}"
            // So the server ALREADY checked the correct radio button! We don't need JS to do it!
        }
    }

    // Initial load
    applyFilters();
});

