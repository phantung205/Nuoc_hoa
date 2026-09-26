document.addEventListener('DOMContentLoaded', function() {
    const productGrid = document.getElementById('productGrid');
    const productItems = document.querySelectorAll('.product-item');
    const productCount = document.getElementById('productCount');
    const noResults = document.getElementById('noResults');
    const filterSearch = document.getElementById('filterSearch');
    const sortSelect = document.getElementById('sortSelect');
    const filterDiscount = document.getElementById('filterDiscount');
    const resetBtn = document.getElementById('resetFilters');
    const viewGridBtn = document.getElementById('viewGrid');
    const viewListBtn = document.getElementById('viewList');
    
    // Custom price range
    const minPriceInput = document.getElementById('minPriceInput');
    const maxPriceInput = document.getElementById('maxPriceInput');
    const applyPriceBtn = document.getElementById('applyPriceBtn');

    const brandFiltersContainer = document.getElementById('brandFilters');

    function applyFilters() {
        const searchTerm = filterSearch.value.toLowerCase().trim();
        const selectedCategory = document.querySelector('input[name="categoryFilter"]:checked');
        const selectedBrand = document.querySelector('input[name="brandFilter"]:checked');
        const onlyDiscount = filterDiscount.checked;

        const categoryVal = selectedCategory ? selectedCategory.value : '';
        const brandVal = selectedBrand ? selectedBrand.value : '';
        
        const minPrice = parseFloat(minPriceInput.value) || 0;
        let maxPriceStr = maxPriceInput.value.trim();
        const maxPrice = maxPriceStr === '' ? null : parseFloat(maxPriceStr);

        let visibleCount = 0;
        let items = Array.from(productItems);

        items.forEach(item => {
            const name = (item.dataset.name || '').toLowerCase();
            const category = item.dataset.category || '';
            const brand = item.dataset.brand || '';
            let price = parseFloat(item.dataset.price) || 0;
            const discount = parseFloat(item.dataset.discount) || 0;

            // If product has discount, filter by discounted price
            if (discount > 0) {
                price = price - discount;
            }

            let show = true;

            // Search filter
            if (searchTerm && !name.includes(searchTerm)) show = false;

            // Category filter
            if (categoryVal && category !== categoryVal) show = false;

            // Brand filter
            if (brandVal && brand !== brandVal) show = false;

            // Price filter
            if (minPrice > 0 && price < minPrice) show = false;
            if (maxPrice !== null && price > maxPrice) show = false;

            // Discount filter
            if (onlyDiscount && (!discount || discount <= 0)) show = false;

            item.style.display = show ? '' : 'none';
            if (show) visibleCount++;
        });

        // Sort visible items
        const sortValue = sortSelect.value;
        let sortedItems = items.filter(i => i.style.display !== 'none');

        sortedItems.sort((a, b) => {
            let priceA = parseFloat(a.dataset.price) || 0;
            let discountA = parseFloat(a.dataset.discount) || 0;
            if (discountA > 0) priceA -= discountA;
            
            let priceB = parseFloat(b.dataset.price) || 0;
            let discountB = parseFloat(b.dataset.discount) || 0;
            if (discountB > 0) priceB -= discountB;

            switch (sortValue) {
                case 'name-asc':
                    return (a.dataset.name || '').localeCompare(b.dataset.name || '', 'vi');
                case 'name-desc':
                    return (b.dataset.name || '').localeCompare(a.dataset.name || '', 'vi');
                case 'price-asc':
                    return priceA - priceB;
                case 'price-desc':
                    return priceB - priceA;
                default:
                    return 0;
            }
        });

        // Reorder in DOM
        sortedItems.forEach(item => productGrid.appendChild(item));
        // Also append hidden items
        items.filter(i => i.style.display === 'none').forEach(item => productGrid.appendChild(item));

        productCount.textContent = 'Hiển thị ' + visibleCount + ' / ' + productItems.length + ' sản phẩm';
        noResults.style.display = visibleCount === 0 ? 'block' : 'none';
    }

    // Event listeners
    filterSearch.addEventListener('input', applyFilters);
    sortSelect.addEventListener('change', applyFilters);
    filterDiscount.addEventListener('change', applyFilters);

    document.querySelectorAll('input[name="categoryFilter"]').forEach(r => r.addEventListener('change', applyFilters));
    document.querySelectorAll('input[name="brandFilter"]').forEach(r => r.addEventListener('change', applyFilters));
    
    applyPriceBtn.addEventListener('click', applyFilters);
    
    // Allow enter key on price inputs
    minPriceInput.addEventListener('keypress', function(e) { if(e.key === 'Enter') applyFilters(); });
    maxPriceInput.addEventListener('keypress', function(e) { if(e.key === 'Enter') applyFilters(); });

    // Also listen for dynamically added brand filter radios
    brandFiltersContainer.addEventListener('change', applyFilters);

    // Reset
    resetBtn.addEventListener('click', function() {
        filterSearch.value = '';
        const catAll = document.querySelector('input[name="categoryFilter"][value=""]');
        if (catAll) catAll.checked = true;
        const brandAll = document.querySelector('input[name="brandFilter"][value=""]');
        if (brandAll) brandAll.checked = true;
        
        minPriceInput.value = '';
        maxPriceInput.value = '';
        
        filterDiscount.checked = false;
        sortSelect.value = 'default';
        applyFilters();
    });

    // View toggle
    viewGridBtn.addEventListener('click', function() {
        productGrid.classList.remove('list-view');
        viewGridBtn.classList.add('active');
        viewListBtn.classList.remove('active');
        productItems.forEach(item => {
            item.className = item.className.replace(/col-lg-\d+/g, 'col-lg-4');
        });
    });

    viewListBtn.addEventListener('click', function() {
        productGrid.classList.add('list-view');
        viewListBtn.classList.add('active');
        viewGridBtn.classList.remove('active');
    });

    // Initialize
    applyFilters();
});
