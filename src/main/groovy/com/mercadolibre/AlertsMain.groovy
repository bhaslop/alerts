package com.mercadolibre

import com.mercadolibre.alert.Alert
import com.mercadolibre.alert.csv.AlertCSVReader
import com.mercadolibre.alert.excel.ExcelService

class AlertsMain {

	static main(args) {
		AlertCSVReader csvReader = new AlertCSVReader()

		List<Alert> rawAlerts = csvReader.readFile("/Users/bhaslop/alerts-2017_11_16.csv")

		ExcelService excelService = new ExcelService("alerts-20127_11_16")

		createSummary(excelService, rawAlerts)

		excelService.save()
	}

	private static void createSummary(ExcelService excelService, List<Alert> alerts) {
		Map<String, List<Alert>> alertsByDate = alerts.groupBy({ Alert alert ->
			getWeekDate(alert.created).format("dd/MM/yyyy")
		})

		List<String> apps = alerts.unique { Alert  alert ->
			alert.app
		}.collect { it.app }

		Date firstAlertDate = alerts.min { Alert alert ->
			alert.created
		}.created

		List<Date> weeks = getAllWeeksFrom(firstAlertDate)

		weeks.each { Date date ->
			String formatedDate = date.format("dd/MM/yyyy")
			if( !alertsByDate.get(formatedDate) ) {
				alertsByDate.put(formatedDate, [])
			}
		}

		excelService.createSummarySheet(apps, alertsByDate)
	}

	private static List<Date> getAllWeeksFrom(Date date) {
		Date weekDate = getWeekDate(date)
		Date now = getWeekDate(new Date())

		List<Date> results = []

		while( weekDate <= now+1 ) {
			results.add(weekDate)
			weekDate += 7
		}

		return results
	}

	private static void createAppSheets() {
		/*
		Map<String, Map<String, List<Alert>>> alertsByAppAndDate = rawAlerts.groupBy({Alert alert ->
			alert.app
		}, { Alert alert ->
			getWeekDate(alert.created).format("dd/MM/yyyy")
		})
		*/

		/*
		Map<String, Map<String, List<Alert>>> alertsByAppAndScope = rawAlerts.groupBy({ Alert alert ->
			alert.app
		}, { Alert alert ->
			alert.pool
		})
		*/

		//excelService.createExcel("alerts-11_08_2017", alertsByAppAndDate, alertsByAppAndScope, alertsByDate)
	}

	private static Date getWeekDate(Date date) {
		GregorianCalendar cal = new GregorianCalendar(new Locale("en", "GB")) //first day of week monday hack
		cal.setTimeInMillis(date.getTime())

		cal.set(Calendar.YEAR, 2017)
		cal.set(Calendar.HOUR, 0)
		cal.set(Calendar.MINUTE, 0)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

		return cal.getTime()

	}

}
