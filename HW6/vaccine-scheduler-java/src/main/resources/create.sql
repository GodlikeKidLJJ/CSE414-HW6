CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);
-- create a new table patient
CREATE TABLE Patients(
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

-- create a new table appointment that keeps track of the appointment
CREATE TABLE Appointment(
    app_id INT,
    vaccine_name VARCHAR(255) REFERENCES Vaccines,
    app_date DATE,
    patient_name VARCHAR(255) REFERENCES Patients,
    caregivers_name VARCHAR(255) REFERENCES Caregivers,
    PRIMARY KEY (app_id)
);
