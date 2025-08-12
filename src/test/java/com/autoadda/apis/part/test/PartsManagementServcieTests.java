package com.autoadda.apis.part.test;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.repository.CategoryRepository;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PartsManagementServcieTests {

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void contextLoads() {
	}

	// JUnit test for Categories
	@Test
	@Order(1)
	@Rollback(value = false)
	public void saveCategoryTest() {

		Category category = Category.builder().id("cat-1001").name("ac filters").code("ac").build();

		categoryRepository.save(category);

		Assertions.assertThat(category.getId()).isEqualTo("cat-1001");
	}

	@Test
	@Order(2)
	public void getListOfCategoriesTest() {

		List<Category> categories = categoryRepository.findAll();

		Assertions.assertThat(categories.size()).isGreaterThan(0);

	}

	@Test
	@Order(3)
	@Rollback(value = false)
	public void updateCategoryTest() {

		Category category = categoryRepository.findById("cat-1001").get();

		category.setName("Engine");

		Category categoryUpdated = categoryRepository.save(category);

		Assertions.assertThat(categoryUpdated.getName()).isEqualTo("Engine");

	}

	@Test
	@Order(4)
	@Rollback(value = false)
	public void deleteCategoryTest() {

		Category category = categoryRepository.findById("cat-1001").get();

		categoryRepository.delete(category);

		Category deletedCategory = null;

		Optional<Category> optionalCategory = categoryRepository.findById("cat-1001");

		if (optionalCategory.isPresent()) {
			deletedCategory = optionalCategory.get();
		}

		Assertions.assertThat(deletedCategory).isNull();
	}
}
