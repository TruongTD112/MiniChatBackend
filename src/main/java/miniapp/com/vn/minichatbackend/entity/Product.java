package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "owner_id", nullable = false)
	private Integer ownerId;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "main_image_url")
	private String mainImageUrl;

	@Column(name = "detail_image_url", columnDefinition = "TEXT")
	private String detailImageUrl;

	@Column(name = "quantity_avail")
	private Integer quantityAvailable;

	@Column(nullable = false, length = 50)
	private String status;

	@Column(name = "metadata")
	private String metadata;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
}
