import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inicio {

	public static void main(String[] args) {

		Connection BaseDatos = null;
		Statement st = null;

		// Donde se localiza la base de datos
		String url = "jdbc:postgresql://172.26.65.137:5432/MXIntegrity";

		// Credenciales de la base de datos
		String usuario = "root";
		String contrasena = "postgres";

		try {
			// Conexion con la base de datos
			BaseDatos = DriverManager.getConnection(url, usuario, contrasena);
			st = BaseDatos.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM public.\"MXErrorSolucion\"");

			ArrayList<Error> errores = prepararTXT("D:\\integcheck.txt");
			ArrayList<String> consultas = new ArrayList<>();

			Map<String, String> erroresDB = new HashMap<String, String>();

			while (rs.next()) {
				String error = rs.getString("error");
				String solucion = rs.getString("solucion");
				erroresDB.put(error, solucion);
			}

			for (int i = 0; i < errores.size(); i++) {

				String solucions = "" + erroresDB.get(errores.get(i).getError());
				String descr = "/* \n" + errores.get(i).getError() + " --- " + errores.get(i).getDescrpcion() + " "
						+ errores.get(i).getObjeto() + "." + errores.get(i).getAtributo() + "\n */";
				String consulta = String.format(solucions, errores.get(i).getObjeto(), errores.get(i).getAtributo());
				if (!solucions.trim().equals("null")) {
					consultas.add(descr);
					consultas.add(consulta);
				}
			}
			GenerarScript(consultas);
			rs.close();
			st.close();
			BaseDatos.close();
		} catch (FileNotFoundException e) {
			System.out.println("Fichero no encontrado...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Fichero no se pudo leer...");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Error al conectar BD...");
			e.printStackTrace();
		}

	}

	static void GenerarScript(ArrayList<String> consulta) {
		try {
			// Escribir ficheros
			FileWriter script = new FileWriter("d:/script.txt");
			PrintWriter err = new PrintWriter(script);
			for (int i = 0; i < consulta.size(); i++) {
				if (!consulta.get(i).equals(null)) {
					err.println(consulta.get(i));
					err.println();
				}
			}
			err.close();
			script.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static ArrayList<Error> prepararTXT(String archivo) throws FileNotFoundException, IOException {
		String cadena;
		// Exp Regulares
		Pattern patternError = Pattern.compile("^BMXAA[0-9]{4}E -- Error - BMXAA[0-9]{4}E*$", Pattern.CASE_INSENSITIVE);
		Pattern patternErrorP = Pattern.compile("^BMXAA[0-9]{4}E$", Pattern.CASE_INSENSITIVE);
		Pattern patternError2 = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#NoAutoKeyName*$",
				Pattern.CASE_INSENSITIVE);
		Pattern patternError3 = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#SeqNameNull*$",
				Pattern.CASE_INSENSITIVE);
		Pattern patternWar = Pattern.compile("^BMXAA[0-9]{4}W*$", Pattern.CASE_INSENSITIVE);
		Pattern patternInfo = Pattern.compile("^BMXAA[0-9]{4}I*$", Pattern.CASE_INSENSITIVE);

		ArrayList<Error> lista = new ArrayList<>();

		FileInputStream fis = new FileInputStream(archivo);
		InputStreamReader isr = new InputStreamReader(fis, "utf8");
		BufferedReader br = new BufferedReader(isr);

		FileOutputStream fos = new FileOutputStream("d:/errores.txt");
		OutputStreamWriter isw = new OutputStreamWriter(fos, "utf8");
		BufferedWriter bw = new BufferedWriter(isw);
		
		FileOutputStream fos2 = new FileOutputStream("d:/total.txt");
		OutputStreamWriter isw2 = new OutputStreamWriter(fos2, "utf8");
		BufferedWriter bw2 = new BufferedWriter(isw2);

		while ((cadena = br.readLine()) != null) {
			cadena = cadena.trim();
			bw2.write(cadena + "\n");
		}
		
		
		
		while ((cadena = br.readLine()) != null) {
			cadena = cadena.trim();
			bw2.write(cadena + "\n");
			
			
			
			if (esErrorP(cadena, patternErrorP)) {
				String errorMain = "";
				String Desc = "";
				if (esError(cadena, patternError)) {
					errorMain = cadena.substring(0, 32);
					Desc = cadena.substring(32, cadena.length());
				} else if (esError2(cadena, patternError2)) {
					errorMain = cadena.substring(0, 45);
					Desc = cadena.substring(45, cadena.length());
				} else if (esError3(cadena, patternError3)) {
					errorMain = cadena.substring(0, 43);
					Desc = cadena.substring(43, cadena.length());
				} else if (esInfo(cadena, patternInfo)) {
					break;
				} else if (esWar(cadena, patternWar)) {
					break;
				}
				while ((cadena = br.readLine()) != null) {
					if (esErrorP(cadena, patternErrorP) || esError(cadena, patternError) || esInfo(cadena, patternInfo)
							|| esWar(cadena, patternWar) || esError2(cadena, patternError2)
							|| esError3(cadena, patternError3))
						break;
					else {
						String atributos = atributo(cadena);
						if (atributos != null) {
							bw.write(errorMain + "---" + atributos + "\n");
							Error obj = crearObj(errorMain, Desc, atributos);
							lista.add(obj);
							System.out.println(errorMain);
						}
					}
				}
			}
		}
		bw.close();
		isw.close();
		fos.close();
		bw2.close();
		isw2.close();
		fos2.close();
		br.close();
		isr.close();
		fis.close();
		lista.sort(new Comparator<Error>() {

			@Override
			public int compare(Error o1, Error o2) {
				return o1.getError().compareTo(o2.getError());
			}

		});
		return lista;
	}
	
	
	
	

	static void ImprimirObj(ArrayList<Error> lista) {
		for (int i = 0; i < lista.size(); i++) {
			Error error = lista.get(i);
			System.out.println(error.getError() + " - " + error.getDescrpcion() + " - " + error.getObjeto() + " - "
					+ error.getAtributo());
		}
	}

	static boolean validar(String cadena, Pattern pa) {
		String a = "";
		if (cadena.length() > 9) {
			a = cadena.trim().substring(0, 10);
			System.out.println(a);
		} else
			return false;
		if (pa.matcher(a).find())
			return true;
		return false;
	}

	static boolean esErrorP(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 9) {
			a = b.substring(0, 10);

		} else
			return false;
		if (pa.matcher(a).find()) {
			return true;
		}
		return false;
	}

	static boolean esError(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 31) {
			a = b.substring(0, 32);
		} else
			return false;
		if (pa.matcher(a).find())
			return true;
		return false;
	}

	static boolean esError2(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 44) {
			a = b.substring(0, 45);
		} else
			return false;
		if (pa.matcher(a).find()) {
			return true;
		}
		return false;
	}

	static boolean esError3(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 42) {
			a = b.substring(0, 43);
		} else
			return false;
		if (pa.matcher(a).find()) {
			System.out.println(a);
			return true;
		}
		return false;
	}

	static boolean esWar(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 9) {
			a = b.substring(0, 10);
		}
		if (pa.matcher(a).find())
			return true;
		return false;
	}

	static boolean esInfo(String cadena, Pattern pa) {
		String a = "";
		String b = cadena.trim();
		if (b.length() > 9) {
			a = b.substring(0, 10);
		}
		if (pa.matcher(a).find())
			return true;
		return false;
	}

	static String atributo(String archivo) {
		String b = archivo.trim();
		String[] aux = b.split(" ");
		if (aux[0].equals(";") || aux[0].equals("Select"))
			return null;
		return aux[0].trim();
	}

	static Error crearObj(String mainE, String Desc, String attr) {

		String[] aux = attr.split("[.]");

		Error auxE = new Error();
		if (aux.length > 1) {
			auxE.setError(mainE);
			auxE.setDescrpcion(Desc);
			auxE.setObjeto(aux[0]);
			auxE.setAtributo(aux[1]);
		}
		if (aux.length == 1) {
			auxE.setError(mainE);
			auxE.setDescrpcion(Desc);
			auxE.setAtributo(aux[0]);
		}
		return auxE;
	}
}
