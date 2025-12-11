package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.CategoryAttribute;
import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.NotFoundException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.CategoryAttributeRepository;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.CategoryRequest;
import com.autoadda.apis.part.response.CategoryResponse;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.validator.PartsDataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {


    public static final String PERFORM_CATEGORY_INPUT_VALIDATION = "Perform Category input validation";
    public static final String SUCCESS = "success";
    private final CategoryRepository categoryRepository;

    private final MediaFileRepository mediaFileRepository;

    private final PartsDataValidator partsDataValidator;

    private final ProductRepository productRepository;

    private final CategoryAttributeRepository categoryAttributeRepository;

    private String prepareAttributeUrl(String categoryId) {
        return "http://localhost:8097/api/v1/categoryattributes/categoryid/" + categoryId;
    }

    private Category prepareCategory(CategoryRequest request) {
        Category category = Category.builder ( ).id ( request.getId ( ) ).name ( request.getName ( ) ).code ( request.getCode ( ) )
                .parentCategoryId ( request.getParentCategoryId ( ) )
                .images ( new ArrayList<> ( ) )
                .build ( );

        if ( request.getImageIds ( ) != null && ! request.getImageIds ( ).isEmpty ( ) ) {
            List<MediaFile> allById = mediaFileRepository.findAllById ( request.getImageIds ( ) );
            category.getImages ( ).addAll ( allById );
        }

        return category;
    }

    private List<MediaFile> takeOutMediaFileFromRequestImageIds(List<Long> imageIds) {
        List<MediaFile> mediaFiles = new ArrayList<> ( );
        for (Long imageId : imageIds) {
            mediaFiles.add ( mediaFileRepository.findById ( imageId )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "media File not found" ) ) );
        }
        return mediaFiles;
    }

    /**
     * Retrieves all categories from the repository and maps them to a response
     * object.
     *
     * @return GenericResponse containing a list of CategoryResponse objects.
     */
    @Transactional(readOnly = true)
    public GenericResponse getTopLevelCategories() {
        List<Category> categories = categoryRepository.findByParentCategoryIdIsNull ( );

        List<CategoryResponse> responseBody = categories.stream ( )
                .map ( category ->
                        CategoryResponse.builder ( )
                                .name ( category.getName ( ) )
                                .id ( category.getId ( ) )
                                .attributeUrl ( prepareAttributeUrl ( category.getId ( ) ) )
                                .code ( category.getCode ( ) )
                                .parentCategoryId ( category.getParentCategoryId ( ) )
                                .subCategories ( categoryRepository.findByParentCategoryId ( category.getId ( ) ).stream ( ).map (
                                        cat -> CategoryResponse
                                                .builder ( )
                                                .name ( cat.getName ( ) )
                                                .id ( cat.getId ( ) )
                                                .parentCategoryId ( cat.getParentCategoryId ( ) )
                                                .attributeUrl ( prepareAttributeUrl ( cat.getId ( ) ) )
                                                .images ( cat.getImages ( ) )
                                                .code ( cat.getCode ( ) )
                                                .build ( ) ).toList ( ) )
                                .images ( category.getImages ( ) )
                                .build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message (SUCCESS).status ( SUCCESS ).build ( );
    }

    /**
     * Retrieves subcategories by parent category ID and maps them to a response
     * object.
     *
     * @param parentId The ID of the parent category.
     * @return GenericResponse containing a list of CategoryResponse objects for
     * subcategories.
     */
    @Transactional(readOnly = true)
    public GenericResponse getSubCategoriesByParentId(String parentId) {
        List<Category> categories = categoryRepository.findByParentCategoryId ( parentId );

        List<CategoryResponse> responseBody = categories.stream ( )
                .map ( category -> CategoryResponse
                        .builder ( )
                        .id ( category.getId ( ) )
                        .name ( category.getName ( ) )
                        .attributeUrl ( prepareAttributeUrl ( category.getId ( ) ) )
                        .code ( category.getCode ( ) )
                        .parentCategoryId ( category.getParentCategoryId ( ) )
                        .images ( category.getImages ( ) )
                        .subCategories(categoryRepository
                                .findByParentCategoryId(category.getId())
                                .stream().map(cat->
                                        CategoryResponse.builder()
                                                .name(cat.getName())
                                                .id(cat.getId())
                                                .attributeUrl(prepareAttributeUrl(cat.getId()))
                                                .code(cat.getCode())
                                                .parentCategoryId(cat.getParentCategoryId())
                                                .images(cat.getImages())
                                                .subCategories(categoryRepository
                                                        .findByParentCategoryId(category.getId())
                                                        .stream().map(cat2->
                                                                CategoryResponse.builder()
                                                                        .name(cat2.getName())
                                                                        .id(cat2.getId())
                                                                        .attributeUrl(prepareAttributeUrl(cat2.getId()))
                                                                        .code(cat2.getCode())
                                                                        .parentCategoryId(cat2.getParentCategoryId())
                                                                        .images(cat2.getImages())
                                                                        .build())
                                                        .toList())
                                                .build())
                                .toList())
                        .build ( ) )
                .toList ( );

        return GenericResponse.builder ( ).body ( responseBody ).code ( "200" ).message ( SUCCESS ).status ( SUCCESS ).build ( );
    }

    /**
     * Retrieves a category by its ID and maps it to a response object.
     *
     * @param name The ID of the category to retrieve.
     * @return GenericResponse containing the CategoryResponse object.
     * @throws NotFoundException if the category with the given ID does not exist.
     */
    @Transactional(readOnly = true)
    public GenericResponse getCategoryByName(String name) {
        log.info (PERFORM_CATEGORY_INPUT_VALIDATION);
        partsDataValidator.validateCategoryName ( name );

        Category category = categoryRepository.findByName ( name ).orElseThrow ( () -> new NotFoundException ( "Category not found for name: " + name ) );


        return GenericResponse.builder ( ).body ( category ).code ( "200" ).message ( "Successfully retrieved category: " + name )
                .status ( SUCCESS ).build ( );
    }

    /**
     * Creates a new category based on the provided request object.
     *
     * @param categoryRequestList List of CategoryRequest objects containing the
     *                            details of the categories to be created.
     * @return GenericResponse containing the created categories.
     */
    @Transactional
    public GenericResponse createCategory(List<CategoryRequest> categoryRequestList) {
        List<Category> categoryList = new ArrayList<> ( );

        for (CategoryRequest categoryRequest : categoryRequestList) {
            log.info ( PERFORM_CATEGORY_INPUT_VALIDATION );
            partsDataValidator.isCategoryAlreadyExists ( categoryRequest.getName ( ) );
            Category category = categoryRepository.save ( prepareCategory ( categoryRequest ) );
            log.info ( "Category created - {}", categoryRequest.toString ( ) );
            categoryList.add ( category );
        }

        return GenericResponse.builder ( ).body ( categoryList ).code ( "201" ).message ( "Successfully created category" )
                .status ( SUCCESS ).build ( );
    }

    /**
     * Updates an existing category based on the provided request object.
     *
     * @param request CategoryRequest object containing the updated details.
     * @return GenericResponse containing the updated category.
     * @throws NotFoundException if the category with the specified ID does not exist.
     */
    @Transactional
    public GenericResponse updateCategory(CategoryRequest request) throws NotFoundException {
        log.info ( PERFORM_CATEGORY_INPUT_VALIDATION );
        partsDataValidator.validateCategoryId ( request.getId ( ) );

        Category category = categoryRepository.findById ( request.getId ( ) )
                .orElseThrow ( () -> new NotFoundException ( "Category not found for ID: " + request.getId ( ) ) );

        // Only update fields that are provided (not null)
        if ( request.getName ( ) != null ) {
            category.setName ( request.getName ( ) );
        }
        if ( request.getCode ( ) != null ) {
            category.setCode ( request.getCode ( ) );
        }
        if ( request.getParentCategoryId ( ) != null ) {
            category.setParentCategoryId ( request.getParentCategoryId ( ) );
        }

        // Update images only if imageIds are provided
        if ( request.getImageIds ( ) != null && ! request.getImageIds ( ).isEmpty ( ) ) {
            List<MediaFile> images = takeOutMediaFileFromRequestImageIds ( request.getImageIds ( ) );
            category.getImages ( ).clear ( );
            category.getImages ( ).addAll ( images );
        }

        categoryRepository.save ( category );

        return GenericResponse.builder ( ).body ( "Category has been Updated" ).code ( "200" )
                .message ( "Successfully updated category: " + request.getId ( ) )
                .status ( SUCCESS ).build ( );
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id The ID of the category to delete.
     * @return GenericResponse containing the deletion status.
     * @throws NotFoundException if the category with the specified ID does not exist.
     */
    @Transactional
    public GenericResponse deleteCategoryById(String id) throws NotFoundException {
        log.info ( PERFORM_CATEGORY_INPUT_VALIDATION );
        partsDataValidator.validateCategoryId ( id );

        Category category = categoryRepository.findById ( id )
                .orElseThrow ( () -> new NotFoundException ( "Category not found for ID: " + id ) );

        // Validate category has no references before deletion
        validateCategoryCanBeDeleted(id);

        categoryRepository.delete ( category );

        return GenericResponse.builder ( ).body ( "Category has been deleted" ).code ( "200" ).message ( "Successfully deleted" )
                .status ( SUCCESS ).build ( );
    }

    public CategoryResponse getCategoryById(String categoryId) {
        Category category = categoryRepository.findById ( categoryId ).orElseThrow ( () -> new ResourceNotFoundException ( "category not found" ) );
        return CategoryResponse.builder()
                .id ( category.getId ( ) )
                .name ( category.getName ( ) )
                .attributeUrl ( prepareAttributeUrl ( category.getId ( ) ) )
                .code ( category.getCode ( ) )
                .parentCategoryId ( category.getParentCategoryId ( ) )
                .images ( category.getImages ( ) )
                .build ( );

    }

    /**
     * Validates that a category can be safely deleted by checking for references
     * @param categoryId the category ID to check
     * @throws ConflictException if the category has references
     */
    private void validateCategoryCanBeDeleted(String categoryId) {
        // Check for product references
        List<Product> products = productRepository.findByCategoryId(categoryId);

        // Check for subcategory references
        List<Category> subCategories = categoryRepository.findByParentCategoryId(categoryId);

        // Check for category attribute references
        List<CategoryAttribute> attributes = categoryAttributeRepository.findByCategoryId(categoryId);

        // If any references exist, prevent deletion
        if (!products.isEmpty() || !subCategories.isEmpty() || !attributes.isEmpty()) {
            String message = buildReferenceErrorMessage(products.size(), subCategories.size(), attributes.size());
            log.warn("Cannot delete category {}: {}", categoryId, message);
            throw new ConflictException(message);
        }
    }

    /**
     * Builds a detailed error message listing all references
     */
    private String buildReferenceErrorMessage(int productCount, int subcategoryCount, int attributeCount) {
        List<String> references = new ArrayList<>();

        if (productCount > 0) {
            references.add(productCount + " product" + (productCount > 1 ? "s" : ""));
        }
        if (subcategoryCount > 0) {
            references.add(subcategoryCount + " subcategory(ies)");
        }
        if (attributeCount > 0) {
            references.add(attributeCount + " category attribute(s)");
        }

        return "Cannot delete category. It is referenced by " +
               String.join(" and ", references) +
               ". Please delete or reassign these items first.";
    }
}
