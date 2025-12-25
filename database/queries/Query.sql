

show databases;
create database GreenhouseManagementSystem;
use GreenhouseManagementSystem;
CREATE TABLE IF NOT EXISTS Plant (
                                     Plant_ID INT PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL,
                                     wateramount DECIMAL(10,2) DEFAULT 0.00,
                                     nut_amount DECIMAL(10,2) DEFAULT 0.00,
                                     plant_type VARCHAR(50),
                                     greenhouse_id INT,
                                     growth_stage VARCHAR(30) DEFAULT 'Seedling',
                                     planting_date DATE,
                                     harvest_date DATE,
                                     status ENUM('Active', 'Dormant', 'Harvested', 'Diseased') DEFAULT 'Active',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                     INDEX idx_plant_name (name),
                                     INDEX idx_plant_type (plant_type),
                                     INDEX idx_greenhouse_id (greenhouse_id),
                                     INDEX idx_status (status),

                                     CONSTRAINT chk_wateramount CHECK (wateramount >= 0),
                                     CONSTRAINT chk_nut_amount CHECK (nut_amount >= 0)
);


INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount, plant_type, greenhouse_id, growth_stage) VALUES
                                                                                                         (1, 'Tomato', 2.5, 1.2, 'Vegetable', 1, 'Fruiting'),
                                                                                                         (2, 'Rose', 1.8, 0.9, 'Flower', 2, 'Blooming'),
                                                                                                         (3, 'Basil', 1.2, 0.5, 'Herb', 1, 'Vegetative'),
                                                                                                         (4, 'Lettuce', 1.5, 0.7, 'Vegetable', 1, 'Harvest Ready'),
                                                                                                         (5, 'Orchid', 1.0, 0.3, 'Flower', 3, 'Blooming');


show tables;
select*from Plant;

CREATE TABLE IF NOT EXISTS Disease (
                                       Disease_ID INT PRIMARY KEY AUTO_INCREMENT,
                                       name VARCHAR(100) NOT NULL,
                                       symptoms TEXT NOT NULL,
                                       treatment TEXT NOT NULL,
                                       severity ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
                                       affected_plant_id INT,
                                       affected_plant_type VARCHAR(50),
                                       first_detected DATE,
                                       status ENUM('Active', 'Treated', 'Controlled') DEFAULT 'Active',
                                       prevention_methods TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       INDEX idx_disease_name (name),
                                       INDEX idx_severity (severity),
                                       INDEX idx_affected_plant (affected_plant_id),
                                       INDEX idx_status (status),

                                       CONSTRAINT fk_affected_plant
                                           FOREIGN KEY (affected_plant_id)
                                               REFERENCES Plant(Plant_ID)
                                               ON DELETE SET NULL
);


INSERT INTO Disease (name, symptoms, treatment, severity, affected_plant_id) VALUES
                                                                                 ('Powdery Mildew', 'White powdery spots on leaves', 'Apply fungicide weekly', 'Medium', 1),
                                                                                 ('Leaf Spot', 'Brown circular spots on leaves', 'Remove affected leaves, apply copper spray', 'Low', 2),
                                                                                 ('Root Rot', 'Wilting, yellow leaves, soft roots', 'Improve drainage, apply fungicide to soil', 'High', 3),
                                                                                 ('Blight', 'Rapid wilting, dark lesions on stems', 'Remove infected plants, apply fungicide', 'Critical', 4);


CREATE TABLE IF NOT EXISTS Greenhouse (
                                          greenhouse_ID INT PRIMARY KEY,
                                          location VARCHAR(100) NOT NULL,
                                          size_sqft DECIMAL(10,2),
                                          temperature_setting DECIMAL(5,2),
                                          humidity_setting DECIMAL(5,2),
                                          light_hours INT DEFAULT 12,
                                          irrigation_system ENUM('Drip', 'Sprinkler', 'Manual') DEFAULT 'Drip',
                                          status ENUM('Operational', 'Maintenance', 'Closed') DEFAULT 'Operational',
                                          manager_name VARCHAR(100),
                                          contact_phone VARCHAR(15),
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                          INDEX idx_location (location),
                                          INDEX idx_status (status)
);


INSERT INTO Greenhouse (greenhouse_ID, location, size_sqft, temperature_setting, humidity_setting) VALUES
                                                                                                       (1, 'North Wing', 1200.00, 25.5, 65.0),
                                                                                                       (2, 'South Wing', 1500.00, 24.0, 70.0),
                                                                                                       (3, 'East Wing', 800.00, 26.0, 60.0),
                                                                                                       (4, 'West Wing', 2000.00, 23.5, 75.0);

