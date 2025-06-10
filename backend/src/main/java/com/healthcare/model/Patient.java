```java
package com.healthcare.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Patient {
    @Id
    private Long id;
    private String name;
    private String medicalRecord;
}
```
