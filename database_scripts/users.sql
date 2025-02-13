USE fit_hub;
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(45) NOT NULL,
  `password` varchar(200) NOT NULL,
  `birth_date` date NOT NULL,
  PRIMARY KEY (`username`),
  UNIQUE KEY `username` (`username`)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
