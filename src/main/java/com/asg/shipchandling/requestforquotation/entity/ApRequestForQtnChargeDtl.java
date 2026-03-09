package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;


@Entity
@Table(name = "AP_REQUEST_FOR_QTN_CHARGE_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*@SequenceGenerator(
        name = "ap_req_qtn_charge_dtl_seq",
        sequenceName = "AP_REQ_QTN_CHARGE_DTL_SEQ",
        allocationSize = 1
)*/
public class ApRequestForQtnChargeDtl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", nullable = false)
    private ApRequestForQtnHdr header;

    @Column(name = "CHARGE_POID")
    private Long chargePoid;

    @Column(name = "CHARGE_AMOUNT", precision = 18, scale = 6)
    private BigDecimal chargeAmount;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

}
