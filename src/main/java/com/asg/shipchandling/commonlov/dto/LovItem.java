package com.asg.shipchandling.commonlov.dto;

public class LovItem {
    private Long poid;
    private String code;
    private String description;

    public LovItem() {}

    public LovItem(Long poid, String code, String description) {
        this.poid = poid;
        this.code = code;
        this.description = description;
    }

    public Long getPoid() {
        return poid;
    }

    public void setPoid(Long poid) {
        this.poid = poid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
