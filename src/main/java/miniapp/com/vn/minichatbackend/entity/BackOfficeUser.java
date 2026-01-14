package miniapp.com.vn.minichatbackend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "back_office_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOfficeUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_id", nullable = false, length = 255, unique = true)
    private String providerId;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
} 