```java
package com.healthcare.controller;

import com.healthcare.model.Patient;
import com.healthcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatientControllerTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllPatients_ReturnsPatientList() throws Exception {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setName("John Doe");
        patient1.setMedicalRecord("Record1");

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("Jane Smith");
        patient2.setMedicalRecord("Record2");

        List<Patient> patients = Arrays.asList(patient1, patient2);

        when(patientRepository.findAll()).thenReturn(patients);

        mockMvc.perform(get("/api/patients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(patients)));
    }

    @Test
    void createPatient_SavesAndReturnsPatient() throws Exception {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setMedicalRecord("Record1");

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(patient)));
    }

    @Test
    void healthCheck_ReturnsHealthy() throws Exception {
        mockMvc.perform(get("/api/patients/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Patient Management API is healthy"));
    }
}
```
