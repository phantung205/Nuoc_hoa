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
            // Máº·c Ä‘á»‹nh táº¡o sáºµn 1 biáº¿n thá»ƒ trá»‘ng trÃªn Form cho ngÆ°á»i dÃ¹ng nháº­p
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
            // Cáº§n DTO Ä‘á»ƒ map lÃªn form, hoáº·c dÃ¹ng láº¡i ProductRequestDTO
            // Äá»ƒ Ä‘Æ¡n giáº£n, ta sáº½ gá»i má»™t hÃ m trong service Ä‘á»ƒ láº¥y ra ProductRequestDTO
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

    /** 5. Xá»­ lÃ½ cáº­p nháº­t sáº£n pháº©m */
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

    /** 6. XÃ³a sáº£n pháº©m (áº¨n sáº£n pháº©m khá»i há»‡ thá»‘ng) */
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "C?p nh?t tr?ng th�i s?n ph?m th�nh c�ng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "L?i: " + e.getMessage());
        }
        return "redirect:/staff/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "ÄÃ£ xÃ³a sáº£n pháº©m thÃ nh cÃ´ng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lá»—i khi xÃ³a sáº£n pháº©m: " + e.getMessage());
        }
        return "redirect:/staff/products";
    }
}

