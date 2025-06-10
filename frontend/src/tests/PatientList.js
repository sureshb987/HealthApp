```javascript
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import PatientList from '../components/PatientList';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';

// Extend jest-dom matchers
import '@testing-library/jest-dom/extend-expect';

describe('PatientList Component', () => {
  let mock;

  beforeEach(() => {
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    mock.reset();
  });

  test('renders patient list when API call succeeds', async () => {
    const patients = [
      { id: 1, name: 'John Doe', medicalRecord: 'Record1' },
      { id: 2, name: 'Jane Smith', medicalRecord: 'Record2' },
    ];

    mock.onGet('/api/patients').reply(200, patients);

    render(<PatientList />);

    await waitFor(() => {
      expect(screen.getByText('John Doe - Record1')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith - Record2')).toBeInTheDocument();
    });
  });

  test('displays error message when API call fails', async () => {
    mock.onGet('/api/patients').reply(500);

    render(<PatientList />);

    await waitFor(() => {
      expect(screen.getByText('Failed to fetch patients')).toBeInTheDocument();
    });
  });

  test('renders loading state initially', () => {
    mock.onGet('/api/patients').reply(() => new Promise(() => {})); // Simulate pending request

    render(<PatientList />);

    expect(screen.getByText('Patients')).toBeInTheDocument();
  });
});
```
