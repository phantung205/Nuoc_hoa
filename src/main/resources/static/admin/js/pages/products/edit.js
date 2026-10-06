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
                    <input type="text" class="form-control form-control-sm" name="variants[${variantIndex}].sku">
                </div>
                <div class="col-md-4 mb-2">
                    <label class="form-label fw-bold variant-label">Dung tích (ml) <span class="text-danger">*</span></label>
                    <input type="number" step="0.1" min="0" class="form-control form-control-sm" name="variants[${variantIndex}].volume" required>
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
    updateRadioIndices();
});

const fileInput = document.getElementById('fileInput');
const previewContainer = document.getElementById('preview-container');
let selectedFiles = [];

if (fileInput) {
    fileInput.addEventListener('change', function(e) {
        const files = Array.from(e.target.files);
        selectedFiles = selectedFiles.concat(files);
        updateFileInput();
        renderNewFilePreviews();
        updateRadioIndices();
    });
}

function updateFileInput() {
    const dt = new DataTransfer();
    selectedFiles.forEach(file => dt.items.add(file));
    fileInput.files = dt.files;
}

function removeNewFile(index) {
    selectedFiles.splice(index, 1);
    updateFileInput();
    renderNewFilePreviews();
    updateRadioIndices();
}

function removeExistingImage(btn) {
    btn.closest('.preview-item').remove();
    updateRadioIndices();
}

function renderNewFilePreviews() {
    // Clear dynamically added new previews
    document.querySelectorAll('.preview-item[data-type="new"]').forEach(el => el.remove());
    
    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const html = `
                <div class="preview-item position-relative border rounded p-2 text-center bg-white shadow-sm" style="width: 120px;" data-type="new" data-index="${index}">
                    <img src="${e.target.result}" style="height: 80px; width: 100px; object-fit: cover; border-radius: 4px;">
                    <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1 p-0" style="width: 22px; height: 22px; line-height: 1; border-radius: 50%;" onclick="removeNewFile(${index})">
                        <i class="bi bi-x"></i>
                    </button>
                    <div class="mt-2 form-check d-flex justify-content-center">
                        <input type="radio" name="primaryImageIndex" class="form-check-input primary-radio me-1" id="new_radio_${index}" onchange="updateRadioIndices()">
                        <label class="form-check-label small text-success fw-bold" for="new_radio_${index}">Ảnh chính</label>
                    </div>
                </div>
            `;
            previewContainer.insertAdjacentHTML('beforeend', html);
            updateRadioIndices();
        }
        reader.readAsDataURL(file);
    });
}

function updateRadioIndices() {
    const items = document.querySelectorAll('.preview-item');
    let hasChecked = false;
    items.forEach((item, index) => {
        const radio = item.querySelector('.primary-radio');
        if (radio) {
            radio.value = index;
            if (radio.checked) hasChecked = true;
        }
    });
    
    // Ensure at least the first one is checked if none is checked
    if (!hasChecked && items.length > 0) {
        const firstRadio = items[0].querySelector('.primary-radio');
        if(firstRadio) firstRadio.checked = true;
    }
}
