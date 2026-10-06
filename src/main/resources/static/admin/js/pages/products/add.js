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

// Call once on load
document.addEventListener("DOMContentLoaded", function() {
    // Init variant logic if needed
});
