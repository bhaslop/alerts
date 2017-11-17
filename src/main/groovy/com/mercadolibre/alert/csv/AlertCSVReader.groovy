package com.mercadolibre.alert.csv

import com.mercadolibre.alert.Alert
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.nio.file.Paths

class AlertCSVReader {

	public List<Alert> readFile(String filePath) {
		List<Alert> alerts = []

		Paths.get(filePath).withReader { reader ->
			CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())

			for( CSVRecord record in parser.iterator() ) {
				alerts.add(Alert.createAlert(record))
			}
		}

		return alerts
	}
}