CREATE TABLE IF NOT EXISTS Inventory (
                                         inventory_id INT PRIMARY KEY AUTO_INCREMENT,
                                         greenhouse_ID INT NOT NULL,
                                         Description VARCHAR(200) NOT NULL,
                                         stock INT DEFAULT 0,
                                         number INT DEFAULT 1,
                                         category ENUM('Seeds', 'Fertilizer', 'Tools', 'Equipment', 'Harvest', 'Chemicals') NOT NULL,
                                         unit_price DECIMAL(10,2) DEFAULT 0.00,
                                         supplier VARCHAR(100),
                                         reorder_level INT DEFAULT 10,
                                         expiry_date DATE,
                                         storage_location VARCHAR(100),
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                         INDEX idx_greenhouse (greenhouse_ID),
                                         INDEX idx_category (category),
                                         INDEX idx_description (Description),
                                         INDEX idx_supplier (supplier),

                                         CONSTRAINT fk_inventory_greenhouse
                                             FOREIGN KEY (greenhouse_ID)
                                                 REFERENCES Greenhouse(greenhouse_ID)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT chk_stock CHECK (stock >= 0),
                                         CONSTRAINT chk_number CHECK (number >= 0),
                                         CONSTRAINT chk_unit_price CHECK (unit_price >= 0)
);


INSERT INTO Inventory (greenhouse_ID, Description, stock, number, category, unit_price) VALUES
                                                                                            (1, 'Tomato Seeds', 500, 50, 'Seeds', 2.50),
                                                                                            (1, 'Organic Fertilizer 5kg', 20, 5, 'Fertilizer', 15.99),
                                                                                            (2, 'Pruning Shears', 15, 15, 'Tools', 8.75),
                                                                                            (1, 'Harvested Tomatoes', 200, 10, 'Harvest', 0.00),
                                                                                            (3, 'Insecticide Spray', 30, 30, 'Chemicals', 12.50),
                                                                                            (4, 'Watering Hose 50ft', 8, 8, 'Equipment', 25.00);

CREATE TABLE IF NOT EXISTS Worker (
                                      worker_id INT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
                                      email VARCHAR(100) UNIQUE NOT NULL,
                                      username VARCHAR(50) UNIQUE NOT NULL,
                                      password VARCHAR(255) NOT NULL,
                                      role ENUM('Manager', 'Supervisor', 'Gardener', 'Technician', 'Admin') DEFAULT 'Gardener',
                                      phone VARCHAR(15),
                                      hire_date DATE NOT NULL,
                                      salary DECIMAL(10,2),
                                      assigned_greenhouse_id INT,
                                      status ENUM('Active', 'On Leave', 'Terminated') DEFAULT 'Active',
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                      INDEX idx_username (username),
                                      INDEX idx_role (role),
                                      INDEX idx_assigned_greenhouse (assigned_greenhouse_id),
                                      INDEX idx_status (status),

                                      CONSTRAINT fk_worker_greenhouse
                                          FOREIGN KEY (assigned_greenhouse_id)
                                              REFERENCES Greenhouse(greenhouse_ID)
                                              ON DELETE SET NULL,

                                      CONSTRAINT chk_email CHECK (email LIKE '%@%.%'),
                                      CONSTRAINT chk_salary CHECK (salary >= 0)
);


INSERT INTO Worker (name, email, username, password, role, phone, hire_date, assigned_greenhouse_id) VALUES
                                                                                                         ('John Smith', 'john@whiteleaf.com', 'john.smith', 'pass123', 'Manager', '1234567890', '2023-01-15', 1),
                                                                                                         ('Maria Garcia', 'maria@whiteleaf.com', 'maria.g', 'pass123', 'Supervisor', '0987654321', '2023-03-20', 2),
                                                                                                         ('Robert Chen', 'robert@whiteleaf.com', 'robert.c', 'pass123', 'Gardener', '5551234567', '2023-05-10', 1),
                                                                                                         ('Lamda Park', 'lisa@whiteleaf.com', 'lisa.p', 'pass123', 'Technician', '5559876543', '2023-06-15', 3),
                                                                                                         ('Admin User', 'admin@whiteleaf.com', 'admin', 'admin123', 'Admin', '5550000000', '2023-01-01', NULL);


