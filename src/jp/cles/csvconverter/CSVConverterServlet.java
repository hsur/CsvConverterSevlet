package jp.cles.csvconverter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.opencsv.CSVReader;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

//Based on https://yoshio3.com/2010/03/11/

@WebServlet(name = "convertcsv", urlPatterns = { "/convertcsv" })
@MultipartConfig(fileSizeThreshold = 5000000, maxFileSize = 10000000)
public class CSVConverterServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		PrintWriter out = response.getWriter();
		Part part = request.getPart("content");
		String name = "変換済" + getFilename(part);
		CSVReader csv = new CSVReader(new InputStreamReader(part.getInputStream()));

		System.err.println(request.getHeader("User-Agent"));
		if (request.getHeader("User-Agent").indexOf("MSIE") == -1 && request.getHeader("User-Agent").indexOf("Trident") == -1) {
			response.setHeader("Content-Disposition", "attachment; filename=" + MimeUtility.encodeWord(name, "ISO-2022-JP", "B"));
		} else {
			response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(name, "UTF-8"));
		}

		for (String[] strings : csv.readAll()) {
			DateFormat idf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date d = null;
			try {
				d = idf.parse(strings[0]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			DateFormat odf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			int num = Integer.parseInt(strings[1]);
			num += 10;
			out.printf("%s,%d\n", odf.format(d), num);
		}
		csv.close();
		out.flush();
		out.close();
	}

	private String getFilename(Part part) {
		for (String cd : part.getHeader("Content-Disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
}