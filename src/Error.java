
public class Error {
	String error;
	String descrpcion;
	String parametros;
	public Error() {
		super();
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getDescrpcion() {
		return descrpcion;
	}
	public void setDescrpcion(String descrpcion) {
		this.descrpcion = descrpcion;
	}
	public String getParametros() {
		return parametros;
	}
	public void setParametros(String objeto) {
		this.parametros = objeto;
	}
	public Error(String error, String descrpcion, String parametros) {
		super();
		this.error = error;
		this.descrpcion = descrpcion;
		this.parametros = parametros;
	}


}