CREATE TABLE IF NOT EXISTS Tool (
                                    tool_id INT PRIMARY KEY AUTO_INCREMENT,
                                    toolname VARCHAR(100) NOT NULL,
                                    connu INT DEFAULT 1,
                                    handovertime VARCHAR(50),
                                    tool_type ENUM('Hand Tool', 'Power Tool', 'Equipment', 'Measuring') DEFAULT 'Hand Tool',
                                    `condition` ENUM('New', 'Good', 'Fair', 'Poor', 'Broken') DEFAULT 'Good',
                                    last_maintenance DATE,
                                    assigned_to_worker_id INT,
                                    greenhouse_id INT,
                                    purchase_date DATE,
                                    purchase_price DECIMAL(10,2),
                                    status ENUM('Available', 'In Use', 'Under Maintenance', 'Retired') DEFAULT 'Available',
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                    INDEX idx_tool_name (toolname),
                                    INDEX idx_tool_type (tool_type),
                                    INDEX idx_assigned_worker (assigned_to_worker_id),
                                    INDEX idx_greenhouse (greenhouse_id),
                                    INDEX idx_status (status),

                                    CONSTRAINT fk_tool_worker
                                        FOREIGN KEY (assigned_to_worker_id)
                                            REFERENCES Worker(worker_id)
                                            ON DELETE SET NULL,

                                    CONSTRAINT fk_tool_greenhouse
                                        FOREIGN KEY (greenhouse_id)
                                            REFERENCES Greenhouse(greenhouse_ID)
                                            ON DELETE SET NULL,

                                    CONSTRAINT chk_connu CHECK (connu >= 0),
                                    CONSTRAINT chk_purchase_price CHECK (purchase_price >= 0)
);


INSERT INTO Tool (toolname, connu, handovertime, tool_type, `condition`, greenhouse_id) VALUES
                                                                                            ('Pruning Shears', 5, '9:00 AM', 'Hand Tool', 'Good', 1),
                                                                                            ('Watering Can', 10, '8:30 AM', 'Equipment', 'Fair', 2),
                                                                                            ('Soil pH Meter', 3, '10:00 AM', 'Measuring', 'New', 1),
                                                                                            ('Gardening Gloves', 20, '8:00 AM', 'Hand Tool', 'Good', 3),
                                                                                            ('Wheelbarrow', 2, 'Not Required', 'Equipment', 'Good', 4);


CREATE TABLE IF NOT EXISTS Harvest (
                                       harvest_id INT PRIMARY KEY AUTO_INCREMENT,
                                       greenhouse_ID INT NOT NULL,
                                       plant_id INT NOT NULL,
                                       Description VARCHAR(200) NOT NULL,
                                       quantity DECIMAL(10,2) NOT NULL,
                                       unit ENUM('kg', 'lb', 'pieces', 'bunches') DEFAULT 'kg',
                                       harvest_date DATE NOT NULL,
                                       quality ENUM('Excellent', 'Good', 'Fair', 'Poor') DEFAULT 'Good',
                                       harvested_by_worker_id INT,
                                       sold_quantity DECIMAL(10,2) DEFAULT 0,
                                       sale_price DECIMAL(10,2),
                                       storage_location VARCHAR(100),
                                       notes TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       INDEX idx_greenhouse (greenhouse_ID),
                                       INDEX idx_plant (plant_id),
                                       INDEX idx_harvest_date (harvest_date),
                                       INDEX idx_quality (quality),

                                       CONSTRAINT fk_harvest_greenhouse
                                           FOREIGN KEY (greenhouse_ID)
                                               REFERENCES Greenhouse(greenhouse_ID)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_harvest_plant
                                           FOREIGN KEY (plant_id)
                                               REFERENCES Plant(Plant_ID)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_harvested_by
                                           FOREIGN KEY (harvested_by_worker_id)
                                               REFERENCES Worker(worker_id)
                                               ON DELETE SET NULL,

                                       CONSTRAINT chk_quantity CHECK (quantity > 0),
                                       CONSTRAINT chk_sold_quantity CHECK (sold_quantity >= 0 AND sold_quantity <= quantity),
                                       CONSTRAINT chk_sale_price CHECK (sale_price >= 0)
);


INSERT INTO Harvest (greenhouse_ID, plant_id, Description, quantity, unit, harvest_date, quality) VALUES
                                                                                                      (1, 1, 'Tomatoes - Summer Harvest', 150.5, 'kg', '2024-03-15', 'Excellent'),
                                                                                                      (1, 4, 'Lettuce Heads', 80.0, 'pieces', '2024-03-10', 'Good'),
                                                                                                      (2, 2, 'Rose Flowers', 200.0, 'pieces', '2024-03-12', 'Excellent'),
                                                                                                      (3, 5, 'Orchid Blooms', 50.0, 'pieces', '2024-03-08', 'Good');

