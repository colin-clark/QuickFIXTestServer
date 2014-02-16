CREATE DATABASE  IF NOT EXISTS `symbols` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `symbols`;
-- MySQL dump 10.13  Distrib 5.5.9, for Win32 (x86)
--
-- Host: 192.168.1.5    Database: symbols
-- ------------------------------------------------------
-- Server version	5.1.49-1ubuntu8.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `QUERY_ENTITY_FIELDS`
--

DROP TABLE IF EXISTS `QUERY_ENTITY_FIELDS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUERY_ENTITY_FIELDS` (
  `FIELD_NAME` varchar(20) DEFAULT NULL,
  `FIELD_TYPE` varchar(20) DEFAULT NULL,
  `FIELD_LENGTH` int(11) DEFAULT NULL,
  `FIELD_CLASS` varchar(20) DEFAULT NULL,
  `FIELD_DEFINITION` varchar(20) DEFAULT NULL,
  `FIELD_DESCRIPTION` varchar(20) DEFAULT NULL,
  KEY `idxQryEntity` (`FIELD_DEFINITION`,`FIELD_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QUERY_ENTITY_FIELDS`
--

LOCK TABLES `QUERY_ENTITY_FIELDS` WRITE;
/*!40000 ALTER TABLE `QUERY_ENTITY_FIELDS` DISABLE KEYS */;
--
-- Fields for ADV
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('Symbol','text',8,'String','ADV','Stock Symbol'),
('ADVToday','number',8,'Double','ADV','Todays ADV'),
('ADV30Day','number',8,'Double','ADV','The 30 day ADV');

--
-- Fields for BBO
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('Symbol','text',8,'String','BBO','Stock Symbol'),
('lowAsk','number',8,'Double','BBO','Low Ask of the day'),
('highAsk','number',8,'Double','BBO','High Ask of the day'),
('lowBid','number',8,'Double','BBO','Low Bid of the day'),
('highBid','number',8,'Double','BBO','High Bid of the day');

--
-- Fields for FIXExecutionReport
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('TargetCompID','text',8,'String','FIXExecutionReport','Target comp ID'),
('SenderCompID','text',8,'String','FIXExecutionReport','Sender Comp ID'),
('LastShares','number',10,'Long','FIXExecutionReport','Shares Traded'),
('AvgPx','number',8,'Double','FIXExecutionReport','Average Price of Exe'),
('ClOrdID','text',50,'String','FIXExecutionReport','Client Order ID'),
('CumQty','number',8,'Long','FIXExecutionReport','Total quantity execu'),
('ExecID','text',8,'String','FIXExecutionReport','Execution ID'),
('Symbol','text',8,'String','FIXExecutionReport','Symbol'),
('ExecTransType','text',8,'String','FIXExecutionReport','Execution Transactio'),
('ExecType','text',8,'String','FIXExecutionReport','Execution Type'),
('Leaves','number',8,'Long','FIXExecutionReport','Outstanding balance'),
('OrderID','text',8,'String','FIXExecutionReport','execution id'),
('LastPx','number',8,'Double','FIXExecutionReport',NULL),
('Firm', 'text', 50, 'String', 'FIXExecutionReport','Originating Firm'),
('OrderQty','number',8,'Long','FIXExecutionReport','Original quantitys');

--
-- Fields for FIXNewOrderSingle
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('ClOrdID','text',50,'String','FIXNewOrderSingle','Client Order ID'),
('Symbol','text',8,'String','FIXNewOrderSingle','Stock Symbol'),
('Firm', 'text', 50, 'String', 'FIXNewOrderSingle','Originating Firm'),
('OrderQty','number',8,'Long','FIXNewOrderSingle','Order Quantity'),
('Price','number',8,'Double','FIXNewOrderSingle','Order Price'),
('TargetCompID','text',8,'String','FIXNewOrderSingle','Target comp ID'),
('SenderCompID','text',8,'String','FIXNewOrderSingle','Sender Comp ID'),
('Side','number',5,'Long','FIXNewOrderSingle','Buy/Sell'),
('Account','text',50,'String','FIXNewOrderSingle','Broker Account'),
('TransactTime','text',50,'String','FIXNewOrderSingle','Transaction time'),
('TimeInForce','text',8,'String','FIXNewOrderSingle','Time in Force (TIF)'),
('OrdType','text',5,'String','FIXNewOrderSingle','Type of Order');

--
-- Fields for FIXOrderAck
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('TargetCompID','text',8,'String','FIXOrderAck','Target comp ID'),
('SenderCompID','text',8,'String','FIXOrderAck','Sender Comp ID'),
('Firm', 'text', 50, 'String', 'FIXOrderAck','Originating Firm'),
('ClOrdID','text',50,'String','FIXOrderAck','Client Order ID'),
('ExecID','text',8,'String','FIXOrderAck','Execution ID'),
('Symbol','text',8,'String','FIXOrderAck','Symbol'),
('OrderID','text',10,'String','FIXOrderAck','');

--
-- Fields for NYSESAX
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('symbol','text',8,'String','NYSESAX','Symbol'),
('TimeStamp','number',10,'Long','NYSESAX','ms since epoch'),
('SAXCode','text',10,'String','NYSESAX','SAX code');

--
-- Fields for NYSETradeEvent
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('date','text',8,'String','NYSETradeEvent','date'),
('time','text',8,'String','NYSETradeEvent','time'),
('ms','number',5,'Long','NYSETradeEvent','milliseconds'),
('symbol','text',5,'String','NYSETradeEvent','stock symbol'),
('volume','number',8,'Long','NYSETradeEvent','trade volume'),
('price','number',8,'Double','NYSETradeEvent','trade price');

--
-- Fields for trfIN
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('AvgPx','number',10,'Double','trfIN',NULL),
('ClOrdID','number',10,'Long','trfIN',NULL),
('Shares','Number',10,'Long','trfIN',NULL),
('ExecID','text',10,'String','trfIN',NULL),
('ExecTransType','text',10,'String','trfIN',NULL),
('MsgType','text',5,'String','trfIN',NULL),
('OrderID','text',10,'String','trfIN',NULL),
('OrdStatus','text',5,'String','trfIN',NULL),
('SenderCompID','text',5,'String','trfIN',NULL),
('SendingTime','text',12,'String','trfIN',NULL),
('Side','number',5,'Long','trfIN',NULL),
('Symbol','text',8,'String','trfIN',NULL),
('TargetCompID','text',8,'String','trfIN',NULL),
('TransactTime','text',10,'String','trfIN',NULL),
('TradeDate','text',10,'String','trfIN',NULL),
('ProcessCode','number',10,'Long','trfIN',NULL),
('ExecType','text',10,'String','trfIN',NULL),
('LeavesQty','number',10,'Long','trfIN',NULL),
('OrderCapacity','text',5,'String','trfIN',NULL),
('TradeReportID','text',15,'String','trfIN',NULL),
('TrdType','number',5,'Long','trfIN',NULL),
('TrdSubType','number',5,'Long','trfIN',NULL),
('PublishTrdIndicator','text',5,'String','trfIN',NULL),
('ShortSaleReason','number',5,'Long','trfIN',NULL),
('TradeReportType','number',5,'Long','trfIN',NULL),
('TrdRptStatus','number',5,'Long','trfIN',NULL),
('AsOfIndicator','text',5,'String','trfIN',NULL),
('TRFPO','text',5,'String','trfIN',NULL),
('BranchSeqNbr','text',10,'String','trfIN',NULL),
('TradeTypeCategory4','number',5,'Long','trfIN',NULL),
('TradeTypeCategory3','number',5,'Long','trfIN',NULL),
('TradeCategory3','number',5,'Long','trfIN',NULL),
('TradeCategory4','number',5,'Long','trfIN',NULL),
('TradeTypeCategory2','number',5,'Long','trfIN',NULL),
('TRDSubType','number',5,'Long','trfIN',NULL),
('TRFPriceOverride','text',5,'String','trfIN',NULL),
('ReversalIndicator','text',5,'String','trfIN',NULL),
('ClearingInstruction','number',10,'Long','trfIN',NULL),
('SecondaryTrdType','text',10,'String','trfIN',NULL),
('Firm','text',10,'String','trfIN',NULL),
('ContraTradePA','text',10,'String','trfIN',NULL);

--
-- Fields for trfOUT
--
INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('AsOfIndicator','text',5,'String','trfOUT',NULL),
('AvgPx','number',10,'Double','trfOUT',NULL),
('BranchSeqNbr','text',10,'String','trfOUT',NULL),
('ClearingInstruction','text',5,'String','trfOUT',NULL),
('ClOrdID','number',10,'Long','trfOUT',NULL),
('ContraTradePA','text',10,'String','trfOUT',NULL),
('ExecID','text',10,'String','trfOUT',NULL),
('ExecTransType','text',10,'String','trfOUT',NULL),
('ExecType','text',10,'String','trfOUT',NULL),
('LeavesQty','number',10,'Long','trfOUT',NULL),
('MsgType','text',5,'String','trfOUT',NULL),
('OrderCapacity','text',5,'String','trfOUT',NULL),
('OrderID','text',10,'String','trfOUT',NULL),
('OrdStatus','text',5,'String','trfOUT',NULL),
('ProcessCode','number',10,'Long','trfOUT',NULL),
('PublishTrdIndicator','text',5,'String','trfOUT',NULL),
('SenderCompID','text',5,'String','trfOUT',NULL),
('SendingTime','text',12,'String','trfOUT',NULL),
('Shares','Number',10,'Long','trfOUT',NULL),
('ShortSaleReason','number',5,'Long','trfOUT',NULL),
('Side','number',5,'Long','trfOUT',NULL),
('Symbol','text',8,'String','trfOUT',NULL),
('TargetCompID','text',8,'String','trfOUT',NULL),
('TradeDate','text',10,'String','trfOUT',NULL),
('TradeReportID','text',15,'String','trfOUT',NULL),
('TradeReportType','number',5,'Long','trfOUT',NULL),
('TransactTime','text',10,'String','trfOUT',NULL),
('TrdRptStatus','number',5,'Long','trfOUT',NULL),
('TrdSubType','number',5,'Long','trfOUT',NULL),
('TrdType','number',5,'Long','trfOUT',NULL),
('TRFPO','text',5,'String','trfOUT',NULL),
('Firm','text',10,'String','trfOUT',NULL),
('ReversalIndicator','text',5,'String','trfOUT',NULL);
 
/*!40000 ALTER TABLE `QUERY_ENTITY_FIELDS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QUERY_ENTITY`
--

DROP TABLE IF EXISTS `QUERY_ENTITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QUERY_ENTITY` (
  `ENTITY_NAME` varchar(20) NOT NULL,
  `ENTITY_DESCRIPTION` varchar(20) DEFAULT NULL,
  `ENTITY_QUERY_KEY` varchar(20) DEFAULT NULL,
  `ENTITY_FIELD_DEFINITION` varchar(20) DEFAULT NULL,
  `ENTITY_TYPE` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ENTITY_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QUERY_ENTITY`
--

LOCK TABLES `QUERY_ENTITY` WRITE;
/*!40000 ALTER TABLE `QUERY_ENTITY` DISABLE KEYS */;
INSERT INTO `QUERY_ENTITY` VALUES 
('Exceptions','All Possible Excepti','Exceptions','Exceptions','VARIANT'),
('FIXExecutionReport','FIX Execution Report','FIXExecutionReport','FIXExecutionReport','MAP'),
('FIXNewOrderSingle','FIX Orders','FIXNewOrderSingle','FIXNewOrderSingle','MAP'),
('FIXOrderAck','FIX Order Ack\'s','FIXOrderAck','FIXOrderAck','MAP'),
('NYSESAX','SAX codes for NYSE T','Trades 5xM10','NYSESAX','MAP'),
('NYSETradeEvent','NYSE Trade Prints','NYSETradeEvent','NYSETradeEvent','MAP'),
('trfIN','TRF Inbound Log','trfIN','trfIN','MAP'),
('trfOUT','TRF Outbound Lod','trfOUT','trfOUT','MAP'),
('ADV','Average Daily Volume','ADV','ADV','MAP'),
('BBO','Best Bid and Offer','BBO','BBO','MAP');
/*!40000 ALTER TABLE `QUERY_ENTITY` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-06-30 16:37:26
