package com.chickentest.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;




/**
 * Servlet implementation class GranjaControlador
 */
@WebServlet("/GranjaControlador")
public class GranjaControlador extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private GranjaDbUtil granjaDbUtil;
	
	@Resource(name="jdbc/ChickenTestWeb")
	private DataSource dataSource;
	
	@Override
	public void init() throws ServletException {
		// crea la granjaDbUtil y la pasa a la conexión en el pool
		super.init();
		granjaDbUtil = new GranjaDbUtil(dataSource);
			
		}
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			String theCommand = request.getParameter("command");
			if (theCommand == null) {
				theCommand = "LIST";
			}
			switch(theCommand) {
			//ACCIONES BASICAS EN ARTICULOS
			case "LIST":
				listarArticulos(request, response);
				break;
			case "INSERT":
				agregarArticulo(request, response);
				break;
			case "LOAD":
				cargarArticulo(request, response);
				break;
			case "UPDATE":
				modificarArticulo(request, response);
				break;
			case "DELETE":
				eliminarArticulo(request, response);
				break;
			case "REPORTE":
				generoReporte(request, response);
				break;
			case "COMPRA":
				comprarArticulo(request, response);
				break;
			case "VENTA":
				venderArticulo(request, response);
				break;
			//ACCIONES DE USUARIO
			case "LOGIN":
				iniciarSesion(request, response);
				break;
			case "LOGOUT":
				cerrarSesion(request, response);
				break;
     		default:
     			listarArticulos(request, response);
				break;
			}
						
		}
		catch(Exception exc) {
			throw new ServletException(exc);
		}
	}

	

	//ACCIONES DE USUARIO: LOGIN y LOGOUT----------------------------------------------------------------
	
	//MANEJO DE SESION
	private void iniciarSesion(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String usuario = request.getParameter("usuario");
		String contraseña = request.getParameter("password");
		Usuarios user = granjaDbUtil.obtengoUsuario(usuario, contraseña);
		
		if (user != null){
			request.getSession().setAttribute("USER", user);
			listarArticulos(request, response);
			
		}
		else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/login-error.jsp");
			dispatcher.forward(request, response);
		}
		
	}
	private void actualizoUsuario(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Usuarios user = (Usuarios)request.getSession().getAttribute("USER");
		Usuarios usuarioActualizado = granjaDbUtil.obtengoUsuario(user.getUsuario(), user.getContraseña());
		request.getSession().setAttribute("USER", usuarioActualizado);
	}
	private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.getSession().invalidate();
		RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
		dispatcher.forward(request, response);
	}
//ACCIONES DE GRANJA: MOVIMIENTOS COMPRA Y VENTA----------------------------------------------------------------------
	private void venderArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//Inicia la venta, se genera la lista con los articulos en el sistema
		String articuloId = request.getParameter("articuloId");
		String cantidad = request.getParameter("cantidad");
		//Si el usuario eligió la cantidad a comprar continúa el proceso de venta
		if (cantidad != null && articuloId!=null) {
			//Si se selecciona un articulo vuelve a ingresar al método con un id
			//obtengo los datos del usuario
			Usuarios user = (Usuarios)request.getSession().getAttribute("USER");
			//traigo el artículo seleccionado
					
			Articulos elArticulo = granjaDbUtil.obtengoArticulo(articuloId);
				
			//realizo las operaciones
			double monto = elArticulo.getPrecio() * Integer.parseInt(cantidad);
			int nuevoStock = elArticulo.getStock() - Integer.parseInt(cantidad);
			
			if (nuevoStock < 0) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/error-venta.jsp");
				dispatcher.forward(request, response);
				
			}
			else {
				elArticulo.setStock(nuevoStock);//nuevo stock
				Double nuevoSaldo=(user.getSaldo() + monto); 
				Movimientos movimiento = new Movimientos(elArticulo.getCategoria(), monto, user.getUsuario());
				
				//y luego actualizo saldo, artículo y registro el movimiento
				granjaDbUtil.modificarArticulo(elArticulo);
				granjaDbUtil.registroMovimiento(movimiento, "venta");
				granjaDbUtil.actualizoSaldo(user.getUsuario(), nuevoSaldo);
				actualizoUsuario(request, response);
				listarArticulos(request, response);
			}
			
		}

	}

	private void comprarArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//Inicia la compra, se genera la lista con los articulos en el sistema
		String articuloId = request.getParameter("articuloId");
		String cantidad = request.getParameter("cantidad");
		//Si el usuario eligió la cantidad a comprar continúa el proceso de compra
		if (cantidad != null && articuloId!=null) {
			//Si se selecciona un articulo vuelve a ingresar al método con un id
			//obtengo los datos del usuario
			Usuarios user = (Usuarios)request.getSession().getAttribute("USER");
			//traigo el artículo seleccionado
			
			Articulos elArticulo = granjaDbUtil.obtengoArticulo(articuloId);
			
			//realizo las operaciones
			double monto = elArticulo.getPrecio() * Integer.parseInt(cantidad);
			elArticulo.setStock(elArticulo.getStock() + Integer.parseInt(cantidad));//nuevo stock
			Double nuevoSaldo=(user.getSaldo() - monto); 
			if (nuevoSaldo < 0) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/error-compra.jsp");
				dispatcher.forward(request, response);
			}else{
				if (calculoStock(elArticulo)) {
					Movimientos movimiento = new Movimientos(elArticulo.getCategoria(), monto, user.getUsuario());
					//y luego actualizo saldo, artículo y registro el movimiento
					granjaDbUtil.registroMovimiento(movimiento, "compra");
					granjaDbUtil.actualizoSaldo(user.getUsuario(), nuevoSaldo);
					actualizoUsuario(request, response);
					granjaDbUtil.modificarArticulo(elArticulo);
					listarArticulos(request, response);
				}
				else {
					RequestDispatcher dispatcher = request.getRequestDispatcher("/error-stockMaximo.jsp");
					dispatcher.forward(request, response);
				}
			}

			
		}
	}
	