CREATE TABLE IF NOT EXISTS Task (
                                    task_id INT PRIMARY KEY AUTO_INCREMENT,
                                    plant_id INT,
                                    greenhouse_id INT NOT NULL,
                                    task_type ENUM('Watering', 'Fertilizing', 'Pruning', 'Pest Control', 'Harvesting', 'Cleaning') NOT NULL,
                                    wateramount VARCHAR(50),
                                    nutrientamount VARCHAR(50),
                                    assigned_to_worker_id INT,
                                    scheduled_date DATE NOT NULL,
                                    completed_date DATE,
                                    status ENUM('Pending', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Pending',
                                    priority ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
                                    notes TEXT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                    INDEX idx_plant (plant_id),
                                    INDEX idx_greenhouse (greenhouse_id),
                                    INDEX idx_assigned_worker (assigned_to_worker_id),
                                    INDEX idx_scheduled_date (scheduled_date),
                                    INDEX idx_status (status),
                                    INDEX idx_priority (priority),

                                    CONSTRAINT fk_task_plant
                                        FOREIGN KEY (plant_id)
                                            REFERENCES Plant(Plant_ID)
                                            ON DELETE SET NULL,

                                    CONSTRAINT fk_task_greenhouse
                                        FOREIGN KEY (greenhouse_id)
                                            REFERENCES Greenhouse(greenhouse_ID)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_task_worker
                                        FOREIGN KEY (assigned_to_worker_id)
                                            REFERENCES Worker(worker_id)
                                            ON DELETE SET NULL
);


INSERT INTO Task (plant_id, greenhouse_id, task_type, wateramount, nutrientamount, assigned_to_worker_id, scheduled_date, status) VALUES
                                                                                                                                      (1, 1, 'Watering', '2.5 liters', '120g fertilizer', 3, '2024-03-20', 'Pending'),
                                                                                                                                      (2, 2, 'Pruning', NULL, NULL, 3, '2024-03-18', 'Completed'),
                                                                                                                                      (3, 1, 'Fertilizing', '1.2 liters', '50g nutrients', 4, '2024-03-19', 'In Progress'),
                                                                                                                                      (4, 1, 'Harvesting', NULL, NULL, 3, '2024-03-22', 'Pending');

CREATE TABLE IF NOT EXISTS Environment_Log (
                                               log_id INT PRIMARY KEY AUTO_INCREMENT,
                                               greenhouse_ID INT NOT NULL,
                                               temperature DECIMAL(5,2) NOT NULL,
                                               humidity DECIMAL(5,2) NOT NULL,
                                               light_intensity INT,
                                               soil_moisture DECIMAL(5,2),
                                               recorded_by_worker_id INT,
                                               notes TEXT,
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                               INDEX idx_greenhouse (greenhouse_ID),
                                               INDEX idx_created_at (created_at),

                                               CONSTRAINT fk_envlog_greenhouse
                                                   FOREIGN KEY (greenhouse_ID)
                                                       REFERENCES Greenhouse(greenhouse_ID)
                                                       ON DELETE CASCADE,

                                               CONSTRAINT fk_envlog_worker
                                                   FOREIGN KEY (recorded_by_worker_id)
                                                       REFERENCES Worker(worker_id)
                                                       ON DELETE SET NULL,

                                               CONSTRAINT chk_temperature CHECK (temperature BETWEEN -20 AND 60),
                                               CONSTRAINT chk_humidity CHECK (humidity BETWEEN 0 AND 100),
                                               CONSTRAINT chk_soil_moisture CHECK (soil_moisture BETWEEN 0 AND 100)
);


INSERT INTO Environment_Log (greenhouse_ID, temperature, humidity, light_intensity, soil_moisture) VALUES
                                                                                                       (1, 25.5, 65.0, 850, 45.5),
                                                                                                       (1, 26.0, 63.5, 900, 42.0),
                                                                                                       (2, 24.0, 70.0, 800, 50.0),
                                                                                                       (3, 26.5, 60.0, 950, 38.5);

CREATE TABLE IF NOT EXISTS Transaction_Log (
                                               transaction_id INT PRIMARY KEY AUTO_INCREMENT,
                                               table_name VARCHAR(50) NOT NULL,
                                               record_id INT NOT NULL,
                                               action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
                                               old_values JSON,
                                               new_values JSON,
                                               performed_by_worker_id INT,
                                               ip_address VARCHAR(45),
                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                               INDEX idx_table_record (table_name, record_id),
                                               INDEX idx_action (action),
                                               INDEX idx_created_at (created_at),
                                               INDEX idx_performed_by (performed_by_worker_id),

                                               CONSTRAINT fk_transaction_worker
                                                   FOREIGN KEY (performed_by_worker_id)
                                                       REFERENCES Worker(worker_id)
                                                       ON DELETE SET NULL
);


show tables;

select*from Disease;

SHOW VARIABLES LIKE 'datadir';
SELECT username, password FROM Worker;
