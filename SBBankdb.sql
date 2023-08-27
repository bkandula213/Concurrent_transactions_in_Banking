CREATE DATABASE  IF NOT EXISTS `sbbankdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `sbbankdb`;
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: localhost    Database: bankdb
-- ------------------------------------------------------
-- Server version	8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_tbl`
--

DROP TABLE IF EXISTS `account_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_tbl` (
  `acc_No` int NOT NULL AUTO_INCREMENT,
  `acc_HolderName` varchar(45) NOT NULL,
  `acc_Balance` int NOT NULL,
  `acc_PhoneNo` int NOT NULL,
  `acc_Email` varchar(45) NOT NULL,
  `acc_Password` varchar(128) NOT NULL,
  PRIMARY KEY (`acc_No`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;




--
-- Table structure for table `transaction_tbl`
--

DROP TABLE IF EXISTS `transaction_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_tbl` (
  `trans_ID` int NOT NULL AUTO_INCREMENT,
  `sender` int NOT NULL,
  `receiver` int NOT NULL,
  `amount` int NOT NULL,
  `verification_Status` varchar(10) NOT NULL,
  `initiation_Date` timestamp NOT NULL,
  `message` varchar(255) NOT NULL,
  PRIMARY KEY (`trans_ID`),
  KEY `index2` (`sender`) /*!80000 INVISIBLE */,
  KEY `index3` (`receiver`),
  CONSTRAINT `FK_from` FOREIGN KEY (`sender`) REFERENCES `account_tbl` (`acc_No`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_to` FOREIGN KEY (`receiver`) REFERENCES `account_tbl` (`acc_No`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `transaction_tbl_AFTER_INSERT` AFTER INSERT ON `transaction_tbl` FOR EACH ROW BEGIN
DECLARE sum_suspiciousentry DECIMAL(10,2) DEFAULT 0;
declare send int;
declare timer timestamp(6);
set timer=new.initiation_Date;
set send = new.sender;
select count(*) INTO sum_suspiciousentry FROM transaction_tbl WHERE `sender`=send and `initiation_Date`=timer;
if sum_suspiciousentry>1 then 
	/*
    update `transaction_tbl`
    set new.verification_Status='flagged'
    where `sender`=send and `initiation_Date`=timer;
    */
	insert into `flaggedtransactions_tbl` (`trans_ID`)
	select (`trans_ID`) from `transaction_tbl`
	WHERE `sender`=send and `initiation_Date`=timer;
end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `transaction_tbl_AFTER_UPDATE` AFTER UPDATE ON `transaction_tbl` FOR EACH ROW /*transferring amount*/
BEGIN
declare amnt int;
declare sendr int;
declare recepient int;
declare senderoldbalance int;
declare receiveroldbalance int;
declare sendernewbalance int;
declare receivernewbalance int;
declare transstatus varchar(10);
set amnt=new.amount;
set sendr=new.sender;
set recepient=new.receiver;
set transstatus =new.verification_Status;
select acc_Balance INTO senderoldbalance FROM account_tbl WHERE acc_No=sendr;
select acc_Balance INTO receiveroldbalance FROM account_tbl WHERE acc_No=recepient;
set sendernewbalance=senderoldbalance-amnt;
set receivernewbalance=receiveroldbalance+amnt;
if transstatus='verified' and sendernewbalance>=0 then
	update account_tbl
	set acc_Balance=sendernewbalance
	where acc_No=sendr;
	update account_tbl
	set acc_Balance=receivernewbalance
	where acc_No=recepient;
end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `flaggedtransactions_tbl`
--

DROP TABLE IF EXISTS `flaggedtransactions_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flaggedtransactions_tbl` (
  `idflaggedtransactions_tbl` int NOT NULL AUTO_INCREMENT,
  `trans_ID` int NOT NULL,
  PRIMARY KEY (`idflaggedtransactions_tbl`),
  KEY `idx_trans` (`trans_ID`),
  CONSTRAINT `FK_trans` FOREIGN KEY (`trans_ID`) REFERENCES `transaction_tbl` (`sender`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verification_tbl`
--

DROP TABLE IF EXISTS `verification_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_tbl` (
  `verification_ID` int NOT NULL AUTO_INCREMENT,
  `trans_ID` int NOT NULL,
  `verification_Code` varchar(6) NOT NULL,
  `request_Date` timestamp NOT NULL,
  `request_TTD` timestamp NOT NULL,
  `code_Status` varchar(10) NOT NULL,
  PRIMARY KEY (`verification_ID`),
  CONSTRAINT `FK_transact` FOREIGN KEY (`trans_ID`) REFERENCES `transaction_tbl` (`trans_ID`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'bankdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-03-02  2:16:45
