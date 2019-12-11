import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
				String descr = "/* \n" + errores.get(i).getError() + " --- " + errores.get(i).getDescrpcion() + "\n */";

				String[] parametros = errores.get(i).getParametros().split(" ");
				String consulta = "";
				if (parametros.length == 2) {
					consulta = String.format(solucions, parametros[0], parametros[1]);
				} else if (parametros.length == 4) {
					consulta = String.format(solucions, parametros[0], parametros[1], parametros[3], parametros[2]);
				}

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
		Pattern patternError = Pattern.compile("^BMXAA[0-9]{4}E -- Error - BMXAA[0-9]{4}E", Pattern.CASE_INSENSITIVE);
		Pattern patternError4 = Pattern.compile("^BMXAA0443E -- Error - BMXAA0497E", Pattern.CASE_INSENSITIVE);
		Pattern patternErrorP = Pattern.compile("^BMXAA[0-9]{4}E", Pattern.CASE_INSENSITIVE);
		Pattern patternErrorNoAutoKeyName = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#NoAutoKeyName",
				Pattern.CASE_INSENSITIVE);
		Pattern patternErrorSeqNameNull = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#SeqNameNull",
				Pattern.CASE_INSENSITIVE);
		Pattern patternWar = Pattern.compile("^BMXAA[0-9]{4}W", Pattern.CASE_INSENSITIVE);
		Pattern patternInfo = Pattern.compile("^BMXAA[0-9]{4}I", Pattern.CASE_INSENSITIVE);

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

		String cadena1 = "";
		String cadena2 = "";

		ArrayList<String> ficherocompleto = new ArrayList<>();
		while ((cadena1 = br.readLine()) != null) {
			ficherocompleto.add(cadena1.trim());
		}

		for (int i = 0; i < ficherocompleto.size() - 2; i++) {
			cadena = ficherocompleto.get(i);
			// bw2.write(cadena + "\n");
			if (esErrorP(cadena, patternErrorP)) {
				String errorMain = "";
				String Desc = "";
				cadena2 = ficherocompleto.get(i + 1);
				if (esError(cadena, patternError4)) {
					errorMain = cadena.substring(0, 32);
					Desc = cadena.substring(32, cadena.length()) + "\n" + cadena2;
					String at = "";
					at += cadena2.split(" ")[0].split("[.]")[0] + " ";// o
					at += cadena2.split(" ")[0].split("[.]")[1] + " ";// a
					int val = Integer.parseInt(cadena2.split("\\(")[1].split("\\)")[0].split("[.]")[0]);// 50
					int val2 = Integer.parseInt(cadena2.split("\\(")[1].split("\\)")[0].split("[.]")[0]);// 30
					if (val > val2) {
						at += val + " ";
					} else {
						at += val2 + " ";
					}
					at += cadena2.split(" ")[4].split("[.]")[0];// o
					String atributos = at;
					atributos = atributos.replace(".", " ");
					bw.write(errorMain + "---" + atributos + "\n");
					Error obj = new Error(errorMain, Desc, atributos);
					lista.add(obj);

				} else if (esError(cadena, patternError)) {
					errorMain = cadena.substring(0, 32);
					Desc = cadena.substring(32, cadena.length()) + "\n" + cadena2;
					while (!esErrorP(cadena2, patternErrorP) && !esError(cadena2, patternError)
							&& !esInfo(cadena2, patternInfo) && !esWar(cadena2, patternWar)
							&& !esError2(cadena2, patternErrorNoAutoKeyName)
							&& !esError3(cadena2, patternErrorSeqNameNull)) {
						i++;
						String atributos = cadena2.split(" ")[0];
						atributos = atributos.replace(".", " ");
						bw.write(errorMain + "---" + atributos + "\n");
						Error obj = new Error(errorMain, Desc, atributos);
						lista.add(obj);
						cadena2 = ficherocompleto.get(i + 1);
					}
				} else if (esError2(cadena, patternErrorNoAutoKeyName)) {
					errorMain = cadena.substring(0, 45);
					Desc = cadena.substring(45, cadena.length()) + "\n" + cadena2;
					String atributos = cadena2.split(" ")[0];
					atributos = atributos.replace(".", " ");
					bw.write(errorMain + "---" + atributos + "\n");
					Error obj = new Error(errorMain, Desc, atributos);
					lista.add(obj);
				} else if (esError3(cadena, patternErrorSeqNameNull)) {
					errorMain = cadena.substring(0, 43);
					Desc = cadena.substring(43, cadena.length()) + "\n" + cadena2;
					String atributos = cadena2.split(" ")[0];
					atributos = atributos.replace(".", " ");
					bw.write(errorMain + "---" + atributos + "\n");
					Error obj = new Error(errorMain, Desc, atributos);
					lista.add(obj);
				} else if (esInfo(cadena, patternInfo)) {
					break;
				} else if (esWar(cadena, patternWar)) {
					break;
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
			auxE.setParametros(aux[0]);
		}
		if (aux.length == 1) {
			auxE.setError(mainE);
			auxE.setDescrpcion(Desc);
		}
		return auxE;
	}
}