//ARTICULOS-----------------------------------------------------------------------------------------------------------------	
	private void listarArticulos(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// crea lista
		List<Articulos> articulos = granjaDbUtil.listarArticulos();	
		boolean diario = false;
		//verifico si hay lote diario
		for (Articulos articulo : articulos) {
			if (articulo.getEdad() == 0 && articulo.getProduccion().toLowerCase().contains("chickentest")) {
				diario = true;
				
			}
		}
		int gallinas = granjaDbUtil.cantidadStock("gallina");
		if (!diario && gallinas!=0) {
			Articulos elArticulo = generoLote(gallinas);
			articulos.add(elArticulo);
			
		}
		// setea lista
		request.getSession().setAttribute("LISTA_ARTICULOS", articulos);
		// send to JSP page (view)
		RequestDispatcher dispatcher = request.getRequestDispatcher("/lista-articulos.jsp");
		dispatcher.forward(request, response);
}
	private boolean calculoStock(Articulos elArticulo) throws Exception {
		int gallinas = granjaDbUtil.cantidadStock("Gallinas");
		int huevos = granjaDbUtil.cantidadStock("Huevo");
		
		if (elArticulo.getCategoria().contains("Huevo") && elArticulo.getStock() + huevos > 2000) {
			return false;
		}
		if (elArticulo.getCategoria().contains("Gallina") && elArticulo.getStock() + gallinas > 1500) {
			return false;
		}
		return true;
		
	}
	//LOTE DIARIO-----------------------------------------------------------------------------------------
	private Articulos generoLote(int gallinas) throws Exception {
		Articulos elArticulo = new Articulos(gallinas, "Lote ChickenTest", 1200.00, "Huevos", 0, 1, "Granja ChickenTest", java.sql.Date.valueOf(java.time.LocalDate.now()), "1200.00");
		granjaDbUtil.agregoArticulo(elArticulo);//
		Movimientos movimiento = new Movimientos(elArticulo.getCategoria(), 0, "ChickenTest");
		granjaDbUtil.registroMovimiento(movimiento, "producción");	
		return elArticulo;
		
	}

	private void cargarArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//leer el id del formulario
		String articuloId =request.getParameter("articuloId");
		// obtener el estudiante del db util
		Articulos elArticulo = granjaDbUtil.obtengoArticulo(articuloId);
		//colocarlo al estudiante en el atributo del request
		request.getSession().setAttribute("EL_ARTICULO", elArticulo);
		//enviarlo al formulario de actualización jsp
		RequestDispatcher dispatcher = request.getRequestDispatcher("/modificar-articulo.jsp");
		dispatcher.forward(request, response);
		
	}
	private void agregarArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//leer el formulario
		String nombre = request.getParameter("nombre");
		String categoria = request.getParameter("categoria");
		int unidades = Integer.parseInt(request.getParameter("unidades"));
		double precio = Double.parseDouble(request.getParameter("precio"));
		int stock = Integer.parseInt(request.getParameter("stock"));
		int edad = Integer.parseInt(request.getParameter("edad"));
		Date creacion = java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(edad));
		String produccion= request.getParameter("produccion");
		//crear 
		Articulos elArticulo = new Articulos(unidades, nombre, precio, categoria, stock, produccion, creacion);
		//agregarlo a la base de datos
		if (calculoStock(elArticulo)) {
		granjaDbUtil.agregoArticulo(elArticulo);
		//volver a la lista
		listarArticulos(request, response);
		}
		else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/error-stockMaximo.jsp");
			dispatcher.forward(request, response);
		}
		
	}
	
	private void eliminarArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//leer info del del formulario
		String articuloId = request.getParameter("articuloId");
		//borrar el estudiante
		granjaDbUtil.eliminarArticulo(articuloId);
		//volver a la lista
		listarArticulos(request, response);
		
	}

	private void modificarArticulo(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//leer info del formulario
		String articuloId =request.getParameter("articuloId");
		String nombre = request.getParameter("nombre");
		String categoria = request.getParameter("categoria");
		int edad = Integer.parseInt(request.getParameter("edad"));
		Date creacion = java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(edad));
		int unidades = Integer.parseInt(request.getParameter("unidades"));
		int stock = Integer.parseInt(request.getParameter("stock"));
		double precio = Double.parseDouble(request.getParameter("precio"));
		String produccion= request.getParameter("produccion");
		//crear el objeto 
		Articulos elArticulo = new Articulos(Integer.parseInt(articuloId), unidades, nombre, precio, categoria, stock, produccion, creacion);
		//realizar el update
		granjaDbUtil.modificarArticulo(elArticulo);
		//enviarlo a la lista 
		listarArticulos(request, response);
	}
