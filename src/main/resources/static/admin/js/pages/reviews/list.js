function showReviewDetail(button) {
    const reviewer = button.getAttribute('data-reviewer');
    const product = button.getAttribute('data-product');
    const rating = parseInt(button.getAttribute('data-rating'));
    const comment = button.getAttribute('data-comment');
    const date = button.getAttribute('data-date');

    document.getElementById('modalReviewerName').innerHTML = '<i class="bi bi-person-circle me-1"></i>' + reviewer;
    document.getElementById('modalProductName').textContent = product;
    document.getElementById('modalReviewDate').textContent = date;
    document.getElementById('modalReviewComment').textContent = comment;

    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            starsHtml += '<i class="bi bi-star-fill me-1"></i>';
        } else {
            starsHtml += '<i class="bi bi-star me-1"></i>';
        }
    }
    document.getElementById('modalReviewStars').innerHTML = starsHtml;
}
