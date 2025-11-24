package com.alsharif.shipchandling.requestforquotation.dto.response;

import com.alsharif.shipchandling.requestforquotation.dto.ItemWithoutSupplierDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsWithoutSuppliersResponse {
    private List<ItemWithoutSupplierDto> itemsWithoutSuppliers;
    private Integer count;
}