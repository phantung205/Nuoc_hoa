document.addEventListener("DOMContentLoaded", function() {

    /* ==============================================================
       1. QR CODE PAYMENT POLLING LOGIC
       ============================================================== */
    if (window.QR_ORDER_ID && window.QR_ORDER_ID !== 0) {
        let orderId = window.QR_ORDER_ID;
        let statusUrl = "/api/orders/status/" + orderId;
        let successUrl = "/orders";
        let statusElement = document.getElementById('payment-status');

        if (statusElement) {
            // Polling má»—i 5 giÃ¢y Ä‘á»ƒ kiá»ƒm tra tráº¡ng thÃ¡i thanh toÃ¡n
            let checkInterval = setInterval(function() {
                fetch(statusUrl, { headers: { 'Accept': 'application/json' } })
                    .then(response => response.json())
                    .then(data => {
                        if (data.status === 'PAID' || data.status === 'COMPLETED' || data.status === 'DELIVERED') {
                            clearInterval(checkInterval);
                            statusElement.className = 'alert alert-success fw-bold fs-5 border-success';
                            statusElement.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Thanh toÃ¡n thÃ nh cÃ´ng! Äang chuyá»ƒn hÆ°á»›ng...';
                            setTimeout(() => window.location.href = successUrl, 2000);
                        }
                    })
                    .catch(err => console.log('Äang kiá»ƒm tra thanh toÃ¡n...'));
            }, 5000);
        }
    }

    /* ==============================================================
       2. CHECKOUT FORM LOGIC (ADDRESS & POINTS)
       ============================================================== */
    const newAddressForm = document.getElementById('newAddressForm');
    
    if (newAddressForm) { // Chá»‰ cháº¡y náº¿u Ä‘ang á»Ÿ trang checkout
        // Xá»­ lÃ½ áº©n/hiá»‡n form Ä‘á»‹a chá»‰
        const addressRadios = document.querySelectorAll('.address-radio');
        const requiredInputs = newAddressForm.querySelectorAll('input[required], textarea[required]');

        function toggleAddressForm() {
            const checkedRadio = document.querySelector('.address-radio:checked');
            if (checkedRadio && checkedRadio.value === '') {
                // Chá»n Ä‘á»‹a chá»‰ khÃ¡c
                newAddressForm.classList.remove('d-none');
                requiredInputs.forEach(input => input.setAttribute('required', 'required'));
            } else {
                // Chá»n Ä‘á»‹a chá»‰ cÃ³ sáºµn
                newAddressForm.classList.add('d-none');
                requiredInputs.forEach(input => input.removeAttribute('required'));
            }
        }

        addressRadios.forEach(radio => {
            radio.addEventListener('change', toggleAddressForm);
        });

        // Initialize on load
        toggleAddressForm();
        
        // Xá»­ lÃ½ Ä‘iá»ƒm tÃ­ch lÅ©y
        const pointsInput = document.getElementById('pointsToUse');
        const btnMaxPoints = document.getElementById('btnMaxPoints');
        const pointsError = document.getElementById('pointsError');
        const discountRow = document.getElementById('discountRow');
        const discountDisplay = document.getElementById('discountDisplay');
        const finalTotalDisplay = document.getElementById('finalTotalDisplay');
        const btnSubmitOrder = document.getElementById('btnSubmitOrder');
        
        const baseTotalAmountEl = document.getElementById('baseTotalAmount');
        const maxPointsValueEl = document.getElementById('maxPointsValue');
        
        if (pointsInput && baseTotalAmountEl && maxPointsValueEl) {
            const baseTotalAmount = parseFloat(baseTotalAmountEl.value) || 0;
            const maxPointsValue = parseInt(maxPointsValueEl.value) || 0;
            
            function formatMoney(amount) {
                return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + ' ₫';
            }
            
            function updatePointsLogic() {
                let points = parseInt(pointsInput.value) || 0;
                
                // Validate points limit
                if (points < 0) points = 0;
                if (points > maxPointsValue) points = maxPointsValue;
                
                // Calculate max usable points that don't exceed order total
                const maxPointsForThisOrder = Math.floor(baseTotalAmount / 10000) * 100;
                if (points > maxPointsForThisOrder) {
                    points = maxPointsForThisOrder;
                }
                
                // Check if multiple of 100
                if (points % 100 !== 0 && points !== 0) {
                    pointsError.classList.remove('d-none');
                    if (btnSubmitOrder) btnSubmitOrder.disabled = true;
                } else {
                    pointsError.classList.add('d-none');
                    if (btnSubmitOrder) btnSubmitOrder.disabled = false;
                    
                    // Update displays
                    const discount = (points / 100) * 10000;
                    const finalTotal = baseTotalAmount - discount;
                    
                    if (discount > 0) {
                        discountRow.classList.remove('d-none');
                        discountDisplay.textContent = '-' + formatMoney(discount);
                    } else {
                        discountRow.classList.add('d-none');
                    }
                    
                    finalTotalDisplay.textContent = formatMoney(finalTotal);
                }
            }
            
            pointsInput.addEventListener('input', updatePointsLogic);
            
            if (btnMaxPoints) {
                btnMaxPoints.addEventListener('click', function() {
                    // Find max points (multiple of 100)
                    let maxUsable = Math.floor(maxPointsValue / 100) * 100;
                    let maxPointsForThisOrder = Math.floor(baseTotalAmount / 10000) * 100;
                    
                    let targetPoints = Math.min(maxUsable, maxPointsForThisOrder);
                    pointsInput.value = targetPoints;
                    updatePointsLogic();
                });
            }
        }
    }
});

function confirmCancel() {
    if (confirm('Bạn có chắc chắn muốn quay lại và không thanh toán đơn hàng này?')) {
        const form = document.getElementById('cancelForm');
        if (form) form.submit();
    }
}
