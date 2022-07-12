package com.itsol.recruit.web.vm;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class SeachVM {
    @Nullable
    Number pageNumber;
    @Nullable
    Number pageSize;
    @Nullable
    String name;
    @Nullable
    String email;
    @Nullable
    String userName;
}
