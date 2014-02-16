package com.cep.darkstar.utility.cassandra;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;

public class SendToFile implements MessageReaderCallback {
	public class LatencyBreakdown {
		public long totalLatency;
		public long latency_1;
		public long latency_2;
		public long latency_3;
		public long latency_4;
		public String order_id;
	}

	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.SendToFile");
	private String fileName;
	private BufferedWriter writer;
	private SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
	private SimpleDateFormat shortFormatColon = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
	private SimpleDateFormat reallyShortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
	private HashSet<String> dateFields = new HashSet<String>();
	
	// latency b/w TransactTime in FIX message and _onrcvTS in Plugin
	private long latency_1 = 0;
	private String order_id_1 = null;
	private double average_latency_1 = 0;
	private int average_latency_1_count = 0;
	// latency b/w _onrcvTS in Plugin and _onsndTS in Plugin
	private long latency_2 = 0;
	private String order_id_2 = null;
	private double average_latency_2 = 0;
	private int average_latency_2_count = 0;
	// latency b/w _onsndTS and ds_timestamp in DS
	private long latency_3 = 0;
	private String order_id_3 = null;
	private double average_latency_3 = 0;
	private int average_latency_3_count = 0;
	// latency b/w ds_timestamp in DS and _dtoReceived timestamp
	private long latency_4 = 0;
	private String order_id_4 = null;
	private double average_latency_4 = 0;
	private int average_latency_4_count = 0;
	
	private long total_latency = 0;
	
	private LatencyBreakdown highestLatency = new LatencyBreakdown();
	
	private long TransactTime = 0;
	private long _onrcvTS = 0;
	private long _onsndTS = 0;
	private long ds_timestamp = 0;
	private long _dtoReceived = 0;
	
	private String order_id = null;
	private List<String> headers = new ArrayList<String>();
	private boolean useTransactTime = true;

