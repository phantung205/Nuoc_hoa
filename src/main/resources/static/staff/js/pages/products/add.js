let variantIndex = document.querySelectorAll('.variant-item').length;

function addVariant() {
    const container = document.getElementById('variants-container');
    
    const html = `
        <div class="variant-item border rounded p-3 mb-3 bg-light position-relative">
            <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-2" onclick="removeVariant(this)">
                <i class="bi bi-trash"></i>
            </button>
            
            <div class="row">
                <div class="col-md-4 mb-2">
                    <label class="form-label fw-bold variant-label">Mã SKU</label>
                    <input type="text" class="form-control form-control-sm" name="variants[${variantIndex}].sku" placeholder="Mã SKU...">
                </div>
                <div class="col-md-4 mb-2">
                    <label class="form-label fw-bold variant-label">Dung tích (ml) <span class="text-danger">*</span></label>
                    <input type="number" step="0.1" min="0" class="form-control form-control-sm" name="variants[${variantIndex}].volume" required placeholder="50, 100...">
                </div>
                <div class="col-md-4 mb-2">
                    <label class="form-label fw-bold variant-label">Nồng Độ</label>
                    <select class="form-select form-select-sm" name="variants[${variantIndex}].concentration">
                        <option value="1">EDT</option>
                        <option value="2" selected>EDP</option>
                        <option value="3">Parfum</option>
                    </select>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6 mb-2">
                    <label class="form-label fw-bold variant-label">Giá Bán (VNĐ) <span class="text-danger">*</span></label>
                    <input type="number" min="0" class="form-control form-control-sm" name="variants[${variantIndex}].price" required>
                </div>
                <div class="col-md-6 mb-2">
                    <label class="form-label fw-bold variant-label">Tồn Kho</label>
                    <input type="number" min="0" class="form-control form-control-sm" name="variants[${variantIndex}].stock" value="10">
                </div>
            </div>
        </div>
    `;
    
    container.insertAdjacentHTML('beforeend', html);
    variantIndex++;
}

function removeVariant(btn) {
    btn.closest('.variant-item').remove();
    
    // Re-index all variants to avoid gaps that cause Spring MVC to insert null elements
    const items = document.querySelectorAll('.variant-item');
    items.forEach((item, index) => {
        const inputs = item.querySelectorAll('input, select');
        inputs.forEach(input => {
            if (input.name && input.name.startsWith('variants[')) {
                input.name = input.name.replace(/variants\[\d+\]/, `variants[${index}]`);
            }
        });
    });
    
    // Update the global variantIndex for the next addition
    variantIndex = items.length;
}

function updateImageIndices() {
    const items = document.querySelectorAll('.image-item');
    const removeBtns = document.querySelectorAll('.image-remove-btn');
    
    // Block deletion if only 1 item left
    if (items.length <= 1) {
        removeBtns.forEach(btn => btn.style.display = 'none');
    } else {
        removeBtns.forEach(btn => btn.style.display = 'block');
    }
    
    // Update radio values sequentially
    const radios = document.querySelectorAll('.primary-image-radio');
    radios.forEach((radio, index) => {
        radio.value = index;
    });

    // Ensure at least one is checked
    if (!document.querySelector('.primary-image-radio:checked') && radios.length > 0) {
        radios[0].checked = true;
    }
}

function addImage() {
    const container = document.getElementById('images-container');
    const html = `
        <div class="image-item border rounded p-3 mb-3 bg-light position-relative">
            <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-2 image-remove-btn" onclick="removeImage(this)">
                <i class="bi bi-trash"></i>
            </button>
            <div class="row align-items-center">
                <div class="col-md-8">
                    <label class="form-label fw-bold variant-label">Chọn file ảnh mới <span class="text-danger">*</span></label>
                    <input type="file" class="form-control form-control-sm" name="imageFiles" accept="image/*" required>
                </div>
                <div class="col-md-4 mt-3 mt-md-0">
                    <div class="form-check mt-md-4">
                        <input class="form-check-input primary-image-radio" type="radio" name="primaryImageIndex" value="0">
                        <label class="form-check-label fw-bold text-success">Ảnh Đại Diện</label>
                    </div>
                </div>
            </div>
        </div>
    `;
    container.insertAdjacentHTML('beforeend', html);
    updateImageIndices();
}

function removeImage(btn) {
    btn.closest('.image-item').remove();
    updateImageIndices();
}

// Call once on load
document.addEventListener("DOMContentLoaded", function() {
    updateImageIndices();
});
