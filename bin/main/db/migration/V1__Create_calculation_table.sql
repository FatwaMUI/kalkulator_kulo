CREATE TABLE calculation (
  id INT NOT NULL AUTO_INCREMENT,
  a VARCHAR(100) NOT NULL,
  b VARCHAR(100) NOT NULL,
  result VARCHAR(100) NOT NULL,
  -- INI KOLOM PENTING YANG KEMUNGKINAN BESAR HILANG
  created_at TIMESTAMP,
  PRIMARY KEY (id)
);