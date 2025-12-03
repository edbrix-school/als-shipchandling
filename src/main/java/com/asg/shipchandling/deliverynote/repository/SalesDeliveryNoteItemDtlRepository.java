package com.asg.shipchandling.deliverynote.repository;

import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteItemDtl;
import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteItemDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesDeliveryNoteItemDtlRepository
        extends JpaRepository<SalesDeliveryNoteItemDtl, SalesDeliveryNoteItemDtlId> {

    List<SalesDeliveryNoteItemDtl> findByTransactionPoid(Long transactionPoid);

    List<SalesDeliveryNoteItemDtl> findByTransactionPoidAndCheckAll(Long transactionPoid, String checkAll);

    void deleteByTransactionPoid(Long transactionPoid);

    @Modifying
    @Query("DELETE FROM SalesDeliveryNoteItemDtl d WHERE d.transactionPoid = :transactionPoid AND d.checkAll = 'N'")
    void deleteByTransactionPoidAndCheckAllN(@Param("transactionPoid") Long transactionPoid);

    @Query("SELECT MAX(d.detRowId) FROM SalesDeliveryNoteItemDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    @Query("SELECT SUM(d.amount) FROM SalesDeliveryNoteItemDtl d WHERE d.transactionPoid = :transactionPoid AND d.checkAll = 'Y'")
    Long sumAmountByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    @Query("SELECT SUM(d.discount) FROM SalesDeliveryNoteItemDtl d WHERE d.transactionPoid = :transactionPoid AND d.checkAll = 'Y'")
    Long sumDiscountByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
