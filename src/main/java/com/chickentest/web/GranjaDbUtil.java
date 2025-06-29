package com.chickentest.web;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.util.*;
import java.util.Date;
import javax.sql.DataSource;

@Component


public class GranjaDbUtil {
	private DataSource dataSource;
	
@Autowired
	public GranjaDbUtil(DataSource theDataSource) {
		dataSource = theDataSource;
	}
	
//LOGIN-------------------------------------------------------------------------------------
	public Usuarios obtengoUsuario(String usuario, String contraseña) throws Exception {		
		Usuarios user = null;
		Connection myConn=null;
		PreparedStatement myStmt = null;
		ResultSet myRs = null;
		
		try{
			//obtener una conexión
			myConn = dataSource.getConnection();
			//crear sentencias
			String sql = "select * from usuarios where (username=? and password=?)";
			myStmt = myConn.prepareStatement(sql);
			myStmt.setString(1, usuario);
			myStmt.setString(2, contraseña);
			
			//ejecutar query
			myRs = myStmt.executeQuery();
			//procesar resultado
			if (myRs.next()) {
				//Debo crear el usuario para la sesión
				String username = myRs.getString("username");
				String password = myRs.getString("password");
				double saldo = Double.parseDouble((myRs.getString("saldo")));
				//utiliza el constructor con id para crear el estudiante
				user = new Usuarios(username, password, saldo);
				return user;
			}
			return user;
			
			//cerrar objetos jdbc
		}
		finally {
			close(myConn, myStmt, myRs);
		}
				
	}
//ACTUALIZO SALDO-------------------------------------------------------------------------------------
		public void actualizoSaldo(String username, double saldo) throws Exception{
			Connection myConn = null;
			PreparedStatement myStmt=null;
			try {
			//obtener la conexión
			myConn = dataSource.getConnection();
			
			//crear el update sql
			String sql = "update usuarios " + "set saldo=?" + "where username=?";
			//preparar la sentencia
			myStmt = myConn.prepareStatement(sql);
			//setear parámetros

			myStmt.setDouble(1, saldo);
			myStmt.setString(2, username);

				//ejecutar la sentencia
			myStmt.execute();
			}
			finally {
				close(myConn, myStmt, null);
			}
		}
	
	


	//LISTAR-------------------------------------------------------------------------------------
	public List<Articulos> listarArticulos() throws Exception{
		List<Articulos> articulos = new ArrayList<>();
		
		Connection myConn=null;
		Statement myStmt = null;
		ResultSet myRs = null;
		
		try{
			//obtener una conexión
			myConn = dataSource.getConnection();
			//crear sentencias
			String sql = "select * from articulos order by nombre";
			myStmt = myConn.createStatement();
			//ejecutar query
			myRs = myStmt.executeQuery(sql);
			//procesar resultado
			while (myRs.next()) {
				//obtener el dato de la columna desde la fila
				int id = myRs.getInt("idarticulo");
				int unidades = myRs.getInt("unidades");
				int stock = myRs.getInt("stock");
				String nombre = myRs.getString("nombre");
				double precio = myRs.getDouble("precio");
				String precioS = String.format("%.2f",precio);
				String categoria = myRs.getString("categoria");
				Date creacion = myRs.getDate("creacion");
				int edad = (int) java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.parse(creacion.toString()), java.time.LocalDate.now());

				String produccion = myRs.getString("produccion");
				//CREO el articulo
				Articulos tempArticulo = new Articulos(id, unidades, nombre, precio, categoria, edad, stock, produccion, creacion, precioS);
				//VERIFICO de lo obtenido de la base para modificarlo si es necesario 
				verificoDatos(tempArticulo);
				//UPDATE para tener la info actualizada por el sistema
				//agregarlo a la lista de articulos
				if (tempArticulo != null) {
					articulos.add(tempArticulo);
				}
			}
			return articulos;
		}
		finally {
			close(myConn, myStmt, myRs);
		}
	}
	public int cantidadStock(String tipo) throws Exception {
		Connection myConn = null;
		PreparedStatement myStmt = null;
		ResultSet myRs = null;
		try {
			myConn = dataSource.getConnection();
			//crear sql para obtener el estudiante seleccionado
			String sql = "select sum(stock) from articulos where categoria like (?)";
			//crear la sentencia preparada
			myStmt = myConn.prepareStatement(sql);
			//setear parámetros
			tipo = "%" + tipo + "%";
			myStmt.setString(1, tipo);
			//ejecutar sentencia
			myRs = myStmt.executeQuery();
			//obtener la información de la línea obtenida
			if (myRs.next()) {//si procesa lineas..
				int stock = myRs.getInt("sum(stock)");
				return stock;
			}
		}
		finally {
			//limpiar el objeto jdbc
			close(myConn, myStmt, null);//no hay resultado y se pasa un null
			
		}
		return 0;
	}

