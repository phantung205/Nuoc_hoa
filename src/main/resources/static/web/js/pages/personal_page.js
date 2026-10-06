function removeFromWishlist(btn) {
    const productId = btn.getAttribute('data-product-id');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';

    fetch('/api/wishlist/toggle?productId=' + productId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Animate and remove card
            const card = btn.closest('.col');
            card.style.transition = 'all 0.4s ease';
            card.style.opacity = '0';
            card.style.transform = 'scale(0.8)';
            setTimeout(() => {
                card.remove();
                // Check if empty
                const remaining = document.querySelectorAll('.product-card').length;
                if (remaining === 0) {
                    location.reload();
                }
            }, 400);
        }
    })
    .catch(err => console.error('Error:', err));
}
