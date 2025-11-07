package com.alsharif.shipchandling.commonlov.dto;

import java.util.List;

public class LovResponse {

        private List<LovItem> items;

        public LovResponse() {}

        public LovResponse(List<LovItem> items) {
            this.items = items;
        }

        public List<LovItem> getItems() {
            return items;
        }

        public void setItems(List<LovItem> items) {
            this.items = items;
        }

}
