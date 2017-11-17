package com.mercadolibre.alert

import org.apache.commons.csv.CSVRecord

import java.util.regex.Matcher

class Alert {
	String alias
	String message

	String pool = 'unknown'
	String app
	String type

	Date created

	String team
	String escalation
	String owner

	public void setCreated(String date) {
		this.created = getDate(date)
	}

	private Date getDate(String date) {
		Calendar calendar = new GregorianCalendar()

		calendar.setTimeInMillis(new Long(date))

		return calendar.getTime()
	}

	public static createAlert(CSVRecord record) {
		Alert alert = new Alert()

		alert.alias = record.get(1)

		alert.parsePool(alert.alias)

		alert.message = record.get(3)
		alert.setCreated(record.get(8))
		alert.owner = record.get(13)
		alert.team = record.get(14)
		alert.escalation = record.get(15)

		return alert
	}

	private void parsePool(String alias) {
		if( alias.contains('|') ) {
			this.type = 'datadog'

			Matcher matcher = (alias =~ /.*application:(.*)?,scope:(.*)/)

			this.app = matcher[0][1]
			this.pool = matcher[0][2]

		} else {
			if( alias.contains('.') ) {
				String[] data = alias.split('\\.')

				this.pool = data[0]
				this.app = data[1]
				this.type = data[2]
			} else {
				def uuid = (alias ==~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/)

				if( !uuid ) {
					String[] data = alias.split('-')

					this.app = data[0] + "-" + data[1]
					if( data.length == 5 ) {
						this.pool = data[2]
					} else {
						if( data.length > 3 ) {
							this.pool = data[3]
						}
					}
				} else {
					this.app = 'unknown'
					this.type = 'noc'
				}
			}
		}
	}

	@Override
	String toString() {
		return "${this.app}-${this.pool}-${this.message}"
	}
}
