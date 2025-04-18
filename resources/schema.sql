-- Database schema for Stock Management System

-- Category table
CREATE TABLE IF NOT EXISTS Category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

-- Supplier table
CREATE TABLE IF NOT EXISTS Supplier (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact TEXT,
    address TEXT
);

-- Product table
CREATE TABLE IF NOT EXISTS Product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    category_id INTEGER,
    supplier_id INTEGER,
    unit_price REAL NOT NULL DEFAULT 0,
    stock_qty INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 5,
    FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(id) ON DELETE SET NULL
);

-- Customer table
CREATE TABLE IF NOT EXISTS Customer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact TEXT,
    email TEXT,
    address TEXT
);

-- Order table
CREATE TABLE IF NOT EXISTS "Order" (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER,
    order_date TEXT NOT NULL,
    total_amount REAL NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'New',
    FOREIGN KEY (customer_id) REFERENCES Customer(id) ON DELETE SET NULL
);

-- OrderItem table
CREATE TABLE IF NOT EXISTS OrderItem (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES "Order"(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE RESTRICT
);

-- Report table
CREATE TABLE IF NOT EXISTS Report (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_type TEXT NOT NULL,
    generated_on TEXT NOT NULL,
    parameters TEXT,
    file_path TEXT NOT NULL
);

-- InventoryAdjustment table
CREATE TABLE IF NOT EXISTS InventoryAdjustment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    change_qty INTEGER NOT NULL,
    reason TEXT,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE RESTRICT
);

-- ========== Category ==========
INSERT OR IGNORE INTO Category (name, description) VALUES ('Electronics', 'Electronic devices and components');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Office Supplies', 'Supplies for office use');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Furniture', 'Office and home furniture');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Clothing', 'Apparel and garments');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Kitchenware', 'Utensils, cookware, and appliances');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Books', 'Printed and electronic books');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Toys', 'Children''s toys and games');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Hardware', 'Tools and building materials');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Software', 'Computer programs and licenses');
INSERT OR IGNORE INTO Category (name, description) VALUES ('Gardening', 'Plants, seeds, and gardening tools');

-- ========== Supplier ==========
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('TechSource Inc.', '555-1234', '123 Tech Blvd, Silicon Valley, CA');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('Office Depot', '555-5678', '456 Supply St, Businessville, NY');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('Furniture World', '555-9012', '789 Chair Ave, Comfort City, TX');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('FashionHub Ltd.', '555-3456', '321 Style Rd, Trendtown, FL');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('CookMaster Co.', '555-7890', '654 Kitchen Ln, Foodville, IL');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('BookWorld', '555-2345', '987 Page St, Readerville, MA');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('ToyMasters', '555-6789', '246 Play Ave, Fun City, WA');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('BuildIt Supplies', '555-1122', '159 Hammer Rd, Construct, OH');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('SoftWare House', '555-3344', '753 Code Dr, Devtown, WA');
INSERT OR IGNORE INTO Supplier (name, contact, address) VALUES ('GardenPro Inc.', '555-5566', '852 Greenway, Plantville, OR');

-- ========== Product ==========
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('ELEC-001', 'Smartphone X100', 1, 1, 699.99, 150, 10);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('OFF-002', 'Printer Paper Pack', 2, 2, 12.50, 500, 50);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('FURN-003', 'Ergonomic Chair', 3, 3, 129.99, 75, 5);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('CLTH-004', 'Men''s T-Shirt (M)', 4, 4, 19.99, 200, 20);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('KITCH-005', 'Non-stick Pan Set', 5, 5, 49.99, 120, 15);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('BOOK-006', 'Java Programming', 6, 6, 39.95, 300, 30);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('TOYS-007', 'Building Blocks Kit', 7, 7, 24.99, 180, 25);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('HWRE-008', 'Cordless Drill', 8, 8, 89.99, 60, 10);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('SOFT-009', 'Antivirus Pro License', 9, 9, 29.99, 400, 40);
INSERT OR IGNORE INTO Product (sku, name, category_id, supplier_id, unit_price, stock_qty, reorder_level) VALUES ('GARD-010', 'Garden Hose 50ft', 10, 10, 34.99, 90, 10);

