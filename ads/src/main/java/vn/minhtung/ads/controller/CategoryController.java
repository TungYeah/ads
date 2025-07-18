package vn.minhtung.ads.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.dto.CreateCategoryDTO;
import vn.minhtung.ads.domain.dto.GetCategoryByIdDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.service.CategoryService;
import vn.minhtung.ads.util.errors.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CreateCategoryDTO dto) {
        Category createdCategory = categoryService.handleCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping("/categories")
    public ResponseEntity<ResultPageinationDTO> getAllCategories(@Filter Specification<Category> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.categoryService.getAllCategories(spec, pageable));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<GetCategoryByIdDTO> getCategoryById(@PathVariable long id) throws IdInvalidException {
        GetCategoryByIdDTO categoryById = this.categoryService.getCategoryById(id);
        if (categoryById == null) {
            throw new IdInvalidException("Khong tim thay id Category" + id);
        }
        GetCategoryByIdDTO category = this.categoryService.getCategoryById(id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(category);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategoryById(@PathVariable long id, @RequestBody Category category)
            throws IdInvalidException {
        Category categoryById = this.categoryService.findById(id);
        if (categoryById == null) {
            throw new IdInvalidException("Không tìm thấy id Category: " + id);
        }
        Category updatedCategory = this.categoryService.updateCategoryById(id, category);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteAdById(@PathVariable("id") long id) throws IdInvalidException {
        Category category = this.categoryService.findById(id);
        if (category == null) {
            throw new IdInvalidException("User khong ton tai Id" + id);
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(null);
    }
}