//VERIFICAR OBJETO-------------------------------------------------------------------------------------
		public void verificoDatos(Articulos tempArticulo) throws Exception {
			if (tempArticulo.getCategoria().contains("Huevos")) {
				if (tempArticulo.getEdad() >= 21) {
					eclosionar(tempArticulo);
				}
			}
			if ((tempArticulo.getCategoria().contains("Gallinas"))){
				if (tempArticulo.getEdad() >= 200) {
					gallinaFallece(tempArticulo);
				}
			}
		}
		private void gallinaFallece(Articulos tempArticulo) throws Exception {
			String id = Integer.toString(tempArticulo.getId());
			eliminarArticulo(id);
			Movimientos movimiento = new Movimientos(tempArticulo.getCategoria(), 0, "Granja ChickenTest");
			registroMovimiento(movimiento, "fallecimiento");
		}
		private void eclosionar(Articulos tempArticulo) throws Exception {
			tempArticulo.setNombre("Gallina");
			tempArticulo.setCategoria("Gallina");
			tempArticulo.setPrecio(3000.00);
			int gallinas = cantidadStock("Gallinas");
			int nuevoStock = gallinas + tempArticulo.getUnidades() * tempArticulo.getStock();
			if (nuevoStock < 1500) {
			tempArticulo.setStock(nuevoStock);
			tempArticulo.setUnidades(1);
			modificarArticulo(tempArticulo);
			Movimientos movimiento = new Movimientos(tempArticulo.getCategoria(), 0, tempArticulo.getProduccion());
			registroMovimiento(movimiento, "nacimiento");
			}
			else {
				gallinaFallece(tempArticulo);
			}
		}
			
	
//LOAD-------------------------------------------------------------------------------------
		public Articulos obtengoArticulo(String ArticuloId) throws Exception {
			Articulos elArticulo = null;
			Connection myConn = null;
			PreparedStatement myStmt = null;
			ResultSet myRs = null;
			
			try {
				int articuloId = Integer.parseInt(ArticuloId);
				//conectar a la base de datos
				myConn = dataSource.getConnection();
				//crear sql para obtener el estudiante seleccionado
				String sql = "select * from articulos where idarticulo=?";
				//crear la sentencia preparada
				myStmt = myConn.prepareStatement(sql);
				//setear parámetros
				myStmt.setInt(1, articuloId);
				//ejecutar sentencia
				myRs = myStmt.executeQuery();
				//obtener la información de la línea obtenida
				if (myRs.next()) {//si procesa lineas..
					int unidades = myRs.getInt("unidades");
					int stock = myRs.getInt("stock");
					String nombre = myRs.getString("nombre");
					double precio = myRs.getDouble("precio");
					Date creacion = myRs.getDate("creacion");
					String categoria = myRs.getString("categoria");
					String produccion = myRs.getString("produccion");
					int edad = (int) java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.parse(creacion.toString()), java.time.LocalDate.now());
					//utiliza el constructor con id para crear el estudiante
					elArticulo = new Articulos(articuloId, unidades, nombre, precio, categoria, edad, stock, produccion, creacion);

				}
				else {
					throw new Exception("No se pudo encontrar el articulo con id: " + articuloId);
				}
				return elArticulo;
			}
			finally {
				//limpiar el objeto jdbc
				close(myConn, myStmt, null);//no hay resultado y se pasa un null
				
			}
		}
		
		
			
//AGREGAR-------------------------------------------------------------------------------------			
			public void agregoArticulo(Articulos elArticulo) throws Exception {
				Connection myConn = null;
				PreparedStatement myStmt = null;
				
				try{
					// crear la sentencia sql para el insert
					myConn = dataSource.getConnection();
					String sql = "insert into articulos" + "(idarticulo, nombre, categoria, unidades, stock, precio, produccion, creacion)" 
								 + "values (?, ?, ?, ?, ?, ?, ?, ?)";
					myStmt = myConn.prepareStatement(sql);
					
					//setear los parámetros para el estudiante
					myStmt.setInt(1, elArticulo.getId());
					myStmt.setString(2, elArticulo.getNombre());
					myStmt.setString(3, elArticulo.getCategoria());
					myStmt.setInt(4, elArticulo.getUnidades());
					myStmt.setInt(5, elArticulo.getStock());
					myStmt.setDouble(6, elArticulo.getPrecio());
					myStmt.setString(7, elArticulo.getProduccion());
					myStmt.setDate(8,  java.sql.Date.valueOf(elArticulo.getCreacion().toString()));
					//ejecutar el insert
					myStmt.execute();
	
				}
				
				finally {
					//limpiar el objeto jdbc
					close(myConn, myStmt, null);//no hay resultado y se pasa un null
					
				}
				
			
		}
