```java
package com.healthcare.repository;

import com.healthcare.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void saveAndFindPatient_Success() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setMedicalRecord("Record1");

        Patient savedPatient = patientRepository.save(patient);

        assertNotNull(savedPatient);
        assertEquals(1L, savedPatient.getId());
        assertEquals("John Doe", savedPatient.getName());

        Patient foundPatient = patientRepository.findById(1L).orElse(null);
        assertNotNull(foundPatient);
        assertEquals("John Doe", foundPatient.getName());
    }

    @Test
    void findAllPatients_ReturnsList() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setName("John Doe");
        patient1.setMedicalRecord("Record1");

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("Jane Smith");
        patient2.setMedicalRecord("Record2");

        patientRepository.save(patient1);
        patientRepository.save(patient2);

        assertEquals(2, patientRepository.findAll().size());
    }
}
```
