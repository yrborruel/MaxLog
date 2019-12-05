import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

			ArrayList<Error> errores = prepararTXT("D:\\integcheck.log");
			ArrayList<String> consultas = new ArrayList<>();

			Map<String, String> erroresDB = new HashMap<String, String>();

			while (rs.next()) {
				String error = rs.getString("error");
				String solucion = rs.getString("solucion");
				erroresDB.put(error, solucion);
			}

			for (int i = 0; i < errores.size(); i++) {

				String solucions = "" + erroresDB.get(errores.get(i).getError());
				String descr = "/* \n" + errores.get(i).getError() + " --- " + errores.get(i).getDescrpcion() + " " +errores.get(i).getObjeto()+ "."+ errores.get(i).getAtributo()
						+ "\n */";
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
		// Leer Fichero
		File fichero = new File(archivo);
		FileReader f = new FileReader(fichero);
		BufferedReader b = new BufferedReader(f);
		// Exp Regulares
		Pattern patternError = Pattern.compile("^BMXAA[0-9]{4}E -- Error - BMXAA[0-9]{4}E*$", Pattern.CASE_INSENSITIVE);
		Pattern patternErrorP = Pattern.compile("^BMXAA[0-9]{4}E*$", Pattern.CASE_INSENSITIVE);
		Pattern patternError2 = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#NoAutoKeyName*$",
				Pattern.CASE_INSENSITIVE);
		Pattern patternError3 = Pattern.compile("^BMXAA[0-9]{4}E -- Error - configure#SeqNameNull*$",
				Pattern.CASE_INSENSITIVE);
		Pattern patternWar = Pattern.compile("^BMXAA[0-9]{4}W*$", Pattern.CASE_INSENSITIVE);
		Pattern patternInfo = Pattern.compile("^BMXAA[0-9]{4}I*$", Pattern.CASE_INSENSITIVE);
		// Escribir ficheros
		FileWriter ferrores = new FileWriter("d:/errores.txt");
		PrintWriter err = new PrintWriter(ferrores);

		FileWriter mimomo = new FileWriter("d:/ficheroLog.txt");
		PrintWriter mo = new PrintWriter(mimomo);
		
		ArrayList<Error> lista = new ArrayList<>();

		int cont = 0;
		while ((cadena = b.readLine()) != null) {
			
			cont++;
			if (cont == 500) {
				System.gc();
				cont = 0;
			}
			cadena = cadena.trim();
			mo.println(cadena);
			if (esErrorP(cadena, patternErrorP))
				if (esError(cadena, patternError)) {
					String errorMain = cadena.substring(0, 32);
					String Desc = cadena.substring(32, cadena.length());
					while ((cadena = b.readLine()) != null) {
						if (esErrorP(cadena, patternErrorP) || esError(cadena, patternError)
								|| esInfo(cadena, patternInfo) || esWar(cadena, patternWar)
								|| esError2(cadena, patternError2) || esError3(cadena, patternError3))
							break;
						else {
							String atributos = atributo(cadena);
							if (atributos != null) {
								err.println(errorMain + "---" + atributos);
								Error obj = crearObj(errorMain, Desc, atributos);
								lista.add(obj);
								System.out.println(errorMain);

							}
						}
					}
				} else if (esError2(cadena, patternError2)) {
					String errorMain = cadena.substring(0, 45);
					String Desc = cadena.substring(45, cadena.length());
					while ((cadena = b.readLine()) != null) {
						if (esErrorP(cadena, patternErrorP) || esError(cadena, patternError)
								|| esInfo(cadena, patternInfo) || esWar(cadena, patternWar)
								|| esError2(cadena, patternError2) || esError3(cadena, patternError3))
							break;
						else {
							String atributos = atributo(cadena);
							if (atributos != null) {
								err.println(errorMain + "---" + atributos);
								Error obj = crearObj(errorMain, Desc, atributos);
								lista.add(obj);
								System.out.println(errorMain);
							}
						}
					}
				}else if (esError3(cadena, patternError3)) {
					String errorMain = cadena.substring(0, 43);
					String Desc = cadena.substring(43, cadena.length());
					while ((cadena = b.readLine()) != null) {
						if (esErrorP(cadena, patternErrorP) || esError(cadena, patternError)
								|| esInfo(cadena, patternInfo) || esWar(cadena, patternWar)
								|| esError2(cadena, patternError2) || esError3(cadena, patternError3))
							break;
						else {
							String atributos = atributo(cadena);
							if (atributos != null) {
								err.println(errorMain + "---" + atributos);
								Error obj = crearObj(errorMain, Desc, atributos);
								lista.add(obj);
								System.out.println(errorMain);
							}
						}
					}
				}
		}
		// ImprimirObj(lista);
		ferrores.close();
		mimomo.close();
		b.close();
		lista.sort( new Comparator<Error>() {

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
			return true;}
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
			return true;}
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
