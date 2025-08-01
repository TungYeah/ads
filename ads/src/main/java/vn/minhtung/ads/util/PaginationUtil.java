package vn.minhtung.ads.util;

import org.springframework.data.domain.Page;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO;
import vn.minhtung.ads.domain.dto.ResultPageinationDTO.Meta;

import java.util.List;

public class PaginationUtil {

    public static <T> ResultPageinationDTO build(Page<?> page, List<T> content) {
        Meta meta = new Meta();
        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ResultPageinationDTO dto = new ResultPageinationDTO();
        dto.setMeta(meta);
        dto.setResult(content);
        return dto;
    }
}