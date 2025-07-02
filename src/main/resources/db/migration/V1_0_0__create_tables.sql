-- Create categories table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create articles table
CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    units INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    age INT NOT NULL DEFAULT 0,
    last_aged_date DATE NULL,
    production VARCHAR(255),
    creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    display_price VARCHAR(255),
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create movement_types table for enum values
CREATE TABLE movement_types (
    name VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

-- Create movements table
CREATE TABLE movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date TIMESTAMP NOT NULL,
    type VARCHAR(255) NOT NULL,
    units INTEGER NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (type) REFERENCES movement_types(name)
);
CREATE TABLE farm_config (
    id              SERIAL PRIMARY KEY,
    config_key      VARCHAR(50) UNIQUE NOT NULL, -- Nombre técnico del parámetro (ej: 'incubation_days')
    value           VARCHAR(100) NOT NULL,       -- Valor en texto (puede ser casteado a int/double/etc)
    description     VARCHAR(200),                -- Explicación humana para admins
    type            VARCHAR(20) DEFAULT 'STRING',-- Tipo: INT, DOUBLE, STRING, BOOL, DATE...
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50)                  -- Último usuario que lo modificó
);


