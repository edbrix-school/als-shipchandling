package com.asg.shipchandling.stockcategory.repository;


import com.asg.shipchandling.stockcategory.entity.StockCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockCategoryRepository extends JpaRepository<StockCategoryMaster, Long>,
        JpaSpecificationExecutor<StockCategoryMaster> {

    boolean existsByCategoryPoid(Long categoryPoid);

    Optional<StockCategoryMaster> findByCategoryPoid(Long categoryPoid);

    Optional<StockCategoryMaster> findByCategoryPoidAndGroupPoid(Long categoryPoid, Long groupPoid);
    Optional<StockCategoryMaster> findByCategoryNameAndGroupPoid(String categoryName, Long groupPoid);

    boolean existsByCategoryCodeIgnoreCaseAndGroupPoid(String categoryCode, Long groupPoid);

    boolean existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
            String categoryCode, Long groupPoid, Long categoryPoid);

    boolean existsByCategoryNameIgnoreCaseAndGroupPoid(String categoryName, Long groupPoid);

    boolean existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
            String categoryName, Long groupPoid, Long categoryPoid);

    @Query("SELECT COUNT(c) FROM StockCategoryMaster c WHERE c.parentCategoryPoid = :categoryPoid")
    Long countChildrenByParentCategoryPoid(@Param("categoryPoid") Long categoryPoid);

    @Query("SELECT COUNT(s) FROM StockMaster s WHERE s.categoryPoid = :categoryPoid")
    Long countStockItemsByCategoryPoid(@Param("categoryPoid") Long categoryPoid);

    @Query("SELECT c FROM StockCategoryMaster c WHERE c.parentCategoryPoid IS NULL " +
            "AND c.groupPoid = :groupPoid AND (c.deleted IS NULL OR c.deleted = 'N')")
    List<StockCategoryMaster> findParentCategoriesByGroupPoid(@Param("groupPoid") Long groupPoid);

    @Query("SELECT c FROM StockCategoryMaster c WHERE c.parentCategoryPoid = :parentCategoryPoid " +
            "AND c.groupPoid = :groupPoid AND (c.deleted IS NULL OR c.deleted = 'N')")
    List<StockCategoryMaster> findChildrenByParentCategoryPoidAndGroupPoid(
            @Param("parentCategoryPoid") Long parentCategoryPoid,
            @Param("groupPoid") Long groupPoid);

    List<StockCategoryMaster> findByGroupPoidAndDeletedNotOrDeletedIsNull(Long groupPoid, String deleted);
}
