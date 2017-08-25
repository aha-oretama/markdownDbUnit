CREATE TABLE USER (
  user_id NCHAR(5) PRIMARY KEY,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  gender VARCHAR(255) NOT NULL DEFAULT 'male',
  age INTEGER
);

CREATE TABLE SCORE (
  user_id NCHAR(5) ,
  subject VARCHAR(255) NOT NULL ,
  score INTEGER ,
  FOREIGN KEY (user_id) REFERENCES USER(user_id)
);
