
DROP TABLE IF EXISTS parking_orders CASCADE;
DROP TABLE IF EXISTS parking_spots CASCADE;
DROP TABLE IF EXISTS rental_plans CASCADE;
DROP TABLE IF EXISTS users CASCADE;


CREATE TABLE users (
  "User_ID" SERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(10) NOT NULL DEFAULT 'USER'
);


ALTER TABLE users
  ADD CONSTRAINT users_role_check
  CHECK (role IN ('USER', 'ADMIN'));


CREATE TABLE parking_spots (
  spot_number INT PRIMARY KEY,
  "Price" INT NOT NULL DEFAULT 5000,
  is_occupied BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT spot_number_range CHECK (spot_number BETWEEN 1 AND 100)
);


CREATE TABLE rental_plans (
  id SERIAL PRIMARY KEY,
  months INT NOT NULL UNIQUE,
  price INT NOT NULL,
  CONSTRAINT rental_months_check CHECK (months IN (1,3,6))
);


CREATE TABLE parking_orders (
  id SERIAL PRIMARY KEY,

  "User_ID" INT NOT NULL,
  spot_number INT NOT NULL,

  owner_phone VARCHAR(11) NOT NULL,
  car_number VARCHAR(8) NOT NULL,

  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
  start_date TIMESTAMPTZ NOT NULL DEFAULT now(),
  end_date TIMESTAMPTZ NULL,

  plan_id INT NULL,

  CONSTRAINT owner_phone_len CHECK (length(owner_phone) = 11),
  CONSTRAINT car_number_len CHECK (length(car_number) = 8),

 
  CONSTRAINT orders_status_check CHECK (status IN ('ACTIVE', 'ENDED', 'EXPIRED')),

  
  CONSTRAINT fk_orders_user FOREIGN KEY ("User_ID")
    REFERENCES users("User_ID") ON DELETE CASCADE,

  CONSTRAINT fk_orders_spot FOREIGN KEY (spot_number)
    REFERENCES parking_spots(spot_number) ON DELETE CASCADE,

  CONSTRAINT fk_orders_plan FOREIGN KEY (plan_id)
    REFERENCES rental_plans(id) ON DELETE SET NULL
);


CREATE UNIQUE INDEX uq_active_spot
ON parking_orders(spot_number)
WHERE status = 'ACTIVE';


INSERT INTO rental_plans(months, price) VALUES
(1, 200),
(3, 550),
(6, 1000);


INSERT INTO parking_spots(spot_number, "Price", is_occupied)
SELECT gs, 5000, false
FROM generate_series(1, 100) gs;


INSERT INTO users(username, password, role)
VALUES ('Admin', 'AdminPS', 'ADMIN');

