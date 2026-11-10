package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Category tree lookups for search: a category plus all of its subcategories, at any depth,
 * so searching a parent category returns the products filed under its children.
 */
@Component
@RequiredArgsConstructor
public class CategoryHierarchy {

    private final CategoryRepository categoryRepository;

    /**
     * The category id and the ids of every category below it (one query per level).
     * Unknown ids are returned as given, so the search simply finds nothing. Cycles in parent links are ignored.
     */
    public Set<String> withDescendants(String categoryId) {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(categoryId.trim());
        Set<String> level = Set.copyOf(ids);
        while (!level.isEmpty()) {
            List<Category> children = categoryRepository.findByParentCategoryIdIn(level);
            Set<String> next = new LinkedHashSet<>();
            for (Category child : children) {
                if (ids.add(child.getId())) {
                    next.add(child.getId());
                }
            }
            level = next;
        }
        return ids;
    }
}
