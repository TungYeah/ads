package vn.minhtung.ads.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.minhtung.ads.domain.Category;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.response.category.CreateCategoryDTO;
import vn.minhtung.ads.domain.response.category.GetCategoryByIdDTO;
import vn.minhtung.ads.mapper.CategoryMapper;
import vn.minhtung.ads.repository.CategoryReposity;
import vn.minhtung.ads.util.PaginationUtil;

@Service
public class CategoryService {

    private final CategoryReposity categoryReposity;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryReposity categoryReposity, CategoryMapper categoryMapper) {
        this.categoryReposity = categoryReposity;
        this.categoryMapper = categoryMapper;
    }

    public Category handleCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return categoryReposity.save(category);
    }

    public ResultPageinationDTO getAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> pageCategory = this.categoryReposity.findAll(spec, pageable);
        List<GetCategoryByIdDTO> listCategory = categoryMapper.toGetCategoryByIdDTOs(pageCategory.getContent());
        return PaginationUtil.build(pageCategory, listCategory);
    }

    @Cacheable(value = "categories", key = "#id")
    public GetCategoryByIdDTO getCategoryById(long id) {
        Category category = this.categoryReposity.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return categoryMapper.toGetCategoryByIdDTO(category);
    }

    public Category findById(long id) {
        return this.categoryReposity.findById(id).orElseThrow();
    }

    @CacheEvict(key = "#id")
    public Category updateCategoryById(long id, Category updateCategory) {
        Category currentCategory = this.categoryReposity.findById(id)
                .orElseThrow();
        currentCategory.setName(updateCategory.getName());
        return this.categoryReposity.save(currentCategory);
    }

    public void deleteCategory(long id) {
        if (!categoryReposity.existsById(id)) {
            throw new NoSuchElementException("Ad not found");
        }
        this.categoryReposity.deleteById(id);
    }

}
