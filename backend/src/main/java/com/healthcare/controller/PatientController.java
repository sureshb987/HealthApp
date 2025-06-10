```java
package com.healthcare.controller;

import com.healthcare.model.Patient;
import com.healthcare.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public List<Patient> getAllPatients() {
        logger.info("Fetching all patients");
        return patientRepository.findAll();
    }

    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        logger.info("Creating patient: {}", patient.getName());
        return patientRepository.save(patient);
    }

    @GetMapping("/health")
    public String healthCheck() {
        logger.info("Health check endpoint accessed");
        return "Patient Management API is healthy";
    }
}
```
