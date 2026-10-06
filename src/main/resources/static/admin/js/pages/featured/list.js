document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.featured-tab');
    const tbody = document.querySelector('.table tbody');
    if (!tbody) return;
    const rows = Array.from(tbody.querySelectorAll('.product-row'));
    
    tabs.forEach(function(tab) {
        tab.addEventListener('click', function() {
            // Active tab
            tabs.forEach(function(t) { t.classList.remove('active'); });
            tab.classList.add('active');
            
            const filter = tab.getAttribute('data-filter');
            
            // 1. Khôi phục thứ tự ban đầu
            rows.forEach(function(row) {
                tbody.appendChild(row);
                row.style.display = ''; // Hiện tất cả theo mặc định
            });
            
            // 2. Xử lý Lọc / Sắp xếp
            if (filter === 'featured') {
                rows.forEach(function(row) {
                    row.style.display = row.getAttribute('data-featured') === 'true' ? '' : 'none';
                });
            } else if (filter === 'top-views') {
                let sortedRows = [...rows].sort(function(a, b) {
                    return parseInt(b.getAttribute('data-views') || '0') - parseInt(a.getAttribute('data-views') || '0');
                });
                sortedRows.forEach(function(row) {
                    tbody.appendChild(row);
                });
            }
        });
    });
});
