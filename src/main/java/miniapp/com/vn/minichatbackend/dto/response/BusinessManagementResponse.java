package miniapp.com.vn.minichatbackend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessManagementResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String description;
    private Integer status;
    private String metadata;
    private String style;
}

