package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "owners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Owner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "address")
	private String address;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "description")
	private String description;

	@Column(name = "status")
	private String status;

	@Column(name = "metadata")
	private String metadata;

	@Column(name = "style")
	private String style;

	@Column(name = "intent")
	private String intent;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
} 