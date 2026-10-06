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


/* ==============================================================
   3. MAP PICKER LOGIC
   ============================================================== */
document.addEventListener('DOMContentLoaded', function() {
    let globalMap = null;
    let globalMarker = null;
    let currentAddressInput = null;
    let tempAddress = "";
    let isFromSearch = false;

    const receiverInput = document.getElementById('receiverAddress');
    if (receiverInput) {
        receiverInput.addEventListener('click', function() {
            currentAddressInput = this;
            tempAddress = this.value;
            document.getElementById('selected-address-text').textContent = tempAddress || "Vui lòng click trên bản đồ để chọn vị trí...";
            
            // Hiện modal map
            let mapModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('mapPickerModal'));
            mapModal.show();
        });
    }

    const mapPickerModal = document.getElementById('mapPickerModal');
    if (mapPickerModal) {
        mapPickerModal.addEventListener('shown.bs.modal', function () {
            if (!globalMap) {
                globalMap = L.map('global-map', {zoomControl: false}).setView([21.0285, 105.8542], 13);
                L.control.zoom({ position: 'bottomright' }).addTo(globalMap);
                
                L.tileLayer('http://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',{
                    maxZoom: 20,
                    subdomains:['mt0','mt1','mt2','mt3'],
                    attribution: '&copy; Google Maps'
                }).addTo(globalMap);
    
                globalMap.on('click', function(e) {
                    if (isFromSearch) return; // Bỏ qua click này vì nó bắt nguồn từ search
    
                    if (globalMarker) globalMap.removeLayer(globalMarker);
                    globalMarker = L.marker(e.latlng).addTo(globalMap);
                    
                    document.getElementById('selected-address-text').innerHTML = '<span class="spinner-border spinner-border-sm text-primary" role="status" aria-hidden="true"></span> Đang tải địa chỉ...';
                    
                    fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${e.latlng.lat}&lon=${e.latlng.lng}&accept-language=vi-VN,vi`)
                        .then(res => {
                            if (!res.ok) throw new Error("Nominatim error");
                            return res.json();
                        })
                        .then(data => {
                            if(data && data.display_name) {
                                tempAddress = data.display_name;
                                document.getElementById('selected-address-text').textContent = tempAddress;
                            } else {
                                throw new Error("No display_name");
                            }
                        })
                        .catch(err => {
                            fetch(`https://photon.komoot.io/reverse?lon=${e.latlng.lng}&lat=${e.latlng.lat}`)
                                .then(r => r.json())
                                .then(d => {
                                    if (d.features && d.features.length > 0) {
                                        let p = d.features[0].properties;
                                        let arr = [];
                                        if (p.name) arr.push(p.name);
                                        if (p.housenumber) arr.push(p.housenumber);
                                        if (p.street) arr.push(p.street);
                                        if (p.district) arr.push(p.district);
                                        if (p.city) arr.push(p.city);
                                        tempAddress = arr.join(", ");
                                        document.getElementById('selected-address-text').textContent = tempAddress;
                                    } else {
                                        document.getElementById('selected-address-text').textContent = "Không thể lấy chi tiết địa chỉ.";
                                    }
                                })
                                .catch(err2 => {
                                    document.getElementById('selected-address-text').textContent = "Lỗi mạng hoặc bị chặn kết nối API.";
                                });
                        });
                });
            }
            globalMap.invalidateSize();
        });
    }

    const btnMapSearch = document.getElementById('btnMapSearch');
    if (btnMapSearch) {
        btnMapSearch.addEventListener('click', function() {
            let query = document.getElementById('mapSearchInput').value;
            if (!query) return;
            
            this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
            let resultsList = document.getElementById('mapSearchResults');
            
            function renderResults(items) {
                btnMapSearch.innerHTML = '<i class="bi bi-search"></i>';
                resultsList.innerHTML = '';
                
                if (items && items.length > 0) {
                    items.forEach(item => {
                        let li = document.createElement('li');
                        li.className = 'list-group-item list-group-item-action text-truncate map-result-item';
                        li.style.cursor = 'pointer';
                        li.title = item.name;
                        li.textContent = item.name;
                        li.dataset.lat = item.lat;
                        li.dataset.lon = item.lon;
                        li.dataset.name = item.name;
                        resultsList.appendChild(li);
                    });
                    resultsList.style.display = 'block';
                } else {
                    let li = document.createElement('li');
                    li.className = 'list-group-item text-muted';
                    li.textContent = 'Không tìm thấy kết quả.';
                    resultsList.appendChild(li);
                    resultsList.style.display = 'block';
                }
            }
            
            if (!resultsList.dataset.bound) {
                resultsList.dataset.bound = "true";
                resultsList.addEventListener('pointerdown', function(e) {
                    let li = e.target.closest('li.map-result-item');
                    if (!li) return;
                    
                    e.preventDefault();
                    isFromSearch = true;
                    setTimeout(() => isFromSearch = false, 500);
                    
                    try {
                        let lat = parseFloat(li.dataset.lat);
                        let lng = parseFloat(li.dataset.lon);
                        let name = li.dataset.name;
                        
                        globalMap.setView([lat, lng], 16, { animate: false });
                        
                        if (globalMarker) globalMap.removeLayer(globalMarker);
                        globalMarker = L.marker([lat, lng]).addTo(globalMap);
                        
                        tempAddress = name;
                        document.getElementById('selected-address-text').textContent = tempAddress;
                        
                        resultsList.style.display = 'none';
                    } catch (err) {
                        console.error(err);
                        alert("Lỗi bản đồ: " + err.message);
                    }
                });
            }
            
            fetch(`https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(query)}&limit=5&countrycodes=vn&accept-language=vi-VN,vi`)
                .then(res => {
                    if(!res.ok) throw new Error("Nominatim failed");
                    return res.json();
                })
                .then(data => {
                    if (data && data.length > 0) {
                        renderResults(data.map(d => ({name: d.display_name, lat: d.lat, lon: d.lon})));
                    } else {
                        throw new Error("Nominatim 0 results");
                    }
                })
                .catch(err => {
                    fetch(`https://photon.komoot.io/api/?q=${encodeURIComponent(query)}&limit=5`)
                        .then(r => r.json())
                        .then(d => {
                            if (d.features && d.features.length > 0) {
                                renderResults(d.features.map(f => {
                                    let p = f.properties;
                                    let arr = [];
                                    if (p.name) arr.push(p.name);
                                    if (p.housenumber) arr.push(p.housenumber);
                                    if (p.street) arr.push(p.street);
                                    if (p.district) arr.push(p.district);
                                    if (p.city) arr.push(p.city);
                                    return {
                                        name: arr.join(", ") || p.name,
                                        lat: f.geometry.coordinates[1],
                                        lon: f.geometry.coordinates[0]
                                    };
                                }));
                            } else {
                                renderResults([]);
                            }
                        })
                        .catch(e => {
                            renderResults([]);
                        });
                });
        });
    }

    document.addEventListener('click', function(e) {
        if (!e.target.closest('#mapSearchResults') && !e.target.closest('#mapSearchInput') && !e.target.closest('#btnMapSearch')) {
            let res = document.getElementById('mapSearchResults');
            if(res) res.style.display = 'none';
        }
    });

    const mapSearchInput = document.getElementById('mapSearchInput');
    if (mapSearchInput) {
        mapSearchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (btnMapSearch) btnMapSearch.click();
            }
        });
    }

    const btnConfirmAddress = document.getElementById('btnConfirmAddress');
    if (btnConfirmAddress) {
        btnConfirmAddress.addEventListener('click', function() {
            if (currentAddressInput && tempAddress) {
                currentAddressInput.value = tempAddress;
            }
            bootstrap.Modal.getInstance(document.getElementById('mapPickerModal')).hide();
        });
    }
});

/* ==============================================================
   4. VOUCHER SEARCH & SUBMIT LOGIC
   ============================================================== */
document.addEventListener('DOMContentLoaded', function() {
    var searchInput = document.getElementById('searchVoucher');
    if(searchInput) {
        searchInput.addEventListener('keyup', function() {
            var term = this.value.toLowerCase();
            var items = document.querySelectorAll('.voucher-item');
            for (var i = 0; i < items.length; i++) {
                var code = items[i].getAttribute('data-code').toLowerCase();
                items[i].style.display = code.indexOf(term) >= 0 ? 'block' : 'none';
            }
        });
    }
});

function submitVoucher(code) {
    if (code) {
        document.getElementById('hiddenVoucherCode').value = code;
    } else {
        var val = document.getElementById('visibleVoucherCode').value;
        document.getElementById('hiddenVoucherCode').value = val;
    }
    document.getElementById('applyVoucherForm').submit();
}
