```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const PatientList = () => {
  const [patients, setPatients] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios.get(window.REACT_APP_API_URL || '/api/patients')
      .then(response => setPatients(response.data))
      .catch(err => setError('Failed to fetch patients'));
  }, []);

  if (error) return <div>{error}</div>;

  return (
    <div>
      <h2>Patients</h2>
      <ul>
        {patients.map(patient => (
          <li key={patient.id}>{patient.name} - {patient.medicalRecord}</li>
        ))}
      </ul>
    </div>
  );
};

export default PatientList;
```