//MODIFICAR-------------------------------------------------------------------------------------		
		public void modificarArticulo(Articulos elArticulo) throws Exception{
			Connection myConn = null;
			PreparedStatement myStmt=null;
			try {
			//obtener la conexión
			myConn = dataSource.getConnection();
			
			//crear el update sql
			String sql = "update articulos " + "set nombre=?, categoria=?, "
					+ "unidades=?, stock=?, precio=?, creacion=? where idarticulo=?";
			//preparar la sentencia
			myStmt = myConn.prepareStatement(sql);
			//setear parámetros

			myStmt.setString(1, elArticulo.getNombre());
			myStmt.setString(2, elArticulo.getCategoria());
			myStmt.setInt(3, elArticulo.getUnidades());
			myStmt.setInt(4, elArticulo.getStock());
			myStmt.setDouble(5, elArticulo.getPrecio());
			myStmt.setDate(6, (java.sql.Date) elArticulo.getCreacion());
			myStmt.setInt(7, elArticulo.getId());

			//ejecutar la sentencia
			myStmt.execute();
			}
			finally {
				close(myConn, myStmt, null);
			}
		}
//ELIMINAR-------------------------------------------------------------------------------------
		public void eliminarArticulo(String ArticuloId) throws Exception{
			Connection myConn = null;
			PreparedStatement myStmt=null;
			try {
				//convertir el id a int
				int articuloId = Integer.parseInt(ArticuloId);
				//obtener la conexión
				myConn = dataSource.getConnection();
				//crear sql para eliminar estudiante
				String sql = "delete from articulos where idarticulo=?";
				//preparar la sentencia
				myStmt = myConn.prepareStatement(sql);
				//setear parámetros
				myStmt.setInt(1, articuloId);
				//ejecutar la sentencia 
				myStmt.execute();
			}
			finally {
				// limpiar los objetos jdbc
				close(myConn, myStmt, null);
			}
			
		}

//MOVIMIENTOS--------------------------------------------------------------------------------
		public void registroMovimiento(Movimientos movimiento, String tipo) throws Exception {
			Connection myConn = null;
			PreparedStatement myStmt = null;
			try{
				// crear la sentencia sql para el insert
				myConn = dataSource.getConnection();
				String sql = "insert into movimientos" + "(articulo, fecha, tipo, monto, username)" 
							 + "values (?, ?, ?, ?, ?)";
				myStmt = myConn.prepareStatement(sql);
				
				//setear los parámetros para el estudiante
				myStmt.setString(1, movimiento.getArticulo());
				//myStmt.setString(2, movimiento.getNombre());
				myStmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
				myStmt.setString(3, tipo);
				myStmt.setDouble(4, movimiento.getMonto());
				myStmt.setString(5, movimiento.getUsername());
				//ejecutar el insert
				myStmt.execute();
				
				
			
			}
			finally {
				//limpiar el objeto jdbc
				close(myConn, myStmt, null);//no hay resultado y se pasa un null
				
			}
			
		}

		public List<Movimientos> listarMovimientos() throws Exception{
			List<Movimientos> movimientos = new ArrayList<>();
			
			Connection myConn=null;
			Statement myStmt = null;
			ResultSet myRs = null;
			
			try{
				//obtener una conexión
				myConn = dataSource.getConnection();
				//crear sentencias
				String sql = "select * from movimientos order by fecha";
				myStmt = myConn.createStatement();
				//ejecutar query
				myRs = myStmt.executeQuery(sql);
				//procesar resultado
				while (myRs.next()) {
					//obtener el dato de la columna desde la fila
					String articulo = myRs.getString("articulo");
					Date fecha = myRs.getDate("fecha");
					String tipo = myRs.getString("tipo");
					double monto = myRs.getDouble("monto");
					String username = myRs.getString("username");
					
					//CREO el articulo
					Movimientos tempMovimiento= new Movimientos(articulo, fecha, tipo, monto, username);
					
					movimientos.add(tempMovimiento);
					}

				return movimientos;
				}
				//cerrar objetos jdbc			
			finally {
				close(myConn, myStmt, myRs);
			}
		}
			
//CLOSE-------------------------------------------------------------------------------------		
		private void close(Connection myConn, Statement myStmt, ResultSet myRs) {
			try {
				if (myRs!=null) {
					myRs.close();
				}
				if (myStmt!=null) {
					myStmt.close();
				}
				if (myConn!=null) {
					myConn.close();//devuelve la conexión a disponible a la pool
				}
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		

}
