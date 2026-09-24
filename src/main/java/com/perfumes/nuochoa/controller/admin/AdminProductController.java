package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.dto.ProductRequestDTO;
import com.perfumes.nuochoa.dto.ProductVariantDTO;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public AdminProductController(ProductService productService,
                                  CategoryService categoryService,
                                  BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    /** 1. Hiển thị danh sách tất cả sản phẩm */
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/pages/products/list";
    }

    /** 2. Hiển thị trang Form thêm mới sản phẩm */
    @GetMapping("/add")
    public String showCreateForm(Model model) {
        ProductRequestDTO requestDTO = new ProductRequestDTO();

        // Mặc định tạo sẵn 1 biến thể trống trên Form cho người dùng nhập
        requestDTO.getVariants().add(new ProductVariantDTO());

        model.addAttribute("product", requestDTO);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "admin/pages/products/add";
    }

    /** 3. Xử lý nhận thông tin Form thêm mới sản phẩm */
    @PostMapping("/add")
    public String createProduct(@ModelAttribute("product") ProductRequestDTO requestDTO,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
        return "redirect:/admin/products";
    }

    /** 4. Hiển thị trang Form cập nhật sản phẩm */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Cần DTO để map lên form, hoặc dùng lại ProductRequestDTO
        // Để đơn giản, ta sẽ gọi một hàm trong service để lấy ra ProductRequestDTO
        ProductRequestDTO requestDTO = productService.getProductRequestDTOById(id);
        
        if (requestDTO.getVariants().isEmpty()) {
            requestDTO.getVariants().add(new ProductVariantDTO());
        }

        model.addAttribute("product", requestDTO);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "admin/pages/products/edit";
    }

    /** 5. Xử lý cập nhật sản phẩm */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") ProductRequestDTO requestDTO,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
        return "redirect:/admin/products";
    }

    /** 6. Xóa sản phẩm (Ẩn sản phẩm khỏi hệ thống) */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}