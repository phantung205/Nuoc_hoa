import sys
path = 'D:/intellij/project/NuocHoa/src/main/resources/templates/admin/pages/user/add.html'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

new_field = """            <div class="mb-4">
                <label class="form-label fw-bold">Điểm Tích Lũy</label>
                <input type="number" class="form-control bg-light" th:field="*{loyaltyPoints}" min="0" value="0">
            </div>

            <h5 class="fw-bold mb-3 border-bottom pb-2 mt-4">Phân Quyền & Trạng Thái</h5>"""

content = content.replace('            <h5 class="fw-bold mb-3 border-bottom pb-2 mt-4">Phân Quyền & Trạng Thái</h5>', new_field)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
