package com.asg.shipchandling.requestforquotation.repository;

import com.asg.shipchandling.requestforquotation.entity.ApRequestForQtnHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApRequestForQtnHdrRepository extends JpaRepository<ApRequestForQtnHdr, Long>,
        JpaSpecificationExecutor<ApRequestForQtnHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<ApRequestForQtnHdr> findByTransactionPoid(Long transactionPoid);

    Optional<ApRequestForQtnHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(
            Long transactionPoid, Long groupPoid, Long companyPoid);

    List<ApRequestForQtnHdr> findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deleted);

    List<ApRequestForQtnHdr> findByGroupPoidAndCompanyPoidAndStatusAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String status, String deleted);

    @Query("SELECT r FROM ApRequestForQtnHdr r WHERE " +
           "r.groupPoid = :groupPoid AND " +
           "r.companyPoid = :companyPoid AND " +
           "(r.deleted IS NULL OR r.deleted != 'Y') AND " +
           "(:status IS NULL OR UPPER(r.status) = UPPER(:status)) AND " +
           "(:divisionPoid IS NULL OR r.divisionPoid = :divisionPoid) AND " +
           "(:salesQtnPoid IS NULL OR r.salesQtnPoid = :salesQtnPoid) AND " +
           "(:fromDate IS NULL OR r.transactionDate >= :fromDate) AND " +
           "(:toDate IS NULL OR r.transactionDate <= :toDate) AND " +
           "(:search IS NULL OR LOWER(r.docRef) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.transactionDate DESC")
    Page<ApRequestForQtnHdr> findAllWithFilters(
            @Param("groupPoid") Long groupPoid,
            @Param("companyPoid") Long companyPoid,
            @Param("status") String status,
            @Param("divisionPoid") Long divisionPoid,
            @Param("salesQtnPoid") Long salesQtnPoid,
            @Param("fromDate") java.sql.Timestamp fromDate,
            @Param("toDate") java.sql.Timestamp toDate,
            @Param("search") String search,
            Pageable pageable);
}
