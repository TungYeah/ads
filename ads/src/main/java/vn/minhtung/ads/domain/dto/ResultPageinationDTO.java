package vn.minhtung.ads.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPageinationDTO {
    private Meta meta;

    private Object result;

    @Getter
    @Setter
    public static class Meta {

        private int page;

        private int pageSize;

        private int pages;

        private long total;
    }
}
