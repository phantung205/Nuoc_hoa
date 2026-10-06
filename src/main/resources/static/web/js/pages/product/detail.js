const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';

// ===== Gallery: Đổi ảnh chính =====
function changeMainImage(element) {
    const imgSrc = element.querySelector('img').src;
    document.getElementById('mainImage').src = imgSrc;
    document.querySelectorAll('.thumbnail-wrapper').forEach(el => el.classList.remove('active'));
    element.classList.add('active');
}

// ===== Cập nhật nút thêm giỏ hàng =====
function updateAddToCartButton(stock) {
    const btn = document.getElementById('addToCartBtn');
    const qtyInput = document.getElementById('quantity');
    if (!btn) return;

    if (stock <= 0) {
        btn.disabled = true;
        btn.classList.remove('btn-gold');
        btn.classList.add('btn-secondary');
        btn.innerHTML = '<i class="bi bi-x-circle me-2"></i> HẾT HÀNG';
        qtyInput.value = 0;
        document.querySelectorAll('.product-qty-group button').forEach(b => b.disabled = true);
    } else {
        btn.disabled = false;
        btn.classList.remove('btn-secondary');
        btn.classList.add('btn-gold');
        btn.innerHTML = '<i class="bi bi-bag-plus me-2"></i> THÊM VÀO GIỎ HÀNG';
        if (parseInt(qtyInput.value) === 0) qtyInput.value = 1;
        document.querySelectorAll('.product-qty-group button').forEach(b => b.disabled = false);
    }
}

// ===== Chọn biến thể =====
document.querySelectorAll('.variant-radio').forEach(radio => {
    radio.addEventListener('change', function() {
        if(this.checked) {
            const discount = parseFloat(document.getElementById('productDiscount').value) || 0;
            const originalPrice = parseFloat(this.getAttribute('data-price'));
            const finalPrice = discount > 0 ? originalPrice - discount : originalPrice;

            document.getElementById('displayPrice').innerText = new Intl.NumberFormat('vi-VN').format(finalPrice) + ' ₫';

            const originalPriceEl = document.getElementById('originalPrice');
            if (originalPriceEl && discount > 0) {
                originalPriceEl.innerText = new Intl.NumberFormat('vi-VN').format(originalPrice) + ' ₫';
            }

            const stock = parseInt(this.getAttribute('data-stock')) || 0;
            document.getElementById('displayStock').innerText = stock;
            updateAddToCartButton(stock);
        }
    });
});

// ===== Tăng giảm số lượng =====
function updateQty(change) {
    const input = document.getElementById('quantity');
    const maxStock = parseInt(document.getElementById('displayStock').innerText);
    if (maxStock <= 0) return;
    let newVal = parseInt(input.value) + change;
    if (newVal >= 1 && newVal <= maxStock) {
        input.value = newVal;
    } else if (newVal > maxStock) {
        alert('Không thể thêm, đã đạt giới hạn tồn kho (' + maxStock + ')');
    }
}

// ===== WISHLIST Toggle =====
function toggleWishlist(btn) {
    const productId = btn.getAttribute('data-product-id');
    fetch('/api/wishlist/toggle?productId=' + productId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        }
    })
    .then(res => {
        if (res.status === 401) {
            window.location.href = '/auth/login';
            return;
        }
        return res.json();
    })
    .then(data => {
        if (!data) return;
        if (data.success) {
            const icon = btn.querySelector('i');
            if (data.wishlisted) {
                btn.classList.add('active');
                icon.className = 'bi bi-heart-fill';
            } else {
                btn.classList.remove('active');
                icon.className = 'bi bi-heart';
            }
            document.getElementById('wishlistCount').textContent = data.count;
        }
    })
    .catch(err => console.error('Wishlist error:', err));
}

// ===== REVIEW Submit =====
const reviewForm = document.getElementById('reviewForm');
if (reviewForm) {
    reviewForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const productId = this.querySelector('[name="productId"]').value;
        const rating = this.querySelector('[name="rating"]:checked')?.value;
        const comment = this.querySelector('[name="comment"]').value;

        if (!rating) {
            alert('Vui lòng chọn số sao đánh giá');
            return;
        }
        if (!comment.trim()) {
            alert('Vui lòng nhập nội dung đánh giá');
            return;
        }

        fetch('/api/reviews/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: `productId=${productId}&rating=${rating}&comment=${encodeURIComponent(comment)}`
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert(data.message || 'Có lỗi xảy ra');
            }
        })
        .catch(err => {
            console.error('Review error:', err);
            alert('Có lỗi xảy ra, vui lòng thử lại');
        });
    });
}

// ===== Init =====
window.addEventListener('DOMContentLoaded', () => {
    const checkedVariant = document.querySelector('.variant-radio:checked');
    if (checkedVariant) {
        updateAddToCartButton(parseInt(checkedVariant.getAttribute('data-stock')) || 0);
    } else {
        updateAddToCartButton(0);
    }
});