//REPORTE-------------------------------------------------------------------------------------------	
	private void generoReporte(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Movimientos> movimientos = granjaDbUtil.listarMovimientos();
		List<Articulos> articulos = granjaDbUtil.listarArticulos();	
		Reporte elReporte = new Reporte ();
		//contador de movimientos historico
		for (Movimientos movimiento: movimientos) {
			//Reporte de gallinas
			if (movimiento.getArticulo().toLowerCase().contains("gallina")) {
				if (movimiento.getTipo().toLowerCase().contains("compra")) {
					elReporte.setCantGallinasC(elReporte.getCantGallinasC()+1);
					elReporte.setMontoCompras(elReporte.getMontoCompras() + movimiento.getMonto());
				}
				if (movimiento.getTipo().toLowerCase().contains("venta")) {
					elReporte.setCantGallinasV(elReporte.getCantGallinasV()+1);
					elReporte.setMontoVentas(elReporte.getMontoVentas() + movimiento.getMonto());
				}
				//
				if (movimiento.getTipo().toLowerCase().contains("fallecimiento")) {
					elReporte.setGallinasFallecidas(elReporte.getGallinasFallecidas()+1);
				}
				if (movimiento.getTipo().toLowerCase().contains("nacimiento")) {
					elReporte.setGallinasNacidas(elReporte.getGallinasNacidas()+1);
				}
			}
			//Reporte de los lotes
			if (movimiento.getArticulo().toLowerCase().contains("huevo")) {
				if (movimiento.getTipo().toLowerCase().contains("compra")) {
					elReporte.setCantLotesC(elReporte.getCantLotesC()+1);
					elReporte.setMontoCompras(elReporte.getMontoCompras() + movimiento.getMonto());
				}
				if (movimiento.getTipo().toLowerCase().contains("venta")) {
					elReporte.setCantLotesV(elReporte.getCantLotesV()+1);
					elReporte.setMontoVentas(elReporte.getMontoVentas() + movimiento.getMonto());
				}
				if (movimiento.getTipo().toLowerCase().contains("producción")) {
					elReporte.setCantLotesProducidos(elReporte.getCantLotesProducidos()+1);
				}
			
			}
		}
		
		//contador de articulos actuales
		for (Articulos articulo: articulos) {
			if (articulo.getCategoria().toLowerCase().contains("huevo")){
				elReporte.setHuevos(elReporte.getHuevos() + articulo.getStock() * articulo.getUnidades());
				if (articulo.getProduccion().toLowerCase().contains("chickentest")) {
					elReporte.setCantLotesProducidos(elReporte.getCantLotesProducidos()+1);
				}
			}
			if (articulo.getCategoria().toLowerCase().contains("gallina")){
				elReporte.setGallinas(elReporte.getGallinas() + articulo.getStock() * articulo.getUnidades());
			}
		
		}
		request.getSession().setAttribute("REPORTE", elReporte);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/movimientos-reporte.jsp");
		dispatcher.forward(request, response);
	}
}
