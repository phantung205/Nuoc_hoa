package com.perfumes.nuochoa.controller.staff;

import com.perfumes.nuochoa.dto.ProductRequestDTO;
import com.perfumes.nuochoa.dto.ProductVariantDTO;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/products")
public class StaffProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public StaffProductController(ProductService productService,
                                  CategoryService categoryService,
                                  BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "staff/pages/products/list";
    }

    @GetMapping("/add")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("product")) {
            ProductRequestDTO requestDTO = new ProductRequestDTO();
            // Mặc định tạo sẵn 1 biến thể trống trên Form cho người dùng nhập
            requestDTO.getVariants().add(new ProductVariantDTO());
            model.addAttribute("product", requestDTO);
        }
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "staff/pages/products/add";
    }

    @PostMapping("/add")
    public String createProduct(@Valid @ModelAttribute("product") ProductRequestDTO requestDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            return "staff/pages/products/add";
        }

        try {
            productService.createProduct(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            return "staff/pages/products/add";
        }
        return "redirect:/staff/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("product")) {
            // Cần DTO để map lên form, hoặc dùng lại ProductRequestDTO
            // Để đơn giản, ta sẽ gọi một hàm trong service để lấy ra ProductRequestDTO
            ProductRequestDTO requestDTO = productService.getProductRequestDTOById(id);

            if (requestDTO.getVariants().isEmpty()) {
                requestDTO.getVariants().add(new ProductVariantDTO());
            }
            model.addAttribute("product", requestDTO);
        }
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "staff/pages/products/edit";
    }

    /** 5. Xử lý cập nhật sản phẩm */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") ProductRequestDTO requestDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            return "staff/pages/products/edit";
        }

        try {
            productService.updateProduct(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            return "staff/pages/products/edit";
        }
        return "redirect:/staff/products";
    }

    /** 6. Bật/tắt trạng thái sản phẩm (Ẩn/hiện sản phẩm khỏi hệ thống) */
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/staff/products";
    }
}