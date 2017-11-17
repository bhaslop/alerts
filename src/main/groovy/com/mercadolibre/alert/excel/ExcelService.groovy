package com.mercadolibre.alert.excel

import com.mercadolibre.alert.Alert
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import java.text.SimpleDateFormat

class ExcelService {
	private Workbook wb = new SXSSFWorkbook()
	private String filename

	public ExcelService(String filename) {
		this.filename = filename
	}

	public void save() {
		FileOutputStream file = new FileOutputStream("/Users/bhaslop/${filename}.xlsx")
		wb.write(file)
		file.close()
	}

	public void createSummarySheet(List<String> apps, Map<String, List<Alert>> alertsByDate) {
		Sheet byAppSheet = wb.createSheet("summary")

		CellStyle headingStyle = getHeadingCellStyle(wb)

		int row = 0

		Map<String, Integer> appIndexes = [:]

		Row headingRow = byAppSheet.createRow(row++)

		Cell cell = headingRow.createCell(0)
		cell.setCellValue("raw-date")
		cell.setCellStyle(headingStyle)

		cell = headingRow.createCell(1)
		cell.setCellValue("date")
		cell.setCellStyle(headingStyle)

		apps.eachWithIndex { String app, int i ->
			int index = i+2

			appIndexes.put(app, index)

			cell = headingRow.createCell(index)

			cell.setCellValue(app)
			cell.setCellStyle(headingStyle)
		}

		headingRow.createCell(apps.size()+1).setCellValue("Total")

		alertsByDate.each{ String date, List<Alert> alerts ->
			Map<String, Integer> countByApp = alerts.countBy({ Alert alert ->
				alert.app
			})

			Row alertRow = byAppSheet.createRow(row++)

			alertRow.createCell(0).setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(date).getTime())
			alertRow.createCell(1).setCellValue(date)

			countByApp.each { String app, Integer count ->
				int scopeIndex = appIndexes.get(app)

				alertRow.createCell(scopeIndex).setCellValue(count)
			}

			alertRow.createCell(apps.size()+1).setCellValue(alerts.size())
		}
	}

	private void createAppSheets(Workbook wb, Map<String, Map<String, List<Alert>>> alertByAppAndDate, Map<String, Map<String, List<Alert>>> alertsByAppAndScope) {
		alertByAppAndDate.each { String app, Map<String, List<Alert>> datedAlerts ->
			Sheet sheet = wb.createSheet(app)

			int row = 0
			CellStyle headingStyle = getHeadingCellStyle(wb)

			Row headingRow = sheet.createRow(row++)

			Set<String> scopes = alertsByAppAndScope.get(app).keySet().sort()

			Map<String, Integer> scopeIndexes = [:]

			Cell cell = headingRow.createCell(0)
			cell.setCellValue("raw-date")
			cell.setCellStyle(headingStyle)

			cell = headingRow.createCell(1)
			cell.setCellValue("date")
			cell.setCellStyle(headingStyle)

			scopes.eachWithIndex{ String scope, int i ->
				int index = i+2

				scopeIndexes.put(scope, index)

				cell = headingRow.createCell(index)

				cell.setCellValue(scope)
				cell.setCellStyle(headingStyle)
			}

			datedAlerts.each { String date, List<Alert> alerts ->
				Map<String, Integer> countByScope = new TreeMap<String, Integer>(alerts.countBy({ Alert alert ->
					alert.pool
				}))

				Row alertRow = sheet.createRow(row++)

				alertRow.createCell(0).setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(date).getTime())
				alertRow.createCell(1).setCellValue(date)

				countByScope.each { String scope, Integer count ->
					int scopeIndex = scopeIndexes.get(scope)

					alertRow.createCell(scopeIndex).setCellValue(count)
				}
			}

		}
	}

	private CellStyle getHeadingCellStyle(Workbook wb) {
		Font font = wb.createFont()
		font.setFontHeightInPoints((short)14)
		font.setBold(true)

		CellStyle style = wb.createCellStyle()
		style.setFont(font)

		return style
	}
}
