document.addEventListener('DOMContentLoaded', function() {
    let globalMap = null;
    let globalMarker = null;
    let currentAddressInput = null;
    let tempAddress = "";
    let isFromSearch = false;

    // Lắng nghe sự kiện click vào tất cả các ô textarea địa chỉ
    document.querySelectorAll('textarea[name="receiverAddress"]').forEach(input => {
        input.readOnly = true;
        input.style.cursor = 'pointer';
        
        input.addEventListener('click', function() {
            currentAddressInput = this;
            tempAddress = this.value;
            document.getElementById('selected-address-text').textContent = tempAddress || "Vui lòng click trên bản đồ để chọn vị trí...";
            
            // Hiển thị modal bản đồ đè lên trên (không ẩn modal cũ)
            let mapModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('mapPickerModal'));
            mapModal.show();
        });
    });

    // Khi map modal đóng, đảm bảo body vẫn có class modal-open để cuộn được modal bên dưới
    document.getElementById('mapPickerModal').addEventListener('hidden.bs.modal', function () {
        if (document.querySelector('.modal.show')) {
            document.body.classList.add('modal-open');
        }
    });

    // Initialize Map khi modal bật lên
    document.getElementById('mapPickerModal').addEventListener('shown.bs.modal', function () {
        if (!globalMap) {
            globalMap = L.map('global-map', {zoomControl: false}).setView([21.0285, 105.8542], 13);
            L.control.zoom({ position: 'bottomright' }).addTo(globalMap);
            
            // Sử dụng Google Maps Tiles để có giao diện chuẩn Google Map
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
                
                // Reverse geocoding (Thử Nominatim trước, nếu lỗi chuyển sang Photon)
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
                        // Fallback sang Photon API
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

    // Xử lý tìm kiếm địa chỉ
    document.getElementById('btnMapSearch').addEventListener('click', function() {
        let query = document.getElementById('mapSearchInput').value;
        if (!query) return;
        
        document.getElementById('btnMapSearch').innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
        let resultsList = document.getElementById('mapSearchResults');
        
        function renderResults(items) {
            document.getElementById('btnMapSearch').innerHTML = '<i class="bi bi-search"></i>';
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
        
        // Gắn sự kiện click bằng Event Delegation để đảm bảo luôn hoạt động
        if (!resultsList.dataset.bound) {
            resultsList.dataset.bound = "true";
            // Sử dụng pointerdown để bắt cả chuột lẫn cảm ứng siêu nhạy
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
        
        // Tìm kiếm địa chỉ bằng Nominatim trước
        fetch(`https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(query)}&limit=5&countrycodes=vn&accept-language=vi-VN,vi`)
            .then(res => {
                if(!res.ok) throw new Error("Nominatim failed");
                return res.json();
            })
            .then(data => {
                if (data && data.length > 0) {
                    renderResults(data.map(d => ({name: d.display_name, lat: d.lat, lon: d.lon})));
                } else {
                    throw new Error("Nominatim 0 results"); // Trigger fallback
                }
            })
            .catch(err => {
                // Fallback sang Photon API (Tìm kiếm tốt hơn với từ khóa viết tắt/không dấu)
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
                            renderResults([]); // Show Không tìm thấy
                        }
                    })
                    .catch(e => {
                        renderResults([]); // Show Không tìm thấy
                    });
            });
    });

    // Ẩn kết quả khi click ra ngoài
    document.addEventListener('click', function(e) {
        if (!e.target.closest('#mapSearchResults') && !e.target.closest('#mapSearchInput') && !e.target.closest('#btnMapSearch')) {
            let res = document.getElementById('mapSearchResults');
            if(res) res.style.display = 'none';
        }
    });

    // Cho phép ấn Enter để tìm kiếm
    document.getElementById('mapSearchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            document.getElementById('btnMapSearch').click();
        }
    });

    // Nút xác nhận địa chỉ
    document.getElementById('btnConfirmAddress').addEventListener('click', function() {
        if (currentAddressInput && tempAddress) {
            currentAddressInput.value = tempAddress;
        }
        bootstrap.Modal.getInstance(document.getElementById('mapPickerModal')).hide();
    });
});
