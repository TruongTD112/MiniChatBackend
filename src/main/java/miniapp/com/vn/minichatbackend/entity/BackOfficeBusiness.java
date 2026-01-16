package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BackOffice_Business")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOfficeBusiness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "backoffice_user_id")
    private Long backOfficeUserId;

    @Column
    private Integer status;

    @Column(length = 10)
    private String role; // owner, staff

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

