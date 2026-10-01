document.addEventListener('DOMContentLoaded', function() {
    var chartData = window.CHART_DATA;
    if (!chartData) return;

    // === Parse data ===
    var revenue7Days = chartData.revenue7Days || [];
    var revenue12Months = chartData.revenue12Months || [];
    var orderStatus = chartData.orderStatus || {};
    var categories = chartData.categories || {};
    var topProducts = chartData.topProducts || [];
    var currentYear = chartData.currentYear || new Date().getFullYear();

    // === Color palette ===
    var goldColor = '#D4AF37';

    var colors = ['#D4AF37', '#3b82f6', '#10b981', '#ef4444', '#8b5cf6', 
                  '#f59e0b', '#ec4899', '#14b8a6', '#6366f1', '#06b6d4'];

    var statusColors = {
        'PENDING': '#ffc107',
        'CONFIRMED': '#0d6efd',
        'SHIPPING': '#0dcaf0',
        'DELIVERED': '#198754',
        'CANCELLED': '#dc3545'
    };

    var statusLabels = {
        'PENDING': 'Chờ xử lý',
        'CONFIRMED': 'Đã xác nhận',
        'SHIPPING': 'Đang giao',
        'DELIVERED': 'Đã giao',
        'CANCELLED': 'Đã hủy'
    };

    // === Helper: format money ===
    function formatMoney(value) {
        if (value == null || isNaN(value)) return '0';
        return new Intl.NumberFormat('vi-VN').format(Math.round(value));
    }

    // === Chart defaults ===
    Chart.defaults.font.family = "'Montserrat', sans-serif";
    Chart.defaults.font.size = 12;
    Chart.defaults.color = '#6c757d';

    // ============================================================
    // 1. DOANH THU 7 NGÀY GẦN ĐÂY (Line Chart with gradient)
    // ============================================================
    (function() {
        var ctx = document.getElementById('revenue7DaysChart');
        if (!ctx) return;

        var gradient = ctx.getContext('2d').createLinearGradient(0, 0, 0, 300);
        gradient.addColorStop(0, 'rgba(212, 175, 55, 0.3)');
        gradient.addColorStop(1, 'rgba(212, 175, 55, 0.02)');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: revenue7Days.map(function(d) { return d.date; }),
                datasets: [{
                    label: 'Doanh thu (đ)',
                    data: revenue7Days.map(function(d) { return d.revenue; }),
                    borderColor: goldColor,
                    backgroundColor: gradient,
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#fff',
                    pointBorderColor: goldColor,
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 8,
                    pointHoverBackgroundColor: goldColor
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#212529',
                        titleFont: { weight: 'bold' },
                        callbacks: {
                            label: function(context) {
                                return 'Doanh thu: ' + formatMoney(context.raw) + ' đ';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.04)' },
                        ticks: {
                            callback: function(value) {
                                if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
                                if (value >= 1000) return (value / 1000).toFixed(0) + 'K';
                                return value;
                            }
                        }
                    },
                    x: {
                        grid: { display: false }
                    }
                }
            }
        });
    })();

    // ============================================================
    // 2. ĐƠN HÀNG THEO TRẠNG THÁI (Doughnut Chart)
    // ============================================================
    (function() {
        var ctx = document.getElementById('orderStatusChart');
        if (!ctx) return;

        var statusKeys = Object.keys(orderStatus);
        var statusData = statusKeys.map(function(k) { return orderStatus[k]; });
        var statusBgColors = statusKeys.map(function(k) { return statusColors[k] || '#ccc'; });
        var statusLabelArr = statusKeys.map(function(k) { return statusLabels[k] || k; });

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: statusLabelArr,
                datasets: [{
                    data: statusData,
                    backgroundColor: statusBgColors,
                    borderWidth: 3,
                    borderColor: '#fff',
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 16,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            font: { size: 12, weight: '500' }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#212529',
                        callbacks: {
                            label: function(context) {
                                var total = context.dataset.data.reduce(function(a, b) { return a + b; }, 0);
                                var percent = total > 0 ? ((context.raw / total) * 100).toFixed(1) : 0;
                                return context.label + ': ' + context.raw + ' (' + percent + '%)';
                            }
                        }
                    }
                }
            }
        });
    })();

    // ============================================================
    // 3. DOANH THU THEO THÁNG (Bar Chart with year filter)
    // ============================================================
    var monthlyChart;
    (function() {
        var ctx = document.getElementById('revenueMonthlyChart');
        if (!ctx) return;

        function renderMonthlyChart(data) {
            if (monthlyChart) monthlyChart.destroy();

            var gradient = ctx.getContext('2d').createLinearGradient(0, 0, 0, 300);
            gradient.addColorStop(0, 'rgba(212, 175, 55, 0.7)');
            gradient.addColorStop(1, 'rgba(212, 175, 55, 0.1)');

            monthlyChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.map(function(d) { return d.month; }),
                    datasets: [{
                        label: 'Doanh thu (đ)',
                        data: data.map(function(d) { return d.revenue; }),
                        backgroundColor: gradient,
                        borderColor: goldColor,
                        borderWidth: 1.5,
                        borderRadius: 6,
                        borderSkipped: false,
                        maxBarThickness: 45
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: '#212529',
                            callbacks: {
                                label: function(context) {
                                    return 'Doanh thu: ' + formatMoney(context.raw) + ' đ';
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: { color: 'rgba(0,0,0,0.04)' },
                            ticks: {
                                callback: function(value) {
                                    if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
                                    if (value >= 1000) return (value / 1000).toFixed(0) + 'K';
                                    return value;
                                }
                            }
                        },
                        x: {
                            grid: { display: false }
                        }
                    }
                }
            });
        }

        // Initial render with 12 months data
        renderMonthlyChart(revenue12Months);

        // Year filter
        var yearSelect = document.getElementById('yearFilter');
        if (yearSelect) {
            for (var y = currentYear; y >= currentYear - 3; y--) {
                var opt = document.createElement('option');
                opt.value = y;
                opt.textContent = y;
                if (y === currentYear) opt.selected = true;
                yearSelect.appendChild(opt);
            }

            yearSelect.addEventListener('change', function() {
                var selectedYear = parseInt(this.value);
                fetch('/admin/api/revenue-by-year?year=' + selectedYear)
                    .then(function(response) { return response.json(); })
                    .then(function(data) {
                        renderMonthlyChart(data);
                    })
                    .catch(function(err) {
                        console.error('Error fetching revenue data:', err);
                    });
            });
        }
    })();

    // ============================================================
    // 4. SẢN PHẨM THEO DANH MỤC (Polar Area Chart)
    // ============================================================
    (function() {
        var ctx = document.getElementById('categoryChart');
        if (!ctx) return;

        var catKeys = Object.keys(categories);
        var catValues = catKeys.map(function(k) { return categories[k]; });

        new Chart(ctx, {
            type: 'polarArea',
            data: {
                labels: catKeys,
                datasets: [{
                    data: catValues,
                    backgroundColor: catKeys.map(function(_, i) {
                        return colors[i % colors.length] + '88';
                    }),
                    borderColor: catKeys.map(function(_, i) {
                        return colors[i % colors.length];
                    }),
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 12,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            font: { size: 11 }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#212529',
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.raw + ' sản phẩm';
                            }
                        }
                    }
                }
            }
        });
    })();

    // ============================================================
    // 5. TOP SẢN PHẨM BÁN CHẠY (Horizontal Bar Chart)
    // ============================================================
    (function() {
        var ctx = document.getElementById('topProductsChart');
        if (!ctx) return;

        var productNames = topProducts.map(function(p) { return p.name; });
        var productQty = topProducts.map(function(p) { return p.quantity; });

        if (productNames.length === 0) {
            // Show empty message
            ctx.parentElement.innerHTML = '<div class="text-center text-muted py-5"><i class="bi bi-inbox fs-1 d-block mb-2"></i>Chưa có dữ liệu sản phẩm bán chạy</div>';
            return;
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: productNames,
                datasets: [{
                    label: 'Số lượng bán',
                    data: productQty,
                    backgroundColor: productNames.map(function(_, i) {
                        return colors[i % colors.length] + 'CC';
                    }),
                    borderColor: productNames.map(function(_, i) {
                        return colors[i % colors.length];
                    }),
                    borderWidth: 1.5,
                    borderRadius: 6,
                    maxBarThickness: 30
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#212529',
                        callbacks: {
                            label: function(context) {
                                return 'Đã bán: ' + context.raw + ' sản phẩm';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.04)' },
                        ticks: { stepSize: 1 }
                    },
                    y: {
                        grid: { display: false },
                        ticks: {
                            font: { size: 11, weight: '500' }
                        }
                    }
                }
            }
        });
    })();
});