	public SendToFile(CassandraClientConfigInfo info) {
		// Create file 
		FileWriter fstream;
		try {
			useTransactTime = info.isUseTransactTime();
			setHeaders();
			setDateFields();
			fileName = info.getFile();
			logger.info("Creating file " + fileName);
			fstream = new FileWriter(fileName);
			writer = new BufferedWriter(fstream);
			logger.info("File " + fileName + " created");
			//headers = info.getHeaders();
			for(String header : headers) {
				writer.write(header+",");
			}
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}


	private void setDateFields() {
		dateFields.add("_onrcvTS");
		dateFields.add("_onsndTS");
		dateFields.add("ds_timestamp");
		dateFields.add("_dtoReceived");
		if (useTransactTime) dateFields.add("TransactTime");	
	}


	private void setHeaders() {
		if (useTransactTime) headers.add("TransactTime");
		headers.add("RowCount");
		headers.add("_onrcvTS");
		headers.add("_onsndTS");
		headers.add("ds_timestamp");
		headers.add("_dtoReceived");
		headers.add("ClOrdID");	
	}


	@Override
	public void send(EventObject event) throws JSONException {
		if (logger.isDebugEnabled()) {
			logger.debug(event);
		}
		try {
			order_id = event.getString("ClOrdID");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		try {
			for (Entry<String, Object> field : event.getMap().entrySet()) {
				if (dateFields.contains(field.getKey())) {
					long timestamp = 0;
					if (field.getKey().equals("TransactTime")) {
						try {
							String timestring = (String) field.getValue();
							if (timestring.length() > 17) {
								if (timestring.charAt(17) == ':') {
									timestamp = shortFormatColon.parse((String) field.getValue()).getTime();
								} else {
									timestamp = shortFormat.parse((String) field.getValue()).getTime();
								}
							} else if (timestring.length() == 16) {
								timestamp = reallyShortFormat.parse((String) field.getValue()).getTime();
							}
						} catch (ParseException e) {
							logger.error(e.getMessage(), e);
						}
					} else if (field.getValue().getClass().equals(Long.class)) {
						timestamp = (Long) field.getValue();
					} else {
						timestamp = Long.valueOf((String) field.getValue());
					}
					String formatted = shortFormat.format(new Date(timestamp));
					event.put(field.getKey(), formatted);
					logLatency(field.getKey(), timestamp);
				}
			}
			determineTotalLatency();
		} catch (Exception e) {
			logger.error("Exception caught translating time fields: " + e.getMessage(), e);
		}
		try {
			writeLine(event);
		} catch (Exception e) {
			logger.error("Exception writing event to file: " + e.getMessage(), e);
		}
		refresh();
	}
	
	private void writeLine(EventObject event) throws IOException, JSONException {
		for (String header : headers) {
			if (event.has(header)) {
				writer.write(event.get(header).toString());
				writer.write(",");
			}
		}
		writer.newLine();
		writer.flush(); 
	}

	private void determineTotalLatency() {
		if (total_latency > highestLatency.totalLatency) {
			highestLatency.totalLatency = total_latency;
			highestLatency.order_id = order_id;
			highestLatency.latency_1 = (_onrcvTS > 0 && TransactTime > 0) ? _onrcvTS - TransactTime : 0;
			highestLatency.latency_2 = (_onsndTS > 0 && _onrcvTS > 0) ? _onsndTS - _onrcvTS : 0;
			highestLatency.latency_3 = (ds_timestamp > 0 && _onsndTS > 0) ? ds_timestamp - _onsndTS : 0;
			highestLatency.latency_4 = (_dtoReceived > 0 && ds_timestamp > 0) ? _dtoReceived - ds_timestamp : 0;
		}
	}

	private void refresh() {
		TransactTime = 0;
		_onrcvTS = 0;
		_onsndTS = 0;
		ds_timestamp = 0;
		_dtoReceived = 0;
		total_latency = 0;
	}
	
	private void setTime(long receive, long send, int which) {
		long latency = receive - send;
		total_latency += latency;
		switch (which) {
		case 1: 
			average_latency_1 += latency;
			average_latency_1_count++;
			if (latency > latency_1) {
				latency_1 = latency;
				order_id_1 = order_id;
			}
			break;
		case 2:
			average_latency_2 += latency;
			average_latency_2_count++;
			if (latency > latency_2) {
				latency_2 = latency;
				order_id_2 = order_id;
			}
			break;
		case 3:
			average_latency_3 += latency;
			average_latency_3_count++;
			if (latency > latency_3) {
				latency_3 = latency;
				order_id_3 = order_id;
			}
			break;
		case 4:
			average_latency_4 += latency;
			average_latency_4_count++;
			if (latency > latency_4) {
				latency_4 = latency;
				order_id_4 = order_id;
				break;
			}
		}
		
	}

	private void logLatency(String fieldName, long time) {
		if (fieldName.equals("TransactTime")) {
			TransactTime = time;
			if (_onrcvTS > 0) {
				setTime(_onrcvTS, TransactTime, 1);
			}
		} else if (fieldName.equals("_onrcvTS")) {
			_onrcvTS = time;
			if (TransactTime > 0) {
				setTime(_onrcvTS, TransactTime, 1);
			}
			if (_onsndTS > 0) {
				setTime(_onsndTS, _onrcvTS, 2);
			}
		} else if (fieldName.equals("_onsndTS")) {
			_onsndTS = time;
			if (_onrcvTS > 0) {
				setTime(_onsndTS, _onrcvTS, 2);
			}
			if (ds_timestamp > 0) {
				setTime(ds_timestamp, _onsndTS, 3);
			}
		} else if (fieldName.equals("ds_timestamp")) {
			ds_timestamp = time;
			if (_onsndTS > 0) {
				setTime(ds_timestamp, _onsndTS, 3);
			}
			if (_dtoReceived > 0) {
				setTime(_dtoReceived, ds_timestamp, 4);
			}
		} else if (fieldName.equals("_dtoReceived")) {
			_dtoReceived = time;
			if (ds_timestamp > 0) {
				setTime(_dtoReceived, ds_timestamp, 4);
			}
		}
		
	}



	@Override
	public void finished() {
		try {
			double totalAvg = 0.0;
			int divisor = 0;
			if (average_latency_1 > 0 && average_latency_1_count > 0) {
				logger.info("High latency between TransactTime and time received by plugin: " + latency_1);
				logger.info("Client Order ID: " + order_id_1);
				logger.info("Average latency between TransactTime and time received by plugin: " + 
						(average_latency_1/average_latency_1_count));
				totalAvg += (average_latency_1/average_latency_1_count);
				divisor++;
			} else {
				logger.info("High latency between TransactTime and time received by plugin cannot be calculated and will" +
						" not be used in the calculation of total average latency");
			}
			if (average_latency_2 > 0 && average_latency_2_count > 0) {
				logger.info("High latency between time received by plugin and time sent by plugin: " + latency_2);
				logger.info("Client Order ID: " + order_id_2);
				logger.info("Average latency between time received by plugin and time sent by plugin: " + 
						(average_latency_2/average_latency_2_count));
				totalAvg += (average_latency_2/average_latency_2_count);
				divisor++;
			} else {
				logger.info("High latency between time received by plugin and time sent by plugin cannot be calculated and will" +
						" not be used in the calculation of total average latency");
			}
			if (average_latency_3 > 0 && average_latency_3_count > 0) {
				logger.info("High latency between time sent by plugin and time received by DarkStar: " + latency_3);
				logger.info("Client Order ID: " + order_id_3);
				logger.info("Average latency between time sent by plugin and time received by DarkStar: " + 
						(average_latency_3/average_latency_3_count));
				totalAvg += (average_latency_3/average_latency_3_count);
				divisor++;
			} else {
				logger.info("High latency between time sent by plugin and time received by DarkStar cannot be calculated and will" +
						" not be used in the calculation of total average latency");
			}
			if (average_latency_4 > 0 && average_latency_4_count > 0) {
				logger.info("High latency between time received by DarkStar and time queued to Cassandra: " + latency_4);
				logger.info("Client Order ID: " + order_id_4);
				logger.info("Average latency between time received by DarkStar and time queued to Cassandra: " + 
						(average_latency_4/average_latency_4_count));
				totalAvg += (average_latency_4/average_latency_4_count);
				divisor++;
			} else {
				logger.info("High latency between time received by DarkStar and time queued to Cassandra cannot be calculated and will" +
						" not be used in the calculation of total average latency");
			}
			
			logger.info("Average total latency: " + Math.round(totalAvg) + " ms");
			
			logger.info("Highest single message latency was " + highestLatency.totalLatency +", for message " + highestLatency.order_id);
			logger.info("Highest single message latency breakdown:\n\tlatency 1: " + highestLatency.latency_1 + "\n\tlatency 2: " + highestLatency.latency_2 + 
					"\n\tlatency 3: " + highestLatency.latency_3 + "\n\tlatency 4: " + highestLatency.latency_4);
			
			logger.info("Flushing file writer");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
