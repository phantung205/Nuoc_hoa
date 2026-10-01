$(document).ready(function() {
    $('.datatable').DataTable({
        "language": {
            "sProcessing":   "Đang xử lý...",
            "sLengthMenu":   "Hiển thị _MENU_ dòng",
            "sZeroRecords":  "Không tìm thấy dữ liệu phù hợp",
            "sInfo":         "Đang xem _START_ đến _END_ trong tổng số _TOTAL_ mục",
            "sInfoEmpty":    "Đang xem 0 đến 0 trong tổng số 0 mục",
            "sInfoFiltered": "(được lọc từ _MAX_ mục)",
            "sInfoPostFix":  "",
            "sSearch":       "Tìm kiếm:",
            "sUrl":          "",
            "oPaginate": {
                "sFirst":    "Đầu",
                "sPrevious": "Trước",
                "sNext":     "Tiếp",
                "sLast":     "Cuối"
            }
        },
        "pageLength": 10,
        "ordering": false // Tắt sắp xếp mặc định để tránh thay đổi thứ tự đang có
    });
});
