package vn.minhtung.ads.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.minhtung.ads.domain.Category;

import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO.Meta;
import vn.minhtung.ads.domain.response.ad.GetAdByIdDTO;
import vn.minhtung.ads.domain.response.category.CreateCategoryDTO;
import vn.minhtung.ads.domain.response.category.GetCategoryByIdDTO;
import vn.minhtung.ads.repository.CategoryReposity;

@Service
public class CategoryService {

    private final CategoryReposity categoryReposity;

    public CategoryService(CategoryReposity categoryReposity) {
        this.categoryReposity = categoryReposity;
    }

    public Category handleCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return categoryReposity.save(category);
    }

    public ResultPageinationDTO getAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> pageCategory = this.categoryReposity.findAll(spec, pageable);
        ResultPageinationDTO rs = new ResultPageinationDTO();
        Meta mt = new Meta();

        mt.setPage(pageCategory.getNumber() + 1);
        mt.setPageSize(pageCategory.getSize());
        mt.setPages(pageCategory.getTotalPages());
        mt.setTotal(pageCategory.getTotalElements());
        rs.setMeta(mt);

        List<GetCategoryByIdDTO> listCategory = pageCategory.getContent()
                .stream().map(category -> {
                    List<GetAdByIdDTO> ads = category.getAds()
                            .stream()
                            .map(ad -> new GetAdByIdDTO(
                                    ad.getId(),
                                    ad.getTitle(),
                                    ad.getDescription(),
                                    ad.getImageUrl(),
                                    ad.getTargetUrl(),
                                    ad.getStartDate(),
                                    ad.getEndDate()))
                            .collect(Collectors.toList());

                    return new GetCategoryByIdDTO(
                            category.getId(),
                            category.getName(),
                            ads);
                }).collect(Collectors.toList());

        rs.setResult(listCategory);
        return rs;
    }

    public GetCategoryByIdDTO getCategoryById(long id) {
        Category category = this.categoryReposity.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        List<GetAdByIdDTO> ads = category.getAds()
                .stream()
                .map(ad -> new GetAdByIdDTO(
                        ad.getId(),
                        ad.getTitle(),
                        ad.getDescription(),
                        ad.getImageUrl(),
                        ad.getTargetUrl(),
                        ad.getStartDate(),
                        ad.getEndDate()))
                .collect(Collectors.toList());

        return new GetCategoryByIdDTO(category.getId(), category.getName(), ads);
    }

    public Category findById(long id) {
        return this.categoryReposity.findById(id).orElseThrow();
    }

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
