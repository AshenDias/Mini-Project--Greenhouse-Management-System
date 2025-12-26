create database Dias;
use Dias;
CREATE TABLE IF NOT EXISTS Greenhouse (
    greenhouse_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    temperature VARCHAR(50)
);
CREATE TABLE IF NOT EXISTS Plant (
    Plant_ID INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    wateramount DECIMAL(10,2) DEFAULT 0.0,
    nut_amount DECIMAL(10,2) DEFAULT 0.0,
    plant_type VARCHAR(50),
    greenhouse_id INT,
    active TINYINT DEFAULT 1,
    FOREIGN KEY (greenhouse_id) REFERENCES Greenhouse(greenhouse_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS Disease (
    Disease_ID INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    symptoms TEXT,
    treatment TEXT,
    severity ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
    affected_plant_id INT,
    FOREIGN KEY (affected_plant_id) REFERENCES Plant(Plant_ID) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS Inventory (
    greenhouse_ID INT,
    Description VARCHAR(255) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    number INT NOT NULL DEFAULT 1,
    category VARCHAR(100),
    unit_price DECIMAL(10,2) DEFAULT 0.00,
    PRIMARY KEY (greenhouse_ID, Description),
    FOREIGN KEY (greenhouse_ID) REFERENCES Greenhouse(greenhouse_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS tool (
    id INT PRIMARY KEY AUTO_INCREMENT,
    toolname VARCHAR(100) NOT NULL UNIQUE,
    connu INT NOT NULL DEFAULT 1,
    handovertime VARCHAR(50),
    tool_type VARCHAR(50),
    `condition` VARCHAR(50) DEFAULT 'Good',
    last_maintenance DATE
);
CREATE TABLE IF NOT EXISTS worker (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) DEFAULT 'Worker',
    phone VARCHAR(20),
    hire_date DATE
);
CREATE TABLE IF NOT EXISTS Task (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    plant_id INT,
    task_name VARCHAR(100),
    due_date DATE,
    status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (plant_id) REFERENCES Plant(Plant_ID) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS Harvest (
    harvest_id INT PRIMARY KEY AUTO_INCREMENT,
    plant_id INT,
    greenhouse_id INT,
    harvest_date DATE,
    quantity DECIMAL(10,2),
    quality VARCHAR(20),
    FOREIGN KEY (plant_id) REFERENCES Plant(Plant_ID) ON DELETE SET NULL,
    FOREIGN KEY (greenhouse_id) REFERENCES Greenhouse(greenhouse_id) ON DELETE SET NULL
);
INSERT INTO Greenhouse (greenhouse_id, name, location, temperature) VALUES
(1, 'Main Greenhouse', 'North Field', '25°C'),
(2, 'Research Greenhouse', 'Campus East', '28°C'),
(3, 'Tropical Greenhouse', 'South Wing', '30°C'),
(4, 'Seedling Greenhouse', 'Nursery Area', '22°C'),
(5, 'Hydroponic Greenhouse', 'Lab Building', '24°C');
INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount, plant_type, greenhouse_id) VALUES
(101, 'Tomato Plant', 2.5, 1.2, 'Vegetable', 1),
(102, 'Rose Bush', 1.8, 0.8, 'Flower', 1),
(103, 'Basil Herb', 1.2, 0.5, 'Herb', 2),
(104, 'Orchid', 0.8, 0.3, 'Flower', 3),
(105, 'Lemon Tree', 3.5, 2.0, 'Fruit', 4),
(106, 'Lettuce', 1.5, 0.6, 'Vegetable', 5),
(107, 'Mint', 1.3, 0.4, 'Herb', 2),
(108, 'Cactus', 0.2, 0.1, 'Succulent', 3),
(109, 'Strawberry', 1.7, 0.9, 'Fruit', 1),
(110, 'Aloe Vera', 0.5, 0.2, 'Medicinal', 4);
INSERT INTO Disease (Disease_ID, name, symptoms, treatment, severity, affected_plant_id) VALUES
(201, 'Powdery Mildew', 'White powdery spots on leaves', 'Apply fungicide weekly', 'Medium', 101),
(202, 'Leaf Spot', 'Brown spots with yellow halo', 'Remove affected leaves, apply copper spray', 'Low', 102),
(203, 'Root Rot', 'Wilting, yellow leaves, soft roots', 'Improve drainage, reduce watering', 'High', 105),
(204, 'Aphid Infestation', 'Sticky leaves, curled leaves', 'Spray with insecticidal soap', 'Medium', 103),
(205, 'Blossom End Rot', 'Dark spots on fruit bottoms', 'Maintain consistent watering, add calcium', 'Medium', 101),
(206, 'Fungal Infection', 'Mold growth, discolored leaves', 'Apply antifungal treatment', 'High', 104),
(207, 'Nutrient Deficiency', 'Yellowing leaves, stunted growth', 'Adjust fertilizer application', 'Low', 106);

INSERT INTO Inventory (greenhouse_ID, Description, stock, number, category, unit_price) VALUES
(1, 'Organic Fertilizer 5kg', 25, 1, 'Fertilizer', 24.99),
(1, 'Watering Can 10L', 12, 1, 'Equipment', 15.50),
(2, 'pH Test Strips', 100, 1, 'Testing', 9.99),
(2, 'Grow Lights LED', 8, 1, 'Lighting', 89.99),
(3, 'Humidity Trays', 30, 1, 'Accessories', 8.75),
(3, 'Orchid Pots 6inch', 45, 1, 'Containers', 4.25),
(4, 'Seed Starter Mix', 20, 1, 'Soil', 12.99),
(4, 'Pruning Shears', 15, 1, 'Tools', 18.50),
(5, 'Hydroponic Nutrients', 18, 1, 'Fertilizer', 32.99),
(5, 'pH Controller', 5, 1, 'Equipment', 125.00);
INSERT INTO tool (toolname, connu, handovertime, tool_type, `condition`, last_maintenance) VALUES
('Pruning Shears', 8, NULL, 'Cutting', 'Good', '2024-01-15'),
('Watering Hose', 4, '2024-03-20', 'Watering', 'Fair', '2024-01-10'),
('Soil Thermometer', 6, NULL, 'Measuring', 'Excellent', '2024-02-01'),
('Gardening Gloves', 20, '2024-03-18', 'Safety', 'Good', NULL),
('Spade', 5, NULL, 'Digging', 'Good', '2024-01-30'),
('Wheelbarrow', 3, '2024-03-22', 'Transport', 'Fair', '2023-12-15'),
('Sprayer', 7, NULL, 'Application', 'Excellent', '2024-02-20'),
('Measuring Tape', 10, NULL, 'Measuring', 'Good', NULL),
('Trowel', 12, '2024-03-19', 'Digging', 'Good', '2024-01-25'),
('Soil pH Meter', 3, NULL, 'Testing', 'Excellent', '2024-02-28');

INSERT INTO worker (name, email, username, password, role, phone, hire_date) VALUES
('John Doe', 'john.doe@agrovision.com', 'johndoe123', 'pass123word', 'Manager', '555-0101', '2023-01-15'),
('Jane Smith', 'jane.smith@agrovision.com', 'janesmith456', 'secure456pass', 'Supervisor', '555-0102', '2023-02-20'),
('Bob Wilson', 'bob.wilson@agrovision.com', 'bobwilson789', 'bobpass789', 'Technician', '555-0103', '2023-03-10'),
('Alice Brown', 'alice.brown@agrovision.com', 'alicebrown101', 'alice101pass', 'Researcher', '555-0104', '2023-04-05'),
('Charlie Davis', 'charlie.davis@agrovision.com', 'charlie202', 'davis202pass', 'Maintenance', '555-0105', '2023-05-12'),
('Diana Evans', 'diana.evans@agrovision.com', 'dianae303', 'evans303pass', 'Technician', '555-0106', '2023-06-18'),
('Edward Foster', 'edward.foster@agrovision.com', 'edwardf404', 'foster404pass', 'Supervisor', '555-0107', '2023-07-22'),
('Fiona Green', 'fiona.green@agrovision.com', 'fionag505', 'green505pass', 'Researcher', '555-0108', '2023-08-30'),
('George Harris', 'george.harris@agrovision.com', 'georgeh606', 'harris606pass', 'Maintenance', '555-0109', '2023-09-14'),
('Hannah White', 'hannah.white@agrovision.com', 'hannahw707', 'white707pass', 'Technician', '555-0110', '2023-10-25');

INSERT INTO Task (plant_id, task_name, due_date, status) VALUES
(101, 'Watering', '2024-03-25', 'Completed'),
(101, 'Fertilize', '2024-03-28', 'Pending'),
(102, 'Pruning', '2024-03-26', 'In Progress'),
(103, 'Harvest', '2024-03-24', 'Completed'),
(105, 'Soil Test', '2024-03-29', 'Pending'),
(106, 'Transplant', '2024-03-27', 'In Progress'),
(108, 'Watering', '2024-03-25', 'Completed'),
(110, 'Harvest', '2024-03-30', 'Pending');

INSERT INTO Harvest (plant_id, greenhouse_id, harvest_date, quantity, quality) VALUES
(101, 1, '2024-03-15', 12.5, 'Excellent'),
(103, 2, '2024-03-10', 2.3, 'Good'),
(105, 4, '2024-03-05', 8.7, 'Excellent'),
(106, 5, '2024-03-12', 5.4, 'Good'),
(109, 1, '2024-03-18', 3.2, 'Average'),
(110, 4, '2024-03-08', 1.8, 'Excellent');

CREATE INDEX idx_plant_greenhouse ON Plant(greenhouse_id);
CREATE INDEX idx_disease_plant ON Disease(affected_plant_id);
CREATE INDEX idx_inventory_greenhouse ON Inventory(greenhouse_ID);
CREATE INDEX idx_task_plant ON Task(plant_id);
CREATE INDEX idx_harvest_plant ON Harvest(plant_id);
CREATE INDEX idx_harvest_greenhouse ON Harvest(greenhouse_id);
CREATE INDEX idx_worker_username ON worker(username);

CREATE VIEW Plant_Summary AS
SELECT 
    p.Plant_ID,
    p.name AS plant_name,
    p.plant_type,
    g.name AS greenhouse_name,
    p.wateramount,
    p.nut_amount
FROM Plant p
LEFT JOIN Greenhouse g ON p.greenhouse_id = g.greenhouse_id;

CREATE VIEW Disease_Summary AS
SELECT 
    d.Disease_ID,
    d.name AS disease_name,
    d.severity,
    p.name AS affected_plant,
    g.name AS plant_location
FROM Disease d
LEFT JOIN Plant p ON d.affected_plant_id = p.Plant_ID
LEFT JOIN Greenhouse g ON p.greenhouse_id = g.greenhouse_id;

CREATE VIEW Inventory_Status AS
SELECT 
    i.greenhouse_ID,
    g.name AS greenhouse_name,
    i.Description,
    i.stock,
    i.category,
    i.unit_price,
    (i.stock * i.unit_price) AS total_value
FROM Inventory i
JOIN Greenhouse g ON i.greenhouse_ID = g.greenhouse_id;

CREATE VIEW Tool_Status AS
SELECT 
    toolname,
    connu AS quantity,
    CASE 
        WHEN handovertime IS NOT NULL THEN 'Borrowed'
        ELSE 'Available'
    END AS status,
    tool_type,
    `condition`,
    last_maintenance
FROM tool;

DELIMITER $$
CREATE PROCEDURE AddNewPlant(
    IN p_id INT,
    IN p_name VARCHAR(100),
    IN p_water DECIMAL(10,2),
    IN p_nutrients DECIMAL(10,2),
    IN p_type VARCHAR(50),
    IN p_greenhouse INT
)
BEGIN
    INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount, plant_type, greenhouse_id)
    VALUES (p_id, p_name, p_water, p_nutrients, p_type, p_greenhouse);
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE ReportLowInventory(IN threshold INT)
BEGIN
    SELECT * FROM Inventory WHERE stock < threshold ORDER BY stock ASC;
END$$
DELIMITER ;

CREATE USER IF NOT EXISTS 'AgroVision'@'localhost' IDENTIFIED BY 'asdf4444';
GRANT ALL PRIVILEGES ON greenhousemanagementsystem.* TO 'AgroVision'@'localhost';
FLUSH PRIVILEGES;

SELECT 'Database created successfully!' AS Status;
SELECT COUNT(*) AS Greenhouse_Count FROM Greenhouse;
SELECT COUNT(*) AS Plant_Count FROM Plant;
SELECT COUNT(*) AS Disease_Count FROM Disease;
SELECT COUNT(*) AS Inventory_Count FROM Inventory;
SELECT COUNT(*) AS Tool_Count FROM tool;
SELECT COUNT(*) AS Worker_Count FROM worker;