-- ========== Customer ==========
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('John Doe', '555-0101', 'john.doe@example.com', '101 Elm St, Smalltown, TX');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('Jane Smith', '555-0202', 'jane.smith@example.com', '202 Oak Rd, Middletown, CA');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('ACME Corp.', '555-0303', 'sales@acmecorp.com', '303 Industrial Way, Anytown, NY');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('Globex LLC', '555-0404', 'info@globex.com', '404 Business Park, Metropolis');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('Foo Bar Inc.', '555-0505', 'contact@foobar.com', '505 Market St, Commerce City');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('The Book Nook', '555-0606', 'orders@booknook.com', '606 Read Ave, Litville');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('Toy Universe', '555-0707', 'service@toyuniverse.io', '707 Playland Rd, Funville');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('BuildRight Ltd.', '555-0808', 'support@buildright.com', '808 Workshop Ln, Hammer City');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('Soft Solutions', '555-0909', 'help@softsolutions.net', '909 Code Ct, Byteburg');
INSERT OR IGNORE INTO Customer (name, contact, email, address) VALUES ('GreenThumbs Nursery', '555-1010', 'grow@greenthumbs.org', '1010 Garden Way, Plant City');

-- ========== "Order" ==========
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (1, '2025-04-01', 1399.98, 'Completed');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (2, '2025-04-02', 125.00, 'Completed');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (3, '2025-04-03', 259.98, 'Pending');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (4, '2025-04-04', 399.99, 'New');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (5, '2025-04-05', 499.90, 'Completed');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (6, '2025-04-06', 799.80, 'Cancelled');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (7, '2025-04-07', 49.99, 'Completed');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (8, '2025-04-08', 179.97, 'Pending');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (9, '2025-04-09', 269.93, 'New');
INSERT OR IGNORE INTO "Order" (customer_id, order_date, total_amount, status) VALUES (10, '2025-04-10', 1049.89, 'Completed');

-- ========== OrderItem ==========
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (1, 1, 2, 699.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (2, 2, 10, 12.50);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (3, 3, 2, 129.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (4, 4, 1, 19.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (5, 5, 10, 49.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (6, 6, 20, 39.95);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (7, 7, 2, 24.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (8, 8, 3, 89.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (9, 9, 7, 29.99);
INSERT OR IGNORE INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (10, 10, 3, 34.99);

-- ========== Report ==========
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Stock Summary', '2025-04-01', '{"category":"Electronics"}', 'reports/stock_summary_2025-04-01.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Sales Summary', '2025-04-02', '{"start":"2025-04-01","end":"2025-04-02"}', 'reports/sales_summary_2025-04-02.csv');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Customer Orders', '2025-04-03', '{"customerId":3}', 'reports/customer_3_orders_2025-04-03.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Top Products', '2025-04-04', '{"limit":5}', 'reports/top5_products_2025-04-04.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Low Stock', '2025-04-05', '{}', 'reports/low_stock_2025-04-05.csv');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Inventory Adj', '2025-04-06', '{"date":"2025-04-06"}', 'reports/inv_adj_2025-04-06.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Sales by Cust', '2025-04-07', '{"groupBy":"customer"}', 'reports/sales_by_customer_2025-04-07.csv');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Monthly Sales', '2025-04-08', '{"month":"2025-03"}', 'reports/monthly_sales_2025-03.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Category Sales', '2025-04-09', '{"category":"Books"}', 'reports/books_sales_2025-04-09.pdf');
INSERT OR IGNORE INTO Report (report_type, generated_on, parameters, file_path) VALUES ('Year-End', '2025-04-10', '{"year":2024}', 'reports/year_end_2024.pdf');

-- ========== InventoryAdjustment ==========
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (1, '2025-04-01', -5, 'Damaged in transit');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (2, '2025-04-02', 50, 'New stock arrival');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (3, '2025-04-03', -2, 'Quality check discard');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (4, '2025-04-04', 20, 'Restock');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (5, '2025-04-05', -1, 'Customer return');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (6, '2025-04-06', 30, 'Warehouse transfer');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (7, '2025-04-07', -3, 'Theft adjustment');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (8, '2025-04-08', 15, 'Supplier bonus stock');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (9, '2025-04-09', -4, 'Expired items removed');
INSERT OR IGNORE INTO InventoryAdjustment (product_id, date, change_qty, reason) VALUES (10, '2025-04-10', 10, 'Manual correction');