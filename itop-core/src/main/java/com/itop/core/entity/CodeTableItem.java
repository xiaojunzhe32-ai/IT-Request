package com.itop.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configurable code table item used by lightweight workflow dropdowns.
 */
@Entity
@Table(name = "code_table_item")
@Getter
@Setter
@NoArgsConstructor
public class CodeTableItem extends BaseEntity {

    @Column(name = "table_code", nullable = false, length = 100)
    private String tableCode;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public CodeTableItem(String tableCode, String code, String name) {
        this.tableCode = tableCode;
        this.code = code;
        setName(name);
    }
}
