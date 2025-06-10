```java
package com.healthcare.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class Patient {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String medicalRecord;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
```
