// Tự động submit form khi input số lượng thay đổi (nếu user tự sửa input)
document.querySelectorAll('input[name="quantity"]').forEach(input => {
    input.addEventListener('change', function() {
        if (this.value >= 1) {
            this.closest('form').submit();
        }
    });
});

// Select voucher from modal
document.querySelectorAll('.select-voucher-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.getElementById('voucherCodeInput').value = this.getAttribute('data-code');
        document.getElementById('applyVoucherBtn').click(); // Tự động áp dụng
    });
});

// Search voucher
const searchInput = document.getElementById('voucherSearchInput');
if (searchInput) {
    searchInput.addEventListener('input', function() {
        const keyword = this.value.toLowerCase().trim();
        const items = document.querySelectorAll('.voucher-item');
        let hasVisible = false;
        
        items.forEach(item => {
            const code = item.getAttribute('data-code').toLowerCase();
            if (code.includes(keyword)) {
                item.classList.remove('d-none');
                hasVisible = true;
            } else {
                item.classList.add('d-none');
            }
        });
        
        const msg = document.getElementById('noVoucherMsg');
        if (msg) {
            if (hasVisible) msg.classList.add('d-none');
            else msg.classList.remove('d-none');
        }
    });
}
