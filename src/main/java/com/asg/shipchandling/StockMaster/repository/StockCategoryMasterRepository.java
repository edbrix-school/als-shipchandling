package com.asg.shipchandling.StockMaster.repository;

import com.asg.shipchandling.StockMaster.entity.StockCategoryMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockCategoryMasterRepository extends JpaRepository<StockCategoryMasterEntity, Long> {
    
    List<StockCategoryMasterEntity> findByGroupPoid(Long groupPoid);
    
    Optional<StockCategoryMasterEntity> findByCategoryPoid(Long categoryPoid);
    
    List<StockCategoryMasterEntity> findByParentCategoryPoid(Long parentCategoryPoid);
    
    List<StockCategoryMasterEntity> findByGroupPoidAndParentCategoryPoidIsNull(Long groupPoid);
}



